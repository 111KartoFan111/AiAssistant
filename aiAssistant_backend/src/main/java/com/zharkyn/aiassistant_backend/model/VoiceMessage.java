package com.zharkyn.aiassistant_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель для хранения голосовых сообщений в интервью
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceMessage {

    private String id;
    private String sessionId;
    private MessageRole role;
    private String textContent; // Текстовое содержание (транскрипция или оригинальный вопрос)
    private String audioBase64; // Аудио контент в base64 (только для MODEL сообщений)
    private Long timestamp; // Unix timestamp в миллисекундах
    private ChatMessage.QuestionType questionType; // BACKGROUND, SITUATIONAL, TECHNICAL (только для MODEL)
    private Integer questionNumber; // 1-20 (только для MODEL сообщений)

    public enum MessageRole {
        USER,   // Сообщение от пользователя (голосовой ответ -> транскрипция)
        MODEL   // Сообщение от AI (вопрос с аудио)
    }
}