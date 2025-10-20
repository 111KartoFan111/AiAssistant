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
    public static class InterviewSetupRequest {
        @NotEmpty
        private String position;
        private String jobDescription;
    }

    @Data
    @Builder
    public static class StartInterviewResponse {
        private Long sessionId;
        private ChatMessageResponse firstMessage;
    }

    @Data
    @Builder
    public static class UserMessageRequest {
        @NotEmpty
        private String content;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessageResponse {
        private ChatMessage.MessageRole role;
        private String content;
    }
}
