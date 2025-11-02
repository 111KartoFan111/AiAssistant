package com.zharkyn.aiassistant_backend.service;

import com.zharkyn.aiassistant_backend.dto.AnalyticsDtos;
import com.zharkyn.aiassistant_backend.model.ChatMessage;
import com.zharkyn.aiassistant_backend.model.InterviewEvaluation;
import com.zharkyn.aiassistant_backend.repository.InterviewEvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Сервис для AI оценки ответов на интервью и генерации аналитики
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewAnalyticsService {

    private final GeminiService geminiService;
    private final InterviewEvaluationRepository evaluationRepository;

    /**
     * Оценить ответ пользователя с помощью AI
     */
    public AnalyticsDtos.AnswerEvaluation evaluateAnswer(
            String question, String answer, String position, ChatMessage.QuestionType questionType) {
        
        log.info("Evaluating answer for question type: {}", questionType);
        
        try {
            String evaluationPrompt = buildEvaluationPrompt(question, answer, position, questionType);
            
            String aiEvaluation = geminiService.evaluateAnswer(evaluationPrompt).block();
            
            return parseAIEvaluation(aiEvaluation, questionType);
            
        } catch (Exception e) {
            log.error("Error evaluating answer", e);
            return createDefaultEvaluation(questionType);
        }
    }

    /**
     * Генерировать детальный отчет по интервью (публичный метод)
     */
    public AnalyticsDtos.InterviewReport generateInterviewReport(String interviewId) 
            throws ExecutionException, InterruptedException {
        
        // Получаем сессию и сообщения
        com.google.cloud.firestore.Firestore dbFirestore = 
            com.google.firebase.cloud.FirestoreClient.getFirestore();
        
        com.zharkyn.aiassistant_backend.model.InterviewSession session = 
            dbFirestore.collection("interview_sessions")
                .document(interviewId)
                .get()
                .get()
                .toObject(com.zharkyn.aiassistant_backend.model.InterviewSession.class);
        
        if (session == null) {
            throw new RuntimeException("Interview not found");
        }
        
        // Получаем сообщения
        com.zharkyn.aiassistant_backend.repository.ChatMessageRepository msgRepo = 
            new com.zharkyn.aiassistant_backend.repository.ChatMessageRepository();
        List<ChatMessage> messages = msgRepo.findBySessionIdOrderByTimestampAsc(interviewId);
        
        return generateReport(interviewId, messages, session.getPosition());
    }
    
    /**
     * Генерировать детальный отчет по интервью (внутренний метод)
     */
    private AnalyticsDtos.InterviewReport generateReport(
            String interviewId, List<ChatMessage> messages, String position) 
            throws ExecutionException, InterruptedException {
        
        log.info("Generating report for interview: {}", interviewId);
        
        // Получаем или создаем оценки для каждого ответа
        List<InterviewEvaluation> evaluations = new ArrayList<>();
        
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            if (msg.getRole() == ChatMessage.MessageRole.USER && i > 0) {
                ChatMessage question = messages.get(i - 1);
                
                // Проверяем есть ли уже оценка
                Optional<InterviewEvaluation> existing = evaluationRepository
                    .findByInterviewIdAndQuestionNumber(interviewId, msg.getQuestionNumber());
                
                InterviewEvaluation evaluation;
                if (existing.isPresent()) {
                    evaluation = existing.get();
                } else {
                    // Создаем новую оценку
                    AnalyticsDtos.AnswerEvaluation aiEval = evaluateAnswer(
                        question.getContent(), 
                        msg.getContent(), 
                        position,
                        question.getQuestionType()
                    );
                    
                    evaluation = InterviewEvaluation.builder()
                        .interviewId(interviewId)
                        .questionNumber(msg.getQuestionNumber())
                        .questionType(question.getQuestionType())
                        .score(aiEval.getScore())
                        .feedback(aiEval.getFeedback())
                        .strengths(aiEval.getStrengths())
                        .improvements(aiEval.getImprovements())
                        .build();
                    
                    evaluationRepository.save(evaluation);
                }
                
                evaluations.add(evaluation);
            }
        }
        
        // Анализируем по категориям навыков
        Map<String, AnalyticsDtos.SkillAnalysis> skillsAnalysis = analyzeSkills(evaluations);
        
        // Общая статистика
        double averageScore = evaluations.stream()
            .mapToInt(InterviewEvaluation::getScore)
            .average()
            .orElse(0.0);
        
        int totalQuestions = evaluations.size();
        
        // Генерируем общие рекомендации
        List<String> recommendations = generateRecommendations(skillsAnalysis, averageScore);
        
        // Определяем сильные и слабые стороны
        List<String> strengths = evaluations.stream()
            .flatMap(e -> e.getStrengths().stream())
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
        
        List<String> weaknesses = evaluations.stream()
            .flatMap(e -> e.getImprovements().stream())
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
        
        return AnalyticsDtos.InterviewReport.builder()
            .interviewId(interviewId)
            .overallScore(averageScore)
            .totalQuestions(totalQuestions)
            .skillsAnalysis(skillsAnalysis)
            .strengths(strengths)
            .weaknesses(weaknesses)
            .recommendations(recommendations)
            .questionEvaluations(evaluations.stream()
                .map(this::mapToQuestionEvaluation)
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Получить аналитику по навыкам
     */
    private Map<String, AnalyticsDtos.SkillAnalysis> analyzeSkills(List<InterviewEvaluation> evaluations) {
        Map<String, AnalyticsDtos.SkillAnalysis> skillsMap = new HashMap<>();
        
        // Группируем по типам вопросов
        Map<ChatMessage.QuestionType, List<InterviewEvaluation>> byType = evaluations.stream()
            .collect(Collectors.groupingBy(InterviewEvaluation::getQuestionType));
        
        // Анализируем каждый тип
        for (Map.Entry<ChatMessage.QuestionType, List<InterviewEvaluation>> entry : byType.entrySet()) {
            String skillName = getSkillNameForQuestionType(entry.getKey());
            List<InterviewEvaluation> evals = entry.getValue();
            
            double avgScore = evals.stream()
                .mapToInt(InterviewEvaluation::getScore)
                .average()
                .orElse(0.0);
            
            int questionsCount = evals.size();
            
            String performance = determinePerformance(avgScore);
            
            List<String> keyPoints = evals.stream()
                .flatMap(e -> e.getStrengths().stream())
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
            
            skillsMap.put(skillName, AnalyticsDtos.SkillAnalysis.builder()
                .skillName(skillName)
                .averageScore(avgScore)
                .questionsCount(questionsCount)
                .performance(performance)
                .keyPoints(keyPoints)
                .build());
        }
        
        return skillsMap;
    }

    /**
     * Генерировать рекомендации на основе анализа
     */
    private List<String> generateRecommendations(
            Map<String, AnalyticsDtos.SkillAnalysis> skillsAnalysis, double averageScore) {
        
        List<String> recommendations = new ArrayList<>();
        
        // Общая рекомендация по баллу
        if (averageScore >= 8.0) {
            recommendations.add("Отличная работа! Вы показали высокий уровень подготовки.");
            recommendations.add("Продолжайте практиковаться для поддержания навыков.");
        } else if (averageScore >= 6.0) {
            recommendations.add("Хорошие результаты. Есть области для улучшения.");
            recommendations.add("Сфокусируйтесь на слабых сторонах для повышения общего балла.");
        } else {
            recommendations.add("Требуется больше практики. Не расстраивайтесь!");
            recommendations.add("Рекомендуем пройти еще несколько интервью для улучшения навыков.");
        }
        
        // Рекомендации по навыкам
        skillsAnalysis.forEach((skill, analysis) -> {
            if (analysis.getAverageScore() < 6.0) {
                recommendations.add(String.format(
                    "Уделите больше внимания навыку: %s (текущий балл: %.1f/10)",
                    skill, analysis.getAverageScore()
                ));
            }
        });
        
        return recommendations;
    }

    // === Helper methods ===

    private String buildEvaluationPrompt(String question, String answer, String position, 
                                        ChatMessage.QuestionType questionType) {
        return String.format(
            "You are an expert interviewer evaluating a candidate's answer.\n\n" +
            "Position: %s\n" +
            "Question Type: %s\n" +
            "Question: %s\n" +
            "Candidate's Answer: %s\n\n" +
            "Please evaluate this answer and provide:\n" +
            "1. Score (0-10)\n" +
            "2. Overall Feedback (2-3 sentences)\n" +
            "3. Strengths (list 2-3 points)\n" +
            "4. Areas for Improvement (list 2-3 points)\n\n" +
            "Format your response as:\n" +
            "Score: X/10\n" +
            "Feedback: [your feedback]\n" +
            "Strengths:\n- [strength 1]\n- [strength 2]\n" +
            "Improvements:\n- [improvement 1]\n- [improvement 2]",
            position, questionType, question, answer
        );
    }

    private AnalyticsDtos.AnswerEvaluation parseAIEvaluation(String aiResponse, 
                                                            ChatMessage.QuestionType questionType) {
        try {
            int score = extractScore(aiResponse);
            String feedback = extractSection(aiResponse, "Feedback:");
            List<String> strengths = extractList(aiResponse, "Strengths:");
            List<String> improvements = extractList(aiResponse, "Improvements:");
            
            return AnalyticsDtos.AnswerEvaluation.builder()
                .score(score)
                .feedback(feedback)
                .strengths(strengths)
                .improvements(improvements)
                .questionType(questionType.name())
                .build();
        } catch (Exception e) {
            log.error("Error parsing AI evaluation", e);
            return createDefaultEvaluation(questionType);
        }
    }

    private int extractScore(String text) {
        // Ищем паттерн "Score: X/10"
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.startsWith("Score:")) {
                String scoreStr = line.replace("Score:", "").replace("/10", "").trim();
                try {
                    return Integer.parseInt(scoreStr);
                } catch (NumberFormatException e) {
                    return 7; // Default
                }
            }
        }
        return 7;
    }

    private String extractSection(String text, String sectionName) {
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(sectionName)) {
                return lines[i].replace(sectionName, "").trim();
            }
        }
        return "Good answer overall.";
    }

    private List<String> extractList(String text, String sectionName) {
        List<String> items = new ArrayList<>();
        String[] lines = text.split("\n");
        boolean inSection = false;
        
        for (String line : lines) {
            if (line.startsWith(sectionName)) {
                inSection = true;
                continue;
            }
            
            if (inSection && line.startsWith("-")) {
                items.add(line.replace("-", "").trim());
            } else if (inSection && !line.startsWith("-") && !line.trim().isEmpty()) {
                break; // End of section
            }
        }
        
        return items.isEmpty() ? List.of("Continue practicing") : items;
    }

    private AnalyticsDtos.AnswerEvaluation createDefaultEvaluation(ChatMessage.QuestionType questionType) {
        return AnalyticsDtos.AnswerEvaluation.builder()
            .score(7)
            .feedback("Your answer demonstrates understanding of the topic.")
            .strengths(List.of("Clear communication", "Relevant examples"))
            .improvements(List.of("Add more specific details", "Structure your answer better"))
            .questionType(questionType.name())
            .build();
    }

    private String getSkillNameForQuestionType(ChatMessage.QuestionType type) {
        return switch (type) {
            case BACKGROUND -> "Communication & Experience";
            case SITUATIONAL -> "Problem Solving & Decision Making";
            case TECHNICAL -> "Technical Knowledge & Skills";
        };
    }

    private String determinePerformance(double score) {
        if (score >= 8.0) return "Excellent";
        if (score >= 6.0) return "Good";
        if (score >= 4.0) return "Fair";
        return "Needs Improvement";
    }

    private AnalyticsDtos.QuestionEvaluation mapToQuestionEvaluation(InterviewEvaluation eval) {
        return AnalyticsDtos.QuestionEvaluation.builder()
            .questionNumber(eval.getQuestionNumber())
            .questionType(eval.getQuestionType().name())
            .score(eval.getScore())
            .feedback(eval.getFeedback())
            .strengths(eval.getStrengths())
            .improvements(eval.getImprovements())
            .build();
    }
}