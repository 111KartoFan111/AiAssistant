package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.AnalyticsDtos;
import com.zharkyn.aiassistant_backend.service.ExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для экспорта данных
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /**
     * Экспортировать историю интервью в CSV
     */
    @PostMapping("/csv")
    public ResponseEntity<byte[]> exportToCSV(
            @Valid @RequestBody AnalyticsDtos.ExportRequest request) {
        try {
            log.info("Exporting {} interviews to CSV", request.getInterviewIds().size());
            
            byte[] csvData = exportService.exportToCSV(request.getInterviewIds());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "interviews.csv");
            headers.setContentLength(csvData.length);
            
            log.info("CSV export completed");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
                    
        } catch (Exception e) {
            log.error("Error exporting to CSV", e);
            throw new RuntimeException("Failed to export to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Экспортировать детальный отчет в HTML (для конвертации в PDF на клиенте)
     */
    @PostMapping("/pdf-html")
    public ResponseEntity<String> exportToPDFHtml(
            @Valid @RequestBody AnalyticsDtos.ExportRequest request) {
        try {
            log.info("Generating PDF HTML for {} interviews", request.getInterviewIds().size());
            
            String html = exportService.exportToPDFHtml(request.getInterviewIds());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            
            log.info("PDF HTML generation completed");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(html);
                    
        } catch (Exception e) {
            log.error("Error generating PDF HTML", e);
            throw new RuntimeException("Failed to generate PDF HTML: " + e.getMessage(), e);
        }
    }

    /**
     * Сохранить интервью как черновик
     */
    @PostMapping("/interviews/{interviewId}/draft")
    public ResponseEntity<Void> saveDraft(@PathVariable String interviewId) {
        try {
            log.info("Saving interview {} as draft", interviewId);
            exportService.saveDraft(interviewId);
            log.info("Interview saved as draft");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error saving draft", e);
            throw new RuntimeException("Failed to save draft: " + e.getMessage(), e);
        }
    }

    /**
     * Поставить интервью на паузу
     */
    @PostMapping("/interviews/{interviewId}/pause")
    public ResponseEntity<Void> pauseInterview(@PathVariable String interviewId) {
        try {
            log.info("Pausing interview {}", interviewId);
            exportService.pauseInterview(interviewId);
            log.info("Interview paused");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error pausing interview", e);
            throw new RuntimeException("Failed to pause interview: " + e.getMessage(), e);
        }
    }
    /**
     * Экспортировать результаты интервью в PDF
     */
    @GetMapping("/{interviewId}")
    public ResponseEntity<ByteArrayResource> exportInterviewPDF(@PathVariable String interviewId) {
        try {
            log.info("Exporting interview {} to PDF", interviewId);
            byte[] pdfBytes = exportService.exportInterviewToPDF(interviewId);
            
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                String.format("attachment; filename=interview-%s.pdf", interviewId));
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
            
            log.info("PDF exported successfully for interview: {}", interviewId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfBytes.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error exporting interview to PDF", e);
            throw new RuntimeException("Failed to export interview: " + e.getMessage(), e);
        }
    }
}