package com.zharkyn.aiassistant_backend.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.zharkyn.aiassistant_backend.dto.AnalyticsDtos;
import com.zharkyn.aiassistant_backend.model.InterviewEvaluation;
import com.zharkyn.aiassistant_backend.model.InterviewSession;
import com.zharkyn.aiassistant_backend.model.User;
import com.zharkyn.aiassistant_backend.repository.InterviewEvaluationRepository;
import com.zharkyn.aiassistant_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Сервис для отслеживания прогресса пользователя
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressAnalyticsService {

    private final UserRepository userRepository;
    private final InterviewEvaluationRepository evaluationRepository;

    /**
     * Получить общую аналитику прогресса
     */
    public AnalyticsDtos.ProgressAnalytics getProgressAnalytics() 
            throws ExecutionException, InterruptedException {
        
        User currentUser = getCurrentUser();
        
        Firestore dbFirestore = FirestoreClient.getFirestore();
        
        // Получаем все интервью пользователя
        List<InterviewSession> sessions = dbFirestore.collection("interview_sessions")
                .whereEqualTo("userId", currentUser.getId())
                .whereEqualTo("status", InterviewSession.InterviewStatus.COMPLETED)
                .get()
                .get()
                .toObjects(InterviewSession.class);
        
        int totalInterviews = sessions.size();
        
        if (totalInterviews == 0) {
            return AnalyticsDtos.ProgressAnalytics.builder()
                    .totalInterviews(0)
                    .averageScore(0.0)
                    .scoreImprovement(0.0)
                    .skillsProgress(new HashMap<>())
                    .recentInterviews(new ArrayList<>())
                    .build();
        }
        
        // Собираем все оценки
        List<InterviewEvaluation> allEvaluations = new ArrayList<>();
        for (InterviewSession session : sessions) {
            allEvaluations.addAll(evaluationRepository.findByInterviewId(session.getId()));
        }
        
        // Средний балл
        double averageScore = allEvaluations.stream()
                .mapToInt(InterviewEvaluation::getScore)
                .average()
                .orElse(0.0);
        
        // Улучшение (сравнение первых 5 и последних 5 интервью)
        double scoreImprovement = calculateScoreImprovement(sessions, allEvaluations);
        
        // Прогресс по навыкам
        Map<String, AnalyticsDtos.SkillProgress> skillsProgress = 
                calculateSkillsProgress(sessions, allEvaluations);
        
        // Последние интервью
        List<AnalyticsDtos.InterviewSummary> recentInterviews = sessions.stream()
                .sorted(Comparator.comparing(InterviewSession::getCreatedAt, 
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(this::mapToSummary)
                .collect(Collectors.toList());
        
        return AnalyticsDtos.ProgressAnalytics.builder()
                .totalInterviews(totalInterviews)
                .averageScore(averageScore)
                .scoreImprovement(scoreImprovement)
                .skillsProgress(skillsProgress)
                .recentInterviews(recentInterviews)
                .build();
    }

    /**
     * Получить аналитику по навыкам (сводная)
     */
    public AnalyticsDtos.SkillAnalysis getSkillsAnalytics() 
            throws ExecutionException, InterruptedException {
        
        // Упрощенная версия - возвращает общую аналитику
        AnalyticsDtos.ProgressAnalytics progress = getProgressAnalytics();
        
        // Преобразуем в SkillAnalysis (берем лучший навык)
        Optional<Map.Entry<String, AnalyticsDtos.SkillProgress>> bestSkill = 
                progress.getSkillsProgress().entrySet().stream()
                        .max(Comparator.comparing(e -> e.getValue().getCurrentScore()));
        
        if (bestSkill.isPresent()) {
            AnalyticsDtos.SkillProgress skill = bestSkill.get().getValue();
            return AnalyticsDtos.SkillAnalysis.builder()
                    .skillName(skill.getSkillName())
                    .averageScore(skill.getCurrentScore())
                    .questionsCount(0) // TODO: посчитать
                    .performance(determinePerformance(skill.getCurrentScore()))
                    .keyPoints(List.of("Keep practicing", "Good progress"))
                    .build();
        }
        
        return AnalyticsDtos.SkillAnalysis.builder()
                .skillName("Overall")
                .averageScore(progress.getAverageScore())
                .questionsCount(0)
                .performance("Good")
                .keyPoints(List.of("Start your first interview"))
                .build();
    }

    // === Helper methods ===

    private double calculateScoreImprovement(List<InterviewSession> sessions, 
                                            List<InterviewEvaluation> allEvaluations) {
        if (sessions.size() < 2) return 0.0;
        
        // Сортируем по дате
        sessions.sort(Comparator.comparing(InterviewSession::getCreatedAt, 
                Comparator.nullsFirst(Comparator.naturalOrder())));
        
        // Берем первые 5 интервью
        List<String> firstFiveIds = sessions.stream()
                .limit(5)
                .map(InterviewSession::getId)
                .collect(Collectors.toList());
        
        // Берем последние 5 интервью
        List<String> lastFiveIds = sessions.stream()
                .skip(Math.max(0, sessions.size() - 5))
                .map(InterviewSession::getId)
                .collect(Collectors.toList());
        
        double firstAvg = allEvaluations.stream()
                .filter(e -> firstFiveIds.contains(e.getInterviewId()))
                .mapToInt(InterviewEvaluation::getScore)
                .average()
                .orElse(0.0);
        
        double lastAvg = allEvaluations.stream()
                .filter(e -> lastFiveIds.contains(e.getInterviewId()))
                .mapToInt(InterviewEvaluation::getScore)
                .average()
                .orElse(0.0);
        
        if (firstAvg == 0) return 0.0;
        
        return ((lastAvg - firstAvg) / firstAvg) * 100.0;
    }

    private Map<String, AnalyticsDtos.SkillProgress> calculateSkillsProgress(
            List<InterviewSession> sessions, List<InterviewEvaluation> allEvaluations) {
        
        Map<String, AnalyticsDtos.SkillProgress> skillsMap = new HashMap<>();
        
        // Группируем оценки по типу вопроса
        Map<String, List<InterviewEvaluation>> bySkill = allEvaluations.stream()
                .collect(Collectors.groupingBy(e -> 
                        getSkillNameForQuestionType(e.getQuestionType())));
        
        bySkill.forEach((skillName, evals) -> {
            // Текущий балл (последние интервью)
            double currentScore = evals.stream()
                    .sorted(Comparator.comparing(InterviewEvaluation::getCreatedAt, 
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(5)
                    .mapToInt(InterviewEvaluation::getScore)
                    .average()
                    .orElse(0.0);
            
            // Предыдущий балл (первые интервью)
            double previousScore = evals.stream()
                    .sorted(Comparator.comparing(InterviewEvaluation::getCreatedAt, 
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .limit(5)
                    .mapToInt(InterviewEvaluation::getScore)
                    .average()
                    .orElse(0.0);
            
            double improvement = currentScore - previousScore;
            String trend = determineTrend(improvement);
            
            skillsMap.put(skillName, AnalyticsDtos.SkillProgress.builder()
                    .skillName(skillName)
                    .currentScore(currentScore)
                    .previousScore(previousScore)
                    .improvement(improvement)
                    .trend(trend)
                    .build());
        });
        
        return skillsMap;
    }

    private AnalyticsDtos.InterviewSummary mapToSummary(InterviewSession session) {
        return AnalyticsDtos.InterviewSummary.builder()
                .interviewId(session.getId())
                .position(session.getPosition())
                .date(session.getCreatedAt() != null ? 
                      session.getCreatedAt().toDate().toString() : "")
                .score(0.0) // TODO: посчитать из оценок
                .status(session.getStatus().name())
                .build();
    }

    private String getSkillNameForQuestionType(com.zharkyn.aiassistant_backend.model.ChatMessage.QuestionType type) {
        return switch (type) {
            case BACKGROUND -> "Communication & Experience";
            case SITUATIONAL -> "Problem Solving";
            case TECHNICAL -> "Technical Skills";
        };
    }

    private String determineTrend(double improvement) {
        if (improvement > 0.5) return "Improving";
        if (improvement < -0.5) return "Declining";
        return "Stable";
    }

    private String determinePerformance(double score) {
        if (score >= 8.0) return "Excellent";
        if (score >= 6.0) return "Good";
        if (score >= 4.0) return "Fair";
        return "Needs Improvement";
    }

    private User getCurrentUser() throws ExecutionException, InterruptedException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }
}