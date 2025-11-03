package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.AnalyticsDtos;
import com.zharkyn.aiassistant_backend.service.InterviewAnalyticsService;
import com.zharkyn.aiassistant_backend.service.ProgressAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

/**
 * REST контроллер для аналитики и отчетов
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final InterviewAnalyticsService analyticsService;
    private final ProgressAnalyticsService progressAnalyticsService;

    /**
     * Получить общую аналитику прогресса пользователя
     */
    @GetMapping("/progress")
    public ResponseEntity<AnalyticsDtos.ProgressAnalytics> getProgressAnalytics() {
        try {
            log.info("Fetching progress analytics for current user");
            AnalyticsDtos.ProgressAnalytics analytics = progressAnalyticsService.getProgressAnalytics();
            log.info("Progress analytics retrieved successfully");
            return ResponseEntity.ok(analytics);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error fetching progress analytics", e);
            throw new RuntimeException("Failed to fetch progress analytics: " + e.getMessage(), e);
        }
    }

    /**
     * Получить аналитику по навыкам
     */
    @GetMapping("/skills")
    public ResponseEntity<AnalyticsDtos.SkillAnalysis> getSkillsAnalytics() {
        try {
            log.info("Fetching skills analytics for current user");
            AnalyticsDtos.SkillAnalysis analysis = progressAnalyticsService.getSkillsAnalytics();
            log.info("Skills analytics retrieved successfully");
            return ResponseEntity.ok(analysis);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error fetching skills analytics", e);
            throw new RuntimeException("Failed to fetch skills analytics: " + e.getMessage(), e);
        }
    }

    /**
     * Получить детальный отчет по интервью
     */
    @GetMapping("/interviews/{interviewId}/report")
    public ResponseEntity<AnalyticsDtos.InterviewReport> getInterviewReport(
            @PathVariable String interviewId) {
        try {
            log.info("Generating report for interview: {}", interviewId);
            AnalyticsDtos.InterviewReport report = analyticsService.generateInterviewReport(interviewId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error generating report", e);
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }
}