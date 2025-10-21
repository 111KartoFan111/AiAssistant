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
    private Timestamp createdAt; // Изменено с LocalDateTime на Timestamp

    public enum InterviewStatus {
        IN_PROGRESS,
        COMPLETED
    }
}