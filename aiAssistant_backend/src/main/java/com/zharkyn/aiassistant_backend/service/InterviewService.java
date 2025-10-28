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

    private static final int TOTAL_QUESTIONS = 20;
    private static final int BACKGROUND_QUESTIONS = 5;  // Вопросы 1-5
    private static final int SITUATIONAL_QUESTIONS = 8; // Вопросы 6-13
    private static final int TECHNICAL_QUESTIONS = 7;   // Вопросы 14-20

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
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .company(request.getCompany())
                .status(InterviewSession.InterviewStatus.IN_PROGRESS)
                .currentQuestionNumber(1)
                .totalQuestions(TOTAL_QUESTIONS)
                .createdAt(Timestamp.now())
                .build();
        session = sessionRepository.save(session);
        log.info("Interview session created with ID: {}", session.getId());

        // Определяем тип первого вопроса
        ChatMessage.QuestionType questionType = determineQuestionType(1);

        // Generate first question
        String firstQuestionText;
        try {
            firstQuestionText = geminiService.generateInitialQuestion(
                request.getPosition(), 
                request.getJobDescription() != null ? request.getJobDescription() : "",
                request.getLanguage() != null ? request.getLanguage() : "en",
                questionType
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
                .questionType(questionType)
                .questionNumber(1)
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(firstMessage);
        log.info("First message saved");

        return InterviewDtos.StartInterviewResponse.builder()
                .interviewId(session.getId())
                .firstQuestion(firstQuestionText)
                .questionType(questionType.name())
                .currentQuestionNumber(1)
                .totalQuestions(TOTAL_QUESTIONS)
                .build();
    }
    
    public InterviewDtos.ChatMessageResponse postMessage(String sessionId, InterviewDtos.UserMessageRequest request) 
            throws ExecutionException, InterruptedException {
        log.info("Processing message for session: {}", sessionId);
        
        // Get session to check current question number
        Firestore dbFirestore = FirestoreClient.getFirestore();
        InterviewSession session = dbFirestore.collection("interview_sessions")
                .document(sessionId)
                .get()
                .get()
                .toObject(InterviewSession.class);

        if (session == null) {
            throw new RuntimeException("Interview session not found");
        }

        // Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.USER)
                .content(request.getAnswer())
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(userMessage);
        log.info("User message saved");

        // Увеличиваем номер вопроса
        int nextQuestionNumber = session.getCurrentQuestionNumber() + 1;

        // Проверяем, не закончилось ли интервью
        if (nextQuestionNumber > TOTAL_QUESTIONS) {
            log.info("Interview completed - reached {} questions", TOTAL_QUESTIONS);
            session.setStatus(InterviewSession.InterviewStatus.COMPLETED);
            sessionRepository.save(session);
            
            return InterviewDtos.ChatMessageResponse.builder()
                    .role(ChatMessage.MessageRole.MODEL)
                    .content("Thank you for completing the interview! You answered all 20 questions.")
                    .isInterviewComplete(true)
                    .currentQuestionNumber(TOTAL_QUESTIONS)
                    .totalQuestions(TOTAL_QUESTIONS)
                    .build();
        }

        // Определяем тип следующего вопроса
        ChatMessage.QuestionType nextQuestionType = determineQuestionType(nextQuestionNumber);

        // Get chat history
        List<ChatMessage> chatHistory = messageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        log.info("Retrieved {} messages from history", chatHistory.size());

        // Generate AI response
        String aiResponseText;
        try {
            aiResponseText = geminiService.generateNextResponse(
                chatHistory, 
                session.getLanguage(),
                nextQuestionType,
                nextQuestionNumber,
                TOTAL_QUESTIONS
            ).block();
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
                .questionType(nextQuestionType)
                .questionNumber(nextQuestionNumber)
                .timestamp(System.currentTimeMillis())
                .build();
        messageRepository.save(aiMessage);
        log.info("AI message saved");

        // Update session with new question number
        session.setCurrentQuestionNumber(nextQuestionNumber);
        sessionRepository.save(session);
        
        return InterviewDtos.ChatMessageResponse.builder()
                .role(aiMessage.getRole())
                .content(aiMessage.getContent())
                .nextQuestion(aiMessage.getContent())
                .questionType(nextQuestionType.name())
                .currentQuestionNumber(nextQuestionNumber)
                .totalQuestions(TOTAL_QUESTIONS)
                .isInterviewComplete(false)
                .build();
    }

    /**
     * Определяет тип вопроса в зависимости от номера
     */
    private ChatMessage.QuestionType determineQuestionType(int questionNumber) {
        if (questionNumber <= BACKGROUND_QUESTIONS) {
            return ChatMessage.QuestionType.BACKGROUND;
        } else if (questionNumber <= BACKGROUND_QUESTIONS + SITUATIONAL_QUESTIONS) {
            return ChatMessage.QuestionType.SITUATIONAL;
        } else {
            return ChatMessage.QuestionType.TECHNICAL;
        }
    }

    /**
     * Получить историю всех интервью текущего пользователя
     */
    public List<InterviewDtos.InterviewHistoryResponse> getInterviewHistory() 
            throws ExecutionException, InterruptedException {
        log.info("Fetching interview history");
        
        User currentUser = getCurrentUser();
        
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("interview_sessions")
                .whereEqualTo("userId", currentUser.getId())
                .get();
        
        List<InterviewSession> sessions = future.get().toObjects(InterviewSession.class);
        log.info("Found {} interview sessions", sessions.size());
        
        return sessions.stream()
                .sorted(Comparator.comparing(InterviewSession::getCreatedAt, 
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(session -> InterviewDtos.InterviewHistoryResponse.builder()
                        .id(session.getId())
                        .position(session.getPosition())
                        .jobDescription(session.getJobDescription())
                        .status(session.getStatus())
                        .language(session.getLanguage())
                        .company(session.getCompany())
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
        
        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByTimestampAsc(interviewId);
        log.info("Found {} messages for interview", messages.size());
        
        List<InterviewDtos.ConversationMessage> conversation = messages.stream()
                .map(msg -> InterviewDtos.ConversationMessage.builder()
                        .role(msg.getRole().name())
                        .content(msg.getContent())
                        .questionType(msg.getQuestionType() != null ? msg.getQuestionType().name() : null)
                        .build())
                .collect(Collectors.toList());
        
        return InterviewDtos.InterviewDetailResponse.builder()
                .id(session.getId())
                .position(session.getPosition())
                .jobDescription(session.getJobDescription())
                .status(session.getStatus())
                .language(session.getLanguage())
                .company(session.getCompany())
                .startTime(session.getCreatedAt() != null ? 
                        session.getCreatedAt().toDate().toString() : null)
                .conversation(conversation)
                .build();
    }

    /**
     * Завершить интервью
     */
    public void completeInterview(String interviewId) 
            throws ExecutionException, InterruptedException {
        log.info("Completing interview: {}", interviewId);
        
        User currentUser = getCurrentUser();
        
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