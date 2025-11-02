package com.zharkyn.aiassistant_backend.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.dto.VoiceInterviewDtos;
import com.zharkyn.aiassistant_backend.model.InterviewSession;
import com.zharkyn.aiassistant_backend.model.User;
import com.zharkyn.aiassistant_backend.model.VoiceMessage;
import com.zharkyn.aiassistant_backend.repository.InterviewSessionRepository;
import com.zharkyn.aiassistant_backend.repository.UserRepository;
import com.zharkyn.aiassistant_backend.repository.VoiceMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Упрощенный сервис для голосового интервью с OpenAI Realtime API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceInterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final VoiceMessageRepository voiceMessageRepository;
    private final UserRepository userRepository;
    private final OpenAIRealtimeService realtimeService;
    
    // Храним активные WebSocket сессии
    private final Map<String, OpenAIRealtimeService.RealtimeSession> activeSessions = new ConcurrentHashMap<>();

    private static final int TOTAL_QUESTIONS = 20;

    /**
     * Начать новое голосовое интервью
     */
    public VoiceInterviewDtos.StartVoiceInterviewResponse startVoiceInterview(
            VoiceInterviewDtos.VoiceInterviewSetupRequest request) 
            throws ExecutionException, InterruptedException {
        
        log.info("Starting voice interview for position: {}", request.getPosition());
        
        User currentUser = getCurrentUser();
        
        // Создаем сессию интервью в Firestore
        InterviewSession session = InterviewSession.builder()
                .userId(currentUser.getId())
                .position(request.getPosition())
                .jobDescription(request.getJobDescription() != null ? request.getJobDescription() : "")
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .company(request.getCompany() != null ? request.getCompany() : "")
                .status(InterviewSession.InterviewStatus.IN_PROGRESS)
                .createdAt(Timestamp.now())
                .currentQuestionNumber(1)
                .totalQuestions(TOTAL_QUESTIONS)
                .build();

        session = sessionRepository.save(session);
        log.info("Interview session created with ID: {}", session.getId());

        // Инициализируем OpenAI Realtime сессию
        OpenAIRealtimeService.RealtimeSession realtimeSession = realtimeService.initializeSession(
            request.getPosition(),
            request.getJobDescription() != null ? request.getJobDescription() : "",
            request.getLanguage() != null ? request.getLanguage() : "en",
            1 // Первый вопрос
        );
        
        // Сохраняем активную сессию
        activeSessions.put(session.getId(), realtimeSession);
        
        log.info("OpenAI Realtime session initialized");

        return VoiceInterviewDtos.StartVoiceInterviewResponse.builder()
                .interviewId(session.getId())
                .firstQuestionText("Voice interview ready. Please start speaking when ready.")
                .firstQuestionAudioBase64("") // Realtime API будет стримить аудио через WebSocket
                .questionType("BACKGROUND")
                .currentQuestionNumber(1)
                .totalQuestions(TOTAL_QUESTIONS)
                .audioFormat("audio/pcm16")
                .build();
    }

    /**
     * Получить WebSocket URL для клиента
     */
    public String getRealtimeWebSocketUrl(String interviewId) {
        OpenAIRealtimeService.RealtimeSession session = activeSessions.get(interviewId);
        if (session == null) {
            throw new RuntimeException("No active realtime session found");
        }
        
        // Возвращаем информацию для подключения клиента
        return "wss://api.openai.com/v1/realtime?model=gpt-4o-realtime-preview-2024-10-01";
    }

    /**
     * Обработать голосовой ответ (для fallback если не используется прямое WebSocket подключение)
     */
    public VoiceInterviewDtos.VoiceResponseDto processAudioAnswer(
            String sessionId, MultipartFile audioFile) 
            throws Exception {
        
        log.info("Processing audio answer for session: {}", sessionId);
        
        OpenAIRealtimeService.RealtimeSession realtimeSession = activeSessions.get(sessionId);
        if (realtimeSession == null) {
            throw new RuntimeException("No active realtime session found");
        }
        
        // Отправляем аудио в OpenAI Realtime API
        byte[] audioData = audioFile.getBytes();
        realtimeService.sendAudio(realtimeSession.getSession(), audioData);
        realtimeService.commitAudio(realtimeSession.getSession());
        
        // Запрашиваем ответ от AI
        realtimeService.createResponse(realtimeSession.getSession());
        
        // Ждем ответ (в реальности это должно быть асинхронно через WebSocket)
        Thread.sleep(2000); // Простая задержка для примера
        
        String transcript = realtimeSession.getHandler().getLastTranscript();
        String audioResponse = realtimeSession.getHandler().getLastAudioResponse();
        
        // Получаем сессию интервью
        Firestore dbFirestore = FirestoreClient.getFirestore();
        InterviewSession session = dbFirestore.collection("interview_sessions")
                .document(sessionId)
                .get()
                .get()
                .toObject(InterviewSession.class);

        if (session == null) {
            throw new RuntimeException("Interview session not found");
        }

        // Сохраняем сообщение пользователя
        VoiceMessage userMessage = VoiceMessage.builder()
                .sessionId(sessionId)
                .role(VoiceMessage.MessageRole.USER)
                .textContent(transcript)
                .timestamp(System.currentTimeMillis())
                .build();
        voiceMessageRepository.save(userMessage);

        // Увеличиваем номер вопроса
        int nextQuestionNumber = session.getCurrentQuestionNumber() + 1;

        // Проверяем завершение интервью
        if (nextQuestionNumber > TOTAL_QUESTIONS) {
            log.info("Voice interview completed");
            session.setStatus(InterviewSession.InterviewStatus.COMPLETED);
            sessionRepository.save(session);
            
            // Закрываем Realtime сессию
            realtimeSession.close();
            activeSessions.remove(sessionId);
            
            return VoiceInterviewDtos.VoiceResponseDto.builder()
                    .transcribedText(transcript)
                    .nextQuestionText("Thank you for completing the interview!")
                    .nextQuestionAudioBase64("")
                    .isInterviewComplete(true)
                    .currentQuestionNumber(nextQuestionNumber)
                    .totalQuestions(TOTAL_QUESTIONS)
                    .audioFormat("audio/pcm16")
                    .build();
        }

        // Сохраняем ответ AI
        VoiceMessage aiMessage = VoiceMessage.builder()
                .sessionId(sessionId)
                .role(VoiceMessage.MessageRole.MODEL)
                .textContent("AI response") // Текст будет получен через WebSocket
                .audioBase64(audioResponse)
                .questionNumber(nextQuestionNumber)
                .timestamp(System.currentTimeMillis())
                .build();
        voiceMessageRepository.save(aiMessage);

        // Обновляем сессию
        session.setCurrentQuestionNumber(nextQuestionNumber);
        sessionRepository.save(session);

        return VoiceInterviewDtos.VoiceResponseDto.builder()
                .transcribedText(transcript)
                .nextQuestionText("AI is speaking...")
                .nextQuestionAudioBase64(audioResponse)
                .questionType(getQuestionType(nextQuestionNumber))
                .currentQuestionNumber(nextQuestionNumber)
                .totalQuestions(TOTAL_QUESTIONS)
                .isInterviewComplete(false)
                .audioFormat("audio/pcm16")
                .build();
    }

    /**
     * Получить историю голосовых интервью
     */
    public List<VoiceInterviewDtos.VoiceInterviewHistoryResponse> getVoiceInterviewHistory() 
            throws ExecutionException, InterruptedException {
        
        User currentUser = getCurrentUser();
        
        Firestore dbFirestore = FirestoreClient.getFirestore();
        List<InterviewSession> sessions = dbFirestore.collection("interview_sessions")
                .whereEqualTo("userId", currentUser.getId())
                .get()
                .get()
                .toObjects(InterviewSession.class);
        
        return sessions.stream()
                .map(session -> VoiceInterviewDtos.VoiceInterviewHistoryResponse.builder()
                        .id(session.getId())
                        .position(session.getPosition())
                        .jobDescription(session.getJobDescription())
                        .status(session.getStatus())
                        .language(session.getLanguage())
                        .company(session.getCompany())
                        .startTime(session.getCreatedAt() != null ? 
                                session.getCreatedAt().toDate().toString() : null)
                        .questionsAnswered(session.getCurrentQuestionNumber())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Получить детали интервью
     */
    public VoiceInterviewDtos.VoiceInterviewDetailResponse getVoiceInterviewDetails(String interviewId) 
            throws ExecutionException, InterruptedException {
        
        User currentUser = getCurrentUser();
        
        Firestore dbFirestore = FirestoreClient.getFirestore();
        InterviewSession session = dbFirestore.collection("interview_sessions")
                .document(interviewId)
                .get()
                .get()
                .toObject(InterviewSession.class);
        
        if (session == null || !session.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Interview not found or unauthorized");
        }
        
        List<VoiceMessage> messages = voiceMessageRepository.findBySessionIdOrderByTimestampAsc(interviewId);
        
        List<VoiceInterviewDtos.VoiceConversationMessage> conversation = messages.stream()
                .map(msg -> VoiceInterviewDtos.VoiceConversationMessage.builder()
                        .role(msg.getRole().name())
                        .textContent(msg.getTextContent())
                        .audioBase64(msg.getAudioBase64())
                        .questionType(msg.getQuestionType() != null ? msg.getQuestionType().name() : null)
                        .timestamp(msg.getTimestamp())
                        .build())
                .collect(Collectors.toList());
        
        return VoiceInterviewDtos.VoiceInterviewDetailResponse.builder()
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
     * Завершить голосовое интервью
     */
    public void completeVoiceInterview(String interviewId) 
            throws Exception {
        
        User currentUser = getCurrentUser();
        
        Firestore dbFirestore = FirestoreClient.getFirestore();
        InterviewSession session = dbFirestore.collection("interview_sessions")
                .document(interviewId)
                .get()
                .get()
                .toObject(InterviewSession.class);
        
        if (session == null || !session.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Interview not found or unauthorized");
        }
        
        session.setStatus(InterviewSession.InterviewStatus.COMPLETED);
        sessionRepository.save(session);
        
        // Закрываем Realtime сессию если она активна
        OpenAIRealtimeService.RealtimeSession realtimeSession = activeSessions.remove(interviewId);
        if (realtimeSession != null) {
            realtimeSession.close();
        }
    }

    public VoiceInterviewDtos.AudioQuestionResponse getQuestionAudio(String interviewId, Integer questionNumber) 
            throws ExecutionException, InterruptedException {
        List<VoiceMessage> messages = voiceMessageRepository.findBySessionIdOrderByTimestampAsc(interviewId);
        
        VoiceMessage question = messages.stream()
                .filter(msg -> msg.getRole() == VoiceMessage.MessageRole.MODEL && 
                              msg.getQuestionNumber() != null &&
                              msg.getQuestionNumber().equals(questionNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Question not found"));

        return VoiceInterviewDtos.AudioQuestionResponse.builder()
                .questionText(question.getTextContent())
                .audioBase64(question.getAudioBase64())
                .questionType(question.getQuestionType() != null ? question.getQuestionType().name() : null)
                .questionNumber(questionNumber)
                .audioFormat("audio/pcm16")
                .build();
    }

    // === Helper methods ===

    private User getCurrentUser() throws ExecutionException, InterruptedException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }

    private String getQuestionType(int questionNumber) {
        if (questionNumber <= 5) return "BACKGROUND";
        if (questionNumber <= 13) return "SITUATIONAL";
        return "TECHNICAL";
    }
}