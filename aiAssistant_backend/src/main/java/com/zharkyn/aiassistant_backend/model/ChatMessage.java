package com.zharkyn.aiassistant_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String id;
    private String sessionId;
    private MessageRole role;
    private String content;
    private Long timestamp; // Unix timestamp в миллисекундах
    private QuestionType questionType; // BACKGROUND, SITUATIONAL, TECHNICAL
    private Integer questionNumber; // 1-20

    public enum MessageRole {
        USER,
        MODEL
    }

    public enum QuestionType {
        BACKGROUND,    // Фоновые вопросы: расскажите о себе, опыт работы
        SITUATIONAL,   // Ситуационные: как бы вы поступили в ситуации X
        TECHNICAL      // Технические: специфические знания и навыки
    }
}