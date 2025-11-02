package com.zharkyn.aiassistant_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO классы для аналитики и отчетов
 */
public class AnalyticsDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerEvaluation {
        private Integer score; // 0-10
        private String feedback;
        private List<String> strengths;
        private List<String> improvements;
        private String questionType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewReport {
        private String interviewId;
        private Double overallScore; // Средний балл
        private Integer totalQuestions;
        private Map<String, SkillAnalysis> skillsAnalysis; // Анализ по навыкам
        private List<String> strengths; // Топ-5 сильных сторон
        private List<String> weaknesses; // Топ-5 слабых сторон
        private List<String> recommendations; // Рекомендации
        private List<QuestionEvaluation> questionEvaluations; // Оценки по вопросам
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillAnalysis {
        private String skillName;
        private Double averageScore;
        private Integer questionsCount;
        private String performance; // Excellent, Good, Fair, Needs Improvement
        private List<String> keyPoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionEvaluation {
        private Integer questionNumber;
        private String questionType;
        private Integer score;
        private String feedback;
        private List<String> strengths;
        private List<String> improvements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressAnalytics {
        private Integer totalInterviews;
        private Double averageScore;
        private Double scoreImprovement; // Процент улучшения
        private Map<String, SkillProgress> skillsProgress;
        private List<InterviewSummary> recentInterviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillProgress {
        private String skillName;
        private Double currentScore;
        private Double previousScore;
        private Double improvement;
        private String trend; // Improving, Stable, Declining
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewSummary {
        private String interviewId;
        private String position;
        private String date;
        private Double score;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportRequest {
        private String format; // PDF or CSV
        private List<String> interviewIds; // Список интервью для экспорта
    }
}