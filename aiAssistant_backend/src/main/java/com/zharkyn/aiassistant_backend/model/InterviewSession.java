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
public class InterviewSession {

    private String id; // Firestore document ID
    private String userId;
    private String position;
    private String jobDescription;
    private InterviewStatus status;
    private LocalDateTime createdAt;

    public enum InterviewStatus {
        IN_PROGRESS,
        COMPLETED
    }
}
