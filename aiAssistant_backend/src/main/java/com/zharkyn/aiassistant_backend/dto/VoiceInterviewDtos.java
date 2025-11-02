package com.zharkyn.aiassistant_backend.dto;

import com.zharkyn.aiassistant_backend.model.ChatMessage;
import com.zharkyn.aiassistant_backend.model.InterviewSession;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO классы для голосового интервью
 */
public class VoiceInterviewDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewSetupRequest {
        @NotEmpty
        private String position;
        private String jobDescription;
        private String language; // en, ru, kz
        private String company; // kaspi, halyk, etc.
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartVoiceInterviewResponse {
        private String interviewId;
        private String firstQuestionText;
        private String firstQuestionAudioBase64; // Аудио вопроса в base64
        private String questionType; // BACKGROUND, SITUATIONAL, TECHNICAL
        private Integer currentQuestionNumber; // 1
        private Integer totalQuestions; // 20
        private String audioFormat; // "audio/mp3" или "audio/wav"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceResponseDto {
        private String transcribedText; // Распознанный текст из аудио
        private String nextQuestionText; // Текст следующего вопроса
        private String nextQuestionAudioBase64; // Аудио следующего вопроса в base64
        private String questionType; // BACKGROUND, SITUATIONAL, TECHNICAL
        private Integer currentQuestionNumber;
        private Integer totalQuestions;
        private Boolean isInterviewComplete; // true если достигли 20 вопросов
        private String audioFormat; // "audio/mp3"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioQuestionResponse {
        private String questionText;
        private String audioBase64;
        private String questionType;
        private Integer questionNumber;
        private String audioFormat;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewHistoryResponse {
        private String id;
        private String position;
        private String jobDescription;
        private InterviewSession.InterviewStatus status;
        private String startTime;
        private String language;
        private String company;
        private Integer questionsAnswered; // Сколько вопросов было отвечено
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceInterviewDetailResponse {
        private String id;
        private String position;
        private String jobDescription;
        private InterviewSession.InterviewStatus status;
        private String startTime;
        private String language;
        private String company;
        private List<VoiceConversationMessage> conversation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceConversationMessage {
        private String role; // USER or MODEL
        private String textContent; // Текстовое содержание
        private String audioBase64; // Аудио контент для MODEL сообщений
        private String questionType; // для MODEL сообщений
        private Long timestamp;
    }
}