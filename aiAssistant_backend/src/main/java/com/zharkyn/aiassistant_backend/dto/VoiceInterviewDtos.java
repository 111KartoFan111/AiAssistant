package com.zharkyn.aiassistant_backend.dto;

import com.zharkyn.aiassistant_backend.model.VoiceInterview;
import com.zharkyn.aiassistant_backend.model.VoiceMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class VoiceInterviewDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewSetupRequest {
        private String positionTitle;
        private String companyName;
        private String jobDescription;
        private Integer difficulty;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewResponse {
        private boolean success;
        private String message;
        private String sessionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartVoiceInterviewResponse {
        private boolean success;
        private String message;
        private String sessionId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceResponseDto {
        private boolean success;
        private String message;
        private String aiResponse;
        private String aiMessageId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioResponse {
        private boolean success;
        private String message;
        private String audioUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioQuestionResponse {
        private boolean success;
        private String message;
        private String audioUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewHistoryResponse {
        private boolean success;
        private String message;
        private List<VoiceInterview> interviews;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewDetailsResponse {
        private boolean success;
        private String message;
        private VoiceInterview interview;
        private List<VoiceMessage> messages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewDetailResponse {
        private boolean success;
        private String message;
        private VoiceInterview interview;
        private List<VoiceMessage> messages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewAnswerRequest {
        private String answer;
        private Integer questionNumber;
    }
}