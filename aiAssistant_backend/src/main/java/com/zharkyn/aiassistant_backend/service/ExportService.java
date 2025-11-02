package com.zharkyn.aiassistant_backend.service;

import com.google.cloud.Timestamp;
import com.zharkyn.aiassistant_backend.dto.AnalyticsDtos;
import com.zharkyn.aiassistant_backend.model.InterviewSession;
import com.zharkyn.aiassistant_backend.model.User;
import com.zharkyn.aiassistant_backend.repository.InterviewSessionRepository;
import com.zharkyn.aiassistant_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Сервис для экспорта истории интервью и работы с черновиками
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewAnalyticsService analyticsService;
    private final UserRepository userRepository;

    /**
     * Экспортировать историю интервью в CSV
     */
    public byte[] exportToCSV(List<String> interviewIds) 
            throws ExecutionException, InterruptedException {
        
        log.info("Exporting {} interviews to CSV", interviewIds.size());
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        
        // Заголовки
        writer.println("Interview ID,Position,Company,Date,Status,Overall Score,Questions Answered");
        
        // Получаем Firestore
        com.google.cloud.firestore.Firestore dbFirestore = 
            com.google.firebase.cloud.FirestoreClient.getFirestore();
        
        // Данные
        for (String interviewId : interviewIds) {
            try {
                InterviewSession session = dbFirestore.collection("interview_sessions")
                    .document(interviewId)
                    .get()
                    .get()
                    .toObject(InterviewSession.class);
                
                if (session == null) continue;
                
                // Получаем отчет для оценки
                AnalyticsDtos.InterviewReport report = analyticsService.generateInterviewReport(interviewId);
                
                writer.printf("%s,%s,%s,%s,%s,%.2f,%d%n",
                    session.getId(),
                    escapeCsv(session.getPosition()),
                    escapeCsv(session.getCompany()),
                    session.getCreatedAt() != null ? session.getCreatedAt().toDate().toString() : "",
                    session.getStatus(),
                    report.getOverallScore(),
                    session.getCurrentQuestionNumber()
                );
            } catch (Exception e) {
                log.error("Error exporting interview {}", interviewId, e);
            }
        }
        
        writer.flush();
        writer.close();
        
        log.info("CSV export completed");
        return outputStream.toByteArray();
    }

    /**
     * Экспортировать детальный отчет в PDF (simplified - возвращает HTML для конвертации)
     */
    public String exportToPDFHtml(List<String> interviewIds) 
            throws ExecutionException, InterruptedException {
        
        log.info("Generating PDF HTML for {} interviews", interviewIds.size());
        
        // Получаем Firestore
        com.google.cloud.firestore.Firestore dbFirestore = 
            com.google.firebase.cloud.FirestoreClient.getFirestore();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Interview Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 40px; }");
        html.append("h1 { color: #3D2D4C; }");
        html.append("h2 { color: #555; margin-top: 30px; }");
        html.append(".interview { margin-bottom: 40px; page-break-after: always; }");
        html.append(".score { font-size: 24px; font-weight: bold; color: #3D2D4C; }");
        html.append(".skills { margin: 20px 0; }");
        html.append(".skill-item { margin: 10px 0; padding: 10px; background: #f5f5f5; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
        html.append("th { background-color: #3D2D4C; color: white; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<h1>Interview Reports</h1>");
        
        for (String interviewId : interviewIds) {
            try {
                InterviewSession session = dbFirestore.collection("interview_sessions")
                    .document(interviewId)
                    .get()
                    .get()
                    .toObject(InterviewSession.class);
                
                if (session == null) continue;
                
                AnalyticsDtos.InterviewReport report = analyticsService.generateInterviewReport(interviewId);
                
                html.append("<div class='interview'>");
                html.append(String.format("<h2>Interview: %s</h2>", session.getPosition()));
                html.append(String.format("<p><strong>Company:</strong> %s</p>", session.getCompany()));
                html.append(String.format("<p><strong>Date:</strong> %s</p>", 
                    session.getCreatedAt() != null ? session.getCreatedAt().toDate().toString() : "N/A"));
                html.append(String.format("<p class='score'>Overall Score: %.1f/10</p>", report.getOverallScore()));
                
                // Skills analysis
                html.append("<div class='skills'>");
                html.append("<h3>Skills Analysis</h3>");
                report.getSkillsAnalysis().forEach((skill, analysis) -> {
                    html.append("<div class='skill-item'>");
                    html.append(String.format("<strong>%s</strong>: %.1f/10 (%s)<br>", 
                        skill, analysis.getAverageScore(), analysis.getPerformance()));
                    html.append("</div>");
                });
                html.append("</div>");
                
                // Recommendations
                html.append("<h3>Recommendations</h3>");
                html.append("<ul>");
                report.getRecommendations().forEach(rec -> 
                    html.append(String.format("<li>%s</li>", rec)));
                html.append("</ul>");
                
                html.append("</div>");
                
            } catch (Exception e) {
                log.error("Error exporting interview {}", interviewId, e);
            }
        }
        
        html.append("</body></html>");
        
        log.info("PDF HTML generation completed");
        return html.toString();
    }

    /**
     * Сохранить интервью как черновик
     */
    public void saveDraft(String interviewId) 
            throws ExecutionException, InterruptedException {
        
        User currentUser = getCurrentUser();
        
        com.google.cloud.firestore.Firestore dbFirestore = 
            com.google.firebase.cloud.FirestoreClient.getFirestore();
        
        InterviewSession session = dbFirestore.collection("interview_sessions")
            .document(interviewId)
            .get()
            .get()
            .toObject(InterviewSession.class);
        
        if (session == null) {
            throw new RuntimeException("Interview not found");
        }
        
        if (!session.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to interview");
        }
        
        // Помечаем как черновик (можно добавить отдельный статус DRAFT)
        session.setStatus(InterviewSession.InterviewStatus.IN_PROGRESS);
        sessionRepository.save(session);
        
        log.info("Interview {} saved as draft", interviewId);
    }

    /**
     * Возобновить интервью из черновика
     */
    public InterviewSession resumeDraft(String interviewId) 
            throws ExecutionException, InterruptedException {
        
        User currentUser = getCurrentUser();
        
        com.google.cloud.firestore.Firestore dbFirestore = 
            com.google.firebase.cloud.FirestoreClient.getFirestore();
        
        InterviewSession session = dbFirestore.collection("interview_sessions")
            .document(interviewId)
            .get()
            .get()
            .toObject(InterviewSession.class);
        
        if (session == null) {
            throw new RuntimeException("Interview not found");
        }
        
        if (!session.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to interview");
        }
        
        if (session.getStatus() != InterviewSession.InterviewStatus.IN_PROGRESS) {
            throw new RuntimeException("Interview is not in progress");
        }
        
        log.info("Resuming interview {} from draft", interviewId);
        return session;
    }

    /**
     * Поставить интервью на паузу
     */
    public void pauseInterview(String interviewId) 
            throws ExecutionException, InterruptedException {
        
        User currentUser = getCurrentUser();
        
        com.google.cloud.firestore.Firestore dbFirestore = 
            com.google.firebase.cloud.FirestoreClient.getFirestore();
        
        InterviewSession session = dbFirestore.collection("interview_sessions")
            .document(interviewId)
            .get()
            .get()
            .toObject(InterviewSession.class);
        
        if (session == null) {
            throw new RuntimeException("Interview not found");
        }
        
        if (!session.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to interview");
        }
        
        // Можно добавить поле lastActivity для отслеживания паузы
        // session.setUpdatedAt(Timestamp.now()); // Убрано, т.к. поля нет в модели
        sessionRepository.save(session);
        
        log.info("Interview {} paused", interviewId);
    }

    // === Helper methods ===

    private User getCurrentUser() throws ExecutionException, InterruptedException {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}