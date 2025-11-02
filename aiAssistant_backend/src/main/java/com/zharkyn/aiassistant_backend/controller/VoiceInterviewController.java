package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.VoiceInterviewDtos;
import com.zharkyn.aiassistant_backend.service.VoiceInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST контроллер для управления голосовыми интервью
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/voice-interviews")
@RequiredArgsConstructor
public class VoiceInterviewController {
    
    private final VoiceInterviewService voiceInterviewService;

    /**
     * Начать новое голосовое интервью
     */
    @PostMapping("/start")
    public ResponseEntity<VoiceInterviewDtos.StartVoiceInterviewResponse> startVoiceInterview(
            @Valid @RequestBody VoiceInterviewDtos.VoiceInterviewSetupRequest request) {
        try {
            log.info("Starting voice interview for position: {}", request.getPosition());
            VoiceInterviewDtos.StartVoiceInterviewResponse response = 
                voiceInterviewService.startVoiceInterview(request);
            log.info("Voice interview started successfully with sessionId: {}", response.getInterviewId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting voice interview", e);
            throw new RuntimeException("Failed to start voice interview: " + e.getMessage(), e);
        }
    }

    /**
     * Отправить голосовой ответ (аудио файл) на вопрос интервью
     */
    @PostMapping(value = "/{interviewId}/answer-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceInterviewDtos.VoiceResponseDto> submitAudioAnswer(
            @PathVariable String interviewId,
            @RequestParam("audio") MultipartFile audioFile) {
        try {
            log.info("Submitting audio answer for interview: {}, file size: {} bytes", 
                interviewId, audioFile.getSize());
            
            VoiceInterviewDtos.VoiceResponseDto response = 
                voiceInterviewService.processAudioAnswer(interviewId, audioFile);
            
            log.info("Audio answer processed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing audio answer", e);
            throw new RuntimeException("Failed to process audio answer: " + e.getMessage(), e);
        }
    }

    /**
     * Получить аудио вопрос в формате base64
     */
    @GetMapping("/{interviewId}/question-audio/{questionNumber}")
    public ResponseEntity<VoiceInterviewDtos.AudioQuestionResponse> getQuestionAudio(
            @PathVariable String interviewId,
            @PathVariable Integer questionNumber) {
        try {
            log.info("Getting audio for question {} in interview {}", questionNumber, interviewId);
            VoiceInterviewDtos.AudioQuestionResponse response = 
                voiceInterviewService.getQuestionAudio(interviewId, questionNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting question audio", e);
            throw new RuntimeException("Failed to get question audio: " + e.getMessage(), e);
        }
    }

    /**
     * Получить историю всех голосовых интервью текущего пользователя
     */
    @GetMapping
    public ResponseEntity<List<VoiceInterviewDtos.VoiceInterviewHistoryResponse>> getVoiceInterviewHistory() {
        try {
            log.info("Fetching voice interview history");
            List<VoiceInterviewDtos.VoiceInterviewHistoryResponse> history = 
                voiceInterviewService.getVoiceInterviewHistory();
            log.info("Retrieved {} voice interviews", history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching voice interview history", e);
            throw new RuntimeException("Failed to fetch voice interview history: " + e.getMessage(), e);
        }
    }

    /**
     * Получить детальную информацию о голосовом интервью
     */
    @GetMapping("/{interviewId}")
    public ResponseEntity<VoiceInterviewDtos.VoiceInterviewDetailResponse> getVoiceInterviewDetails(
            @PathVariable String interviewId) {
        try {
            log.info("Fetching voice interview details for: {}", interviewId);
            VoiceInterviewDtos.VoiceInterviewDetailResponse details = 
                voiceInterviewService.getVoiceInterviewDetails(interviewId);
            log.info("Voice interview details retrieved successfully");
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error fetching voice interview details", e);
            throw new RuntimeException("Failed to fetch voice interview details: " + e.getMessage(), e);
        }
    }

    /**
     * Завершить голосовое интервью
     */
    @PostMapping("/{interviewId}/complete")
    public ResponseEntity<Void> completeVoiceInterview(@PathVariable String interviewId) {
        try {
            log.info("Completing voice interview: {}", interviewId);
            voiceInterviewService.completeVoiceInterview(interviewId);
            log.info("Voice interview completed successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error completing voice interview", e);
            throw new RuntimeException("Failed to complete voice interview: " + e.getMessage(), e);
        }
    }
}