package com.zharkyn.aiassistant_backend.dto;

import com.zharkyn.aiassistant_backend.model.ChatMessage;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class InterviewDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewSetupRequest {
        @NotEmpty
        private String position;
        private String jobDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartInterviewResponse {
        private String interviewId;  // Changed from sessionId to match frontend
        private String firstQuestion;  // Changed from firstMessage to match frontend
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMessageRequest {
        @NotEmpty
        private String answer;  // Changed from content to match frontend
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessageResponse {
        private ChatMessage.MessageRole role;
        private String content;
        private String nextQuestion;  // Added to match frontend expectations
    }
}