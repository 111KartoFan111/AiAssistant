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
        private String language; // en, ru, kz
        private String company; // Добавлено для выбора компании
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartInterviewResponse {
        private String interviewId;
        private String firstQuestion;
        private String questionType; // BACKGROUND, SITUATIONAL, TECHNICAL
        private Integer currentQuestionNumber; // 1
        private Integer totalQuestions; // 20
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
        private String questionType; // BACKGROUND, SITUATIONAL, TECHNICAL
        private Integer currentQuestionNumber;
        private Integer totalQuestions;
        private Boolean isInterviewComplete; // true если достигли 20 вопросов
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
        private String language;
        private String company;
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
        private String language;
        private String company;
        private List<ConversationMessage> conversation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationMessage {
        private String role;
        private String content;
        private String questionType; // для AI сообщений
    }
}