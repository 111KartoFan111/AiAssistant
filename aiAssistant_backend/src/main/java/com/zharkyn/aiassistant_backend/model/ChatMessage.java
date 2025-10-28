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
    // ✅ ИСПРАВЛЕНИЕ: Изменено с LocalDateTime на Long для совместимости с Firestore
    private Long timestamp; // Unix timestamp в миллисекундах

    public enum MessageRole {
        USER,
        MODEL
    }
}