package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.AnalyticsDtos;
import com.zharkyn.aiassistant_backend.service.InterviewAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для аналитики и отчетов
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final InterviewAnalyticsService analyticsService;

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