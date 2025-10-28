package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.InterviewDtos;
import com.zharkyn.aiassistant_backend.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {
    
    private final InterviewService interviewService;

    /**
     * Начать новое интервью
     */
    @PostMapping("/start")
    public ResponseEntity<InterviewDtos.StartInterviewResponse> startInterview(
            @Valid @RequestBody InterviewDtos.InterviewSetupRequest request) {
        try {
            log.info("Starting interview for position: {}", request.getPosition());
            InterviewDtos.StartInterviewResponse response = interviewService.startInterview(request);
            log.info("Interview started successfully with sessionId: {}", response.getInterviewId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting interview", e);
            throw new RuntimeException("Failed to start interview: " + e.getMessage(), e);
        }
    }

    /**
     * Отправить ответ на вопрос интервью
     */
    @PostMapping("/{interviewId}/answer")
    public ResponseEntity<InterviewDtos.ChatMessageResponse> submitAnswer(
            @PathVariable String interviewId,
            @Valid @RequestBody InterviewDtos.UserMessageRequest request) {
        try {
            log.info("Submitting answer for interview: {}", interviewId);
            InterviewDtos.ChatMessageResponse response = interviewService.postMessage(interviewId, request);
            log.info("Answer submitted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error submitting answer", e);
            throw new RuntimeException("Failed to submit answer: " + e.getMessage(), e);
        }
    }

    /**
     * Получить историю всех интервью текущего пользователя
     */
    @GetMapping
    public ResponseEntity<List<InterviewDtos.InterviewHistoryResponse>> getInterviewHistory() {
        try {
            log.info("Fetching interview history");
            List<InterviewDtos.InterviewHistoryResponse> history = interviewService.getInterviewHistory();
            log.info("Retrieved {} interviews", history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching interview history", e);
            throw new RuntimeException("Failed to fetch interview history: " + e.getMessage(), e);
        }
    }

    /**
     * Получить детальную информацию об интервью (включая переписку)
     */
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewDtos.InterviewDetailResponse> getInterviewDetails(
            @PathVariable String interviewId) {
        try {
            log.info("Fetching interview details for: {}", interviewId);
            InterviewDtos.InterviewDetailResponse details = interviewService.getInterviewDetails(interviewId);
            log.info("Interview details retrieved successfully");
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error fetching interview details", e);
            throw new RuntimeException("Failed to fetch interview details: " + e.getMessage(), e);
        }
    }

    /**
     * Завершить интервью
     */
    @PostMapping("/{interviewId}/complete")
    public ResponseEntity<Void> completeInterview(@PathVariable String interviewId) {
        try {
            log.info("Completing interview: {}", interviewId);
            interviewService.completeInterview(interviewId);
            log.info("Interview completed successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error completing interview", e);
            throw new RuntimeException("Failed to complete interview: " + e.getMessage(), e);
        }
    }
}