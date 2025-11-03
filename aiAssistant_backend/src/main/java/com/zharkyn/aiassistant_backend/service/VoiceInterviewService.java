package com.zharkyn.aiassistant_backend.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.zharkyn.aiassistant_backend.dto.VoiceInterviewAnswerResponse;
import com.zharkyn.aiassistant_backend.dto.VoiceInterviewDtos;
import com.zharkyn.aiassistant_backend.model.VoiceInterview;
import com.zharkyn.aiassistant_backend.model.VoiceMessage;
import com.zharkyn.aiassistant_backend.repository.VoiceInterviewRepository;
import com.zharkyn.aiassistant_backend.repository.VoiceMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class VoiceInterviewService {
    
    private final OpenAIRealtimeService openAIService;
    private final VoiceInterviewRepository voiceInterviewRepository;
    private final VoiceMessageRepository voiceMessageRepository;

    @Autowired
    public VoiceInterviewService(OpenAIRealtimeService openAIService,
                                  VoiceInterviewRepository voiceInterviewRepository,
                                  VoiceMessageRepository voiceMessageRepository) {
        this.openAIService = openAIService;
        this.voiceInterviewRepository = voiceInterviewRepository;
        this.voiceMessageRepository = voiceMessageRepository;
    }

    public VoiceInterviewDtos.VoiceInterviewResponse startVoiceInterview(
            VoiceInterviewDtos.VoiceInterviewSetupRequest request) 
            throws ExecutionException, InterruptedException {
        
        String userId = getCurrentUserId();
        
        VoiceInterview interview = new VoiceInterview();
        interview.setUserId(userId);
        interview.setPositionTitle(request.getPositionTitle());
        interview.setCompanyName(request.getCompanyName());
        interview.setStatus("active");
        
        String interviewId = voiceInterviewRepository.create(interview);
        
        log.info("Voice interview started: {}", interviewId);
        
        return new VoiceInterviewDtos.VoiceInterviewResponse(
                true,
                "Voice interview started",
                interviewId
        );
    }

    public VoiceInterviewAnswerResponse processAudioAnswer(String sessionId, MultipartFile audioFile) 
            throws IOException, ExecutionException, InterruptedException {
        
        String userId = getCurrentUserId();
        return submitAudioAnswer(sessionId, audioFile, userId);
    }

    public VoiceInterviewAnswerResponse submitAudioAnswer(String sessionId, MultipartFile audioFile, String userId) 
            throws IOException, ExecutionException, InterruptedException {
        
        log.info("Processing audio answer for session: {}", sessionId);

        VoiceInterview interview = voiceInterviewRepository.getById(sessionId);
        if (interview == null) {
            log.error("Interview not found: {}", sessionId);
            throw new IllegalArgumentException("Interview not found: " + sessionId);
        }

        if (!interview.getUserId().equals(userId)) {
            log.error("Unauthorized access attempt to interview {} by user {}", sessionId, userId);
            throw new IllegalArgumentException("Unauthorized access to interview");
        }

        byte[] audioBytes = audioFile.getBytes();
        log.info("Audio file received: size = {} bytes, content type = {}", 
                 audioBytes.length, audioFile.getContentType());

        String audioFileName = "voice_answers/" + sessionId + "/" + System.currentTimeMillis() + ".webm";
        Bucket bucket = StorageClient.getInstance().bucket();
        
        Blob blob = bucket.create(audioFileName, audioBytes, "audio/webm");
        String audioUrl = blob.getMediaLink();
        log.info("Audio file saved to Storage: {}", audioFileName);

        VoiceMessage userMessage = new VoiceMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setSender("user");
        userMessage.setAudioUrl(audioUrl);
        String userMessageId = voiceMessageRepository.create(userMessage);
        log.info("User message saved with ID: {}", userMessageId);

        String aiResponse;
        try {
            log.info("Sending audio to OpenAI Realtime API...");
            aiResponse = openAIService.sendAudioAndGetResponse(audioBytes);
            log.info("AI response received: {}", aiResponse);
        } catch (Exception e) {
            log.error("Error getting AI response", e);
            aiResponse = "Извините, произошла ошибка при обработке вашего ответа. Попробуйте еще раз.";
        }

        VoiceMessage aiMessage = new VoiceMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setSender("assistant");
        aiMessage.setTextContent(aiResponse);
        String aiMessageId = voiceMessageRepository.create(aiMessage);
        log.info("AI message saved with ID: {}", aiMessageId);

        return new VoiceInterviewAnswerResponse(
                true,
                "Ответ обработан успешно",
                aiResponse,
                aiMessageId
        );
    }

    public VoiceInterviewDtos.AudioResponse getQuestionAudio(String sessionId, Integer questionNumber) 
            throws ExecutionException, InterruptedException {
        
        log.info("Getting question audio for session: {}, question: {}", sessionId, questionNumber);
        
        return new VoiceInterviewDtos.AudioResponse(
                true,
                "Audio URL",
                "audio_url_placeholder"
        );
    }

    public VoiceInterviewDtos.VoiceInterviewHistoryResponse getVoiceInterviewHistory() 
            throws ExecutionException, InterruptedException {
        
        String userId = getCurrentUserId();
        List<VoiceInterview> interviews = voiceInterviewRepository.getByUserId(userId);
        
        log.info("Found {} voice interviews for user {}", interviews.size(), userId);
        
        return new VoiceInterviewDtos.VoiceInterviewHistoryResponse(
                true,
                "History retrieved",
                interviews
        );
    }

    public VoiceInterviewDtos.VoiceInterviewDetailsResponse getVoiceInterviewDetails(String sessionId) 
            throws ExecutionException, InterruptedException {
        
        log.info("Fetching voice interview details for: {}", sessionId);
        
        VoiceInterview interview = voiceInterviewRepository.getById(sessionId);
        if (interview == null) {
            throw new IllegalArgumentException("Interview not found: " + sessionId);
        }
        
        List<VoiceMessage> messages = voiceMessageRepository.getBySessionId(sessionId);
        
        log.info("Voice interview details retrieved successfully");
        
        return new VoiceInterviewDtos.VoiceInterviewDetailsResponse(
                true,
                "Details retrieved",
                interview,
                messages
        );
    }

    public VoiceInterviewDtos.VoiceInterviewResponse completeVoiceInterview(String sessionId) 
            throws ExecutionException, InterruptedException {
        
        VoiceInterview interview = voiceInterviewRepository.getById(sessionId);
        if (interview == null) {
            throw new IllegalArgumentException("Interview not found: " + sessionId);
        }
        
        interview.setStatus("completed");
        voiceInterviewRepository.update(sessionId, interview);
        
        log.info("Voice interview completed: {}", sessionId);
        
        return new VoiceInterviewDtos.VoiceInterviewResponse(
                true,
                "Interview completed",
                sessionId
        );
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName();
    }
}