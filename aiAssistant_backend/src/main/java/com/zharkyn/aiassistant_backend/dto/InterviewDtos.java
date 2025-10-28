package com.zharkyn.aiassistant_backend.dto;

import com.zharkyn.aiassistant_backend.model.ChatMessage;
import com.zharkyn.aiassistant_backend.model.InterviewSession;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
        private String interviewId;
        private String firstQuestion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserMessageRequest {
        @NotEmpty
        private String answer;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessageResponse {
        private ChatMessage.MessageRole role;
        private String content;
        private String nextQuestion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewHistoryResponse {
        private String id;
        private String position;
        private String jobDescription;
        private InterviewSession.InterviewStatus status;
        private String startTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewDetailResponse {
        private String id;
        private String position;
        private String jobDescription;
        private InterviewSession.InterviewStatus status;
        private String startTime;
        private List<ConversationMessage> conversation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationMessage {
        private String role;
        private String content;
    }
}