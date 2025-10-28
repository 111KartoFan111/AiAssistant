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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public InterviewDtos.StartInterviewResponse startInterview(InterviewDtos.InterviewSetupRequest request) throws ExecutionException, InterruptedException {
        User currentUser = getCurrentUser();

        InterviewSession session = InterviewSession.builder()
                .userId(currentUser.getId())
                .position(request.getPosition())
                .jobDescription(request.getJobDescription())
                .status(InterviewSession.InterviewStatus.IN_PROGRESS)
                .createdAt(Timestamp.now())
                .build();
        sessionRepository.save(session);

        String firstQuestionText = geminiService.generateInitialQuestion(
            request.getPosition(), 
            request.getJobDescription()
        ).block();

        // ✅ ИСПРАВЛЕНИЕ: Используем System.currentTimeMillis() вместо LocalDateTime.now()
        ChatMessage firstMessage = ChatMessage.builder()
                .sessionId(session.getId())
                .role(ChatMessage.MessageRole.MODEL)
                .content(firstQuestionText)
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(firstMessage);

        return InterviewDtos.StartInterviewResponse.builder()
                .sessionId(Long.valueOf(session.getId()))
                .firstMessage(new InterviewDtos.ChatMessageResponse(
                    firstMessage.getRole(), 
                    firstMessage.getContent()
                ))
                .build();
    }
    
    public InterviewDtos.ChatMessageResponse postMessage(String sessionId, InterviewDtos.UserMessageRequest request) throws ExecutionException, InterruptedException {
        // ✅ ИСПРАВЛЕНИЕ: Используем System.currentTimeMillis()
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getContent())
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(userMessage);

        List<ChatMessage> chatHistory = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);

        String aiResponseText = geminiService.generateNextResponse(chatHistory).block();

        // ✅ ИСПРАВЛЕНИЕ: Используем System.currentTimeMillis()
        ChatMessage aiMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.MODEL)
                .content(aiResponseText)
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(aiMessage);
        
        return new InterviewDtos.ChatMessageResponse(aiMessage.getRole(), aiMessage.getContent());
    }

    private User getCurrentUser() throws ExecutionException, InterruptedException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}