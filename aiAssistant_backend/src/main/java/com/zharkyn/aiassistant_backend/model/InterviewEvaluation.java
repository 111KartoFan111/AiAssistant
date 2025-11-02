package com.zharkyn.aiassistant_backend.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Модель для хранения оценок ответов на интервью
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewEvaluation {

    private String id;
    private String interviewId;
    private Integer questionNumber;
    private ChatMessage.QuestionType questionType;
    private Integer score; // 0-10
    private String feedback;
    private List<String> strengths;
    private List<String> improvements;
    private Timestamp createdAt;
}