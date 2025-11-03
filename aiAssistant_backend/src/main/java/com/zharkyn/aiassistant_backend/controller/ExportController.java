package com.zharkyn.aiassistant_backend.controller;

import com.itextpdf.text.DocumentException;
import com.zharkyn.aiassistant_backend.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/export")
@CrossOrigin(origins = "*")
@Slf4j
public class ExportController {

    private final ExportService exportService;

    @Autowired
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/csv")
    public ResponseEntity<byte[]> exportToCSV(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> interviewIds = request.get("interviewIds");
            log.info("Exporting {} interviews to CSV", interviewIds.size());
            
            byte[] csvData = exportService.exportToCSV(interviewIds);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "interviews.csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
                    
        } catch (Exception e) {
            log.error("Error exporting to CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> exportToPDF(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> interviewIds = request.get("interviewIds");
            log.info("Exporting {} interviews to PDF", interviewIds.size());
            
            byte[] pdfData = exportService.exportToPDFHtml(interviewIds);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "interviews.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
                    
        } catch (Exception e) {
            log.error("Error exporting to PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{interviewId}/draft")
    public ResponseEntity<Map<String, String>> saveDraft(@PathVariable String interviewId) {
        try {
            log.info("Saving draft for interview: {}", interviewId);
            String result = exportService.saveDraft(interviewId);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            log.error("Error saving draft", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{interviewId}/pause")
    public ResponseEntity<Map<String, String>> pauseInterview(@PathVariable String interviewId) {
        try {
            log.info("Pausing interview: {}", interviewId);
            String result = exportService.pauseInterview(interviewId);
            return ResponseEntity.ok(Map.of("message", result));
        } catch (Exception e) {
            log.error("Error pausing interview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{interviewId}/pdf")
    public ResponseEntity<byte[]> exportInterviewToPDF(@PathVariable String interviewId) {
        try {
            log.info("Exporting single interview to PDF: {}", interviewId);
            
            byte[] pdfData = exportService.exportInterviewToPDF(interviewId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "interview_" + interviewId + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
                    
        } catch (ExecutionException | InterruptedException | DocumentException e) {
            log.error("Error exporting interview to PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}