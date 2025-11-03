package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.VoiceInterviewDtos;
import com.zharkyn.aiassistant_backend.dto.VoiceInterviewAnswerResponse;
import com.zharkyn.aiassistant_backend.service.VoiceInterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/voice-interviews")
@CrossOrigin(origins = "*")
@Slf4j
public class VoiceInterviewController {

    private final VoiceInterviewService voiceInterviewService;

    @Autowired
    public VoiceInterviewController(VoiceInterviewService voiceInterviewService) {
        this.voiceInterviewService = voiceInterviewService;
    }

    @PostMapping("/start")
    public ResponseEntity<VoiceInterviewDtos.StartVoiceInterviewResponse> startVoiceInterview(
            @RequestBody VoiceInterviewDtos.VoiceInterviewSetupRequest request) {
        try {
            log.info("Starting voice interview for position: {}", request.getPositionTitle());
            VoiceInterviewDtos.VoiceInterviewResponse response = voiceInterviewService.startVoiceInterview(request);
            
            return ResponseEntity.ok(new VoiceInterviewDtos.StartVoiceInterviewResponse(
                    response.isSuccess(),
                    response.getMessage(),
                    response.getSessionId()
            ));
        } catch (Exception e) {
            log.error("Error starting voice interview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VoiceInterviewDtos.StartVoiceInterviewResponse(
                            false,
                            "Error: " + e.getMessage(),
                            null
                    ));
        }
    }

    @PostMapping("/{sessionId}/answer-audio")
    public ResponseEntity<VoiceInterviewDtos.VoiceResponseDto> submitAudioAnswer(
            @PathVariable String sessionId,
            @RequestParam("audio") MultipartFile audioFile) {
        try {
            log.info("Submitting audio answer for interview: {}, file size: {} bytes", 
                    sessionId, audioFile.getSize());
            
            VoiceInterviewAnswerResponse response = voiceInterviewService.processAudioAnswer(sessionId, audioFile);
            
            return ResponseEntity.ok(new VoiceInterviewDtos.VoiceResponseDto(
                    response.isSuccess(),
                    response.getMessage(),
                    response.getAiResponse(),
                    response.getAiMessageId()
            ));
        } catch (IOException | ExecutionException | InterruptedException e) {
            log.error("Error processing audio answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VoiceInterviewDtos.VoiceResponseDto(
                            false,
                            "Error: " + e.getMessage(),
                            null,
                            null
                    ));
        }
    }

    @GetMapping("/{sessionId}/question/{questionNumber}/audio")
    public ResponseEntity<VoiceInterviewDtos.AudioQuestionResponse> getQuestionAudio(
            @PathVariable String sessionId,
            @PathVariable Integer questionNumber) {
        try {
            log.info("Getting question audio for session: {}, question: {}", sessionId, questionNumber);
            VoiceInterviewDtos.AudioResponse response = voiceInterviewService.getQuestionAudio(sessionId, questionNumber);
            
            return ResponseEntity.ok(new VoiceInterviewDtos.AudioQuestionResponse(
                    response.isSuccess(),
                    response.getMessage(),
                    response.getAudioUrl()
            ));
        } catch (Exception e) {
            log.error("Error getting question audio", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VoiceInterviewDtos.AudioQuestionResponse(
                            false,
                            "Error: " + e.getMessage(),
                            null
                    ));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<VoiceInterviewDtos.VoiceInterviewHistoryResponse> getVoiceInterviewHistory() {
        try {
            log.info("Fetching voice interview history");
            VoiceInterviewDtos.VoiceInterviewHistoryResponse response = voiceInterviewService.getVoiceInterviewHistory();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching voice interview history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VoiceInterviewDtos.VoiceInterviewHistoryResponse(
                            false,
                            "Error: " + e.getMessage(),
                            null
                    ));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<VoiceInterviewDtos.VoiceInterviewDetailResponse> getVoiceInterviewDetails(
            @PathVariable String sessionId) {
        try {
            log.info("Fetching voice interview details for: {}", sessionId);
            VoiceInterviewDtos.VoiceInterviewDetailsResponse response = voiceInterviewService.getVoiceInterviewDetails(sessionId);
            
            return ResponseEntity.ok(new VoiceInterviewDtos.VoiceInterviewDetailResponse(
                    response.isSuccess(),
                    response.getMessage(),
                    response.getInterview(),
                    response.getMessages()
            ));
        } catch (Exception e) {
            log.error("Error fetching voice interview details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VoiceInterviewDtos.VoiceInterviewDetailResponse(
                            false,
                            "Error: " + e.getMessage(),
                            null,
                            null
                    ));
        }
    }

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<VoiceInterviewDtos.VoiceInterviewResponse> completeVoiceInterview(
            @PathVariable String sessionId) {
        try {
            log.info("Completing voice interview: {}", sessionId);
            VoiceInterviewDtos.VoiceInterviewResponse response = voiceInterviewService.completeVoiceInterview(sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error completing voice interview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new VoiceInterviewDtos.VoiceInterviewResponse(
                            false,
                            "Error: " + e.getMessage(),
                            null
                    ));
        }
    }
}