package com.zharkyn.aiassistant_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceInterviewAnswerResponse {
    private boolean success;
    private String message;
    private String aiResponse;
    private String aiMessageId;
}