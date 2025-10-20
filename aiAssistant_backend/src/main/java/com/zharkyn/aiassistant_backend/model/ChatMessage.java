package com.zharkyn.aiassistant_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String id;
    private String sessionId;
    private MessageRole role;
    private String content;
    private LocalDateTime timestamp;

    public enum MessageRole {
        USER,
        MODEL
    }
}
