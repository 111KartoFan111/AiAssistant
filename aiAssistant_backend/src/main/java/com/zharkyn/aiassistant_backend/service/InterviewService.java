package com.zharkyn.aiassistant_backend.service;

import com.google.cloud.Timestamp;
import com.zharkyn.aiassistant_backend.dto.InterviewDtos;
import com.zharkyn.aiassistant_backend.model.ChatMessage;
import com.zharkyn.aiassistant_backend.model.InterviewSession;
import com.zharkyn.aiassistant_backend.model.User;
import com.zharkyn.aiassistant_backend.repository.ChatMessageRepository;
import com.zharkyn.aiassistant_backend.repository.InterviewSessionRepository;
import com.zharkyn.aiassistant_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public InterviewDtos.StartInterviewResponse startInterview(InterviewDtos.InterviewSetupRequest request) 
            throws ExecutionException, InterruptedException {
        log.info("Starting interview for position: {}", request.getPosition());
        
        User currentUser = getCurrentUser();
        log.info("Current user: {}", currentUser.getEmail());

        // Create interview session
        InterviewSession session = InterviewSession.builder()
                .userId(currentUser.getId())
                .position(request.getPosition())
                .jobDescription(request.getJobDescription())
                .status(InterviewSession.InterviewStatus.IN_PROGRESS)
                .createdAt(Timestamp.now())
                .build();
        session = sessionRepository.save(session);
        log.info("Interview session created with ID: {}", session.getId());

        // Generate first question
        String firstQuestionText;
        try {
            firstQuestionText = geminiService.generateInitialQuestion(
                request.getPosition(), 
                request.getJobDescription() != null ? request.getJobDescription() : ""
            ).block();
            log.info("First question generated: {}", firstQuestionText);
        } catch (Exception e) {
            log.error("Error generating first question", e);
            throw new RuntimeException("Failed to generate first question: " + e.getMessage(), e);
        }

        // Save first message
        ChatMessage firstMessage = ChatMessage.builder()
                .sessionId(session.getId())
                .role(ChatMessage.MessageRole.MODEL)
                .content(firstQuestionText)
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(firstMessage);
        log.info("First message saved");

        return InterviewDtos.StartInterviewResponse.builder()
                .interviewId(session.getId())  // Return as String
                .firstQuestion(firstQuestionText)  // Match frontend expectations
                .build();
    }
    
    public InterviewDtos.ChatMessageResponse postMessage(String sessionId, InterviewDtos.UserMessageRequest request) 
            throws ExecutionException, InterruptedException {
        log.info("Processing message for session: {}", sessionId);
        
        // Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getAnswer())  // Changed from getContent() to getAnswer()
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(userMessage);
        log.info("User message saved");

        // Get chat history
        List<ChatMessage> chatHistory = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        log.info("Retrieved {} messages from history", chatHistory.size());

        // Generate AI response
        String aiResponseText;
        try {
            aiResponseText = geminiService.generateNextResponse(chatHistory).block();
            log.info("AI response generated");
        } catch (Exception e) {
            log.error("Error generating AI response", e);
            throw new RuntimeException("Failed to generate response: " + e.getMessage(), e);
        }

        // Save AI message
        ChatMessage aiMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.MODEL)
                .content(aiResponseText)
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(aiMessage);
        log.info("AI message saved");
        
        return InterviewDtos.ChatMessageResponse.builder()
                .role(aiMessage.getRole())
                .content(aiMessage.getContent())
                .nextQuestion(aiMessage.getContent())  // Add for frontend compatibility
                .build();
    }

    private User getCurrentUser() throws ExecutionException, InterruptedException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting current user: {}", userEmail);
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));
    }
}