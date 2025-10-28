package com.zharkyn.aiassistant_backend.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {

    private String id;
    private String userId;
    private String position;
    private String jobDescription;
    private InterviewStatus status;
    private Timestamp createdAt;
    private String language; // en, ru, kz
    private String company; // kaspi, jusan, halyk, etc.
    private Integer currentQuestionNumber; // Текущий номер вопроса (1-20)
    private Integer totalQuestions; // Всего вопросов = 20

    public enum InterviewStatus {
        IN_PROGRESS,
        COMPLETED
    }
}