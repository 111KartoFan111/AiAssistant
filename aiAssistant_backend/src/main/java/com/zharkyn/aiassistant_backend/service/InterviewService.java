package com.zharkyn.aiassistant_backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
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

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
                .interviewId(session.getId())
                .firstQuestion(firstQuestionText)
                .build();
    }
    
    public InterviewDtos.ChatMessageResponse postMessage(String sessionId, InterviewDtos.UserMessageRequest request) 
            throws ExecutionException, InterruptedException {
        log.info("Processing message for session: {}", sessionId);
        
        // Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getAnswer())
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
                .nextQuestion(aiMessage.getContent())
                .build();
    }

    /**
     * Получить историю всех интервью текущего пользователя
     * ✅ ИСПРАВЛЕНО: Убрана сортировка orderBy для избежания требования индекса
     */
    public List<InterviewDtos.InterviewHistoryResponse> getInterviewHistory() 
            throws ExecutionException, InterruptedException {
        log.info("Fetching interview history");
        
        User currentUser = getCurrentUser();
        
        // Запрос БЕЗ orderBy - не требует индекса
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("interview_sessions")
                .whereEqualTo("userId", currentUser.getId())
                .get();
        
        List<InterviewSession> sessions = future.get().toObjects(InterviewSession.class);
        log.info("Found {} interview sessions", sessions.size());
        
        // Сортируем в Java по убыванию даты (новые сначала)
        return sessions.stream()
                .sorted(Comparator.comparing(InterviewSession::getCreatedAt, 
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(session -> InterviewDtos.InterviewHistoryResponse.builder()
                        .id(session.getId())
                        .position(session.getPosition())
                        .jobDescription(session.getJobDescription())
                        .status(session.getStatus())
                        .startTime(session.getCreatedAt() != null ? 
                                session.getCreatedAt().toDate().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Получить детальную информацию об интервью
     */
    public InterviewDtos.InterviewDetailResponse getInterviewDetails(String interviewId) 
            throws ExecutionException, InterruptedException {
        log.info("Fetching interview details for: {}", interviewId);
        
        User currentUser = getCurrentUser();
        
        // Get interview session
        Firestore dbFirestore = FirestoreClient.getFirestore();
        InterviewSession session = dbFirestore.collection("interview_sessions")
                .document(interviewId)
                .get()
                .get()
                .toObject(InterviewSession.class);
        
        if (session == null) {
            throw new RuntimeException("Interview not found");
        }
        
        // Verify ownership
        if (!session.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to interview");
        }
        
        // Get conversation messages
        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByTimestampAsc(interviewId);
        log.info("Found {} messages for interview", messages.size());
        
        List<InterviewDtos.ConversationMessage> conversation = messages.stream()
                .map(msg -> InterviewDtos.ConversationMessage.builder()
                        .role(msg.getRole().name())
                        .content(msg.getContent())
                        .build())
                .collect(Collectors.toList());
        
        return InterviewDtos.InterviewDetailResponse.builder()
                .id(session.getId())
                .position(session.getPosition())
                .jobDescription(session.getJobDescription())
                .status(session.getStatus())
                .startTime(session.getCreatedAt() != null ? 
                        session.getCreatedAt().toDate().toString() : null)
                .conversation(conversation)
                .build();
    }

    /**
     * Завершить интервью (изменить статус на COMPLETED)
     */
    public void completeInterview(String interviewId) 
            throws ExecutionException, InterruptedException {
        log.info("Completing interview: {}", interviewId);
        
        User currentUser = getCurrentUser();
        
        // Get interview session
        Firestore dbFirestore = FirestoreClient.getFirestore();
        InterviewSession session = dbFirestore.collection("interview_sessions")
                .document(interviewId)
                .get()
                .get()
                .toObject(InterviewSession.class);
        
        if (session == null) {
            throw new RuntimeException("Interview not found");
        }
        
        if (!session.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to interview");
        }
        
        session.setStatus(InterviewSession.InterviewStatus.COMPLETED);
        sessionRepository.save(session);
        
        log.info("Interview {} marked as completed", interviewId);
    }

    private User getCurrentUser() throws ExecutionException, InterruptedException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting current user: {}", userEmail);
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userEmail));
    }
}