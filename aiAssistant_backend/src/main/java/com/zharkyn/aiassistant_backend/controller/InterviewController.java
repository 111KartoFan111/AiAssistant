package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.InterviewDtos;
import com.zharkyn.aiassistant_backend.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {
    
    private final InterviewService interviewService;

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
}