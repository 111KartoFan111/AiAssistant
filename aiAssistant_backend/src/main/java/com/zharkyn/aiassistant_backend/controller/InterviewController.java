package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.InterviewDtos;
import com.zharkyn.aiassistant_backend.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {
    
    private final InterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<InterviewDtos.StartInterviewResponse> startInterview(@Valid @RequestBody InterviewDtos.InterviewSetupRequest request) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(interviewService.startInterview(request));
    }

    @PostMapping("/{sessionId}/message")
    public ResponseEntity<InterviewDtos.ChatMessageResponse> postMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody InterviewDtos.UserMessageRequest request) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(interviewService.postMessage(sessionId, request));
    }
}

