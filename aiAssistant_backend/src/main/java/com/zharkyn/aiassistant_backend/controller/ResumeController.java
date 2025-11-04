package com.zharkyn.aiassistant_backend.controller;

import com.zharkyn.aiassistant_backend.dto.ResumeDtos;
import com.zharkyn.aiassistant_backend.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/v1/resume")
@RequiredArgsConstructor
public class ResumeController {
    
    private final ResumeService resumeService;

    /**
     * Создать новое резюме для текущего пользователя
     */
    @PostMapping
    public ResponseEntity<ResumeDtos.ResumeResponse> createResume(
            @Valid @RequestBody ResumeDtos.CreateResumeRequest request) 
            throws ExecutionException, InterruptedException {
        log.info("Creating resume for current user");
        try {
            ResumeDtos.ResumeResponse response = resumeService.createOrUpdateResume(request);
            log.info("Resume created successfully with id: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Failed to create resume", ex);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Получить резюме текущего пользователя
     */
    @GetMapping
    public ResponseEntity<ResumeDtos.ResumeResponse> getMyResume() 
            throws ExecutionException, InterruptedException {
        log.info("Fetching resume for current user");
        try {
            ResumeDtos.ResumeResponse response = resumeService.getMyResume();
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("resume not found")) {
                return ResponseEntity.notFound().build();
            }
            throw ex;
        }
    }

    /**
     * Обновить резюме текущего пользователя
     */
    @PutMapping
    public ResponseEntity<ResumeDtos.ResumeResponse> updateResume(
            @Valid @RequestBody ResumeDtos.UpdateResumeRequest request) 
            throws ExecutionException, InterruptedException {
        log.info("Updating resume for current user");
        try {
            ResumeDtos.ResumeResponse response = resumeService.updateResume(request);
            log.info("Resume updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Failed to update resume", ex);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Удалить резюме текущего пользователя
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteResume() 
            throws ExecutionException, InterruptedException {
        log.info("Deleting resume for current user");
        resumeService.deleteResume();
        log.info("Resume deleted successfully");
        return ResponseEntity.noContent().build();
    }
}