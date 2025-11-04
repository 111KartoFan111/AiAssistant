package com.zharkyn.aiassistant_backend.service;

import com.google.cloud.Timestamp;
import com.zharkyn.aiassistant_backend.dto.ResumeDtos;
import com.zharkyn.aiassistant_backend.model.Resume;
import com.zharkyn.aiassistant_backend.model.User;
import com.zharkyn.aiassistant_backend.repository.ResumeRepository;
import com.zharkyn.aiassistant_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeDtos.ResumeResponse createOrUpdateResume(ResumeDtos.CreateResumeRequest request) throws ExecutionException, InterruptedException {
        User currentUser = getCurrentUser();
        
        Resume resume = resumeRepository.findByUserId(currentUser.getId())
                .orElse(Resume.builder()
                        .userId(currentUser.getId())
                        .createdAt(Timestamp.now())
                        .build());

        resume.setPersonalInfo(request.getPersonalInfo());
        resume.setSummary(request.getSummary());
        resume.setSkills(request.getSkills());
        resume.setExperience(request.getExperience());
        resume.setEducation(request.getEducation());
        resume.setProjects(request.getProjects());
        resume.setUpdatedAt(Timestamp.now());

        Resume savedResume = resumeRepository.save(resume);
        return mapToResponse(savedResume);
    }

    public ResumeDtos.ResumeResponse getMyResume() throws ExecutionException, InterruptedException {
        User currentUser = getCurrentUser();
        Resume resume = resumeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        return mapToResponse(resume);
    }

    public ResumeDtos.ResumeResponse updateResume(ResumeDtos.UpdateResumeRequest request) throws ExecutionException, InterruptedException {
        User currentUser = getCurrentUser();
        Resume resume = resumeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (request.getPersonalInfo() != null) {
            resume.setPersonalInfo(request.getPersonalInfo());
        }
        if (request.getSummary() != null) {
            resume.setSummary(request.getSummary());
        }
        if (request.getSkills() != null) {
            resume.setSkills(request.getSkills());
        }
        if (request.getExperience() != null) {
            resume.setExperience(request.getExperience());
        }
        if (request.getEducation() != null) {
            resume.setEducation(request.getEducation());
        }
        if (request.getProjects() != null) {
            resume.setProjects(request.getProjects());
        }
        resume.setUpdatedAt(Timestamp.now());

        Resume savedResume = resumeRepository.save(resume);
        return mapToResponse(savedResume);
    }

    public void deleteResume() throws ExecutionException, InterruptedException {
        User currentUser = getCurrentUser();
        Resume resume = resumeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        resumeRepository.deleteById(resume.getId());
    }

    private User getCurrentUser() throws ExecutionException, InterruptedException {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        if (principal == null || principal.isBlank()) {
            throw new RuntimeException("Authenticated user not found");
        }
        if (principal.contains("@")) {
            return userRepository.findByEmail(principal)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        } else {
            return userRepository.findById(principal)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        }
    }

    private ResumeDtos.ResumeResponse mapToResponse(Resume resume) {
        return ResumeDtos.ResumeResponse.builder()
                .id(resume.getId())
                .personalInfo(resume.getPersonalInfo())
                .summary(resume.getSummary())
                .skills(resume.getSkills())
                .experience(resume.getExperience())
                .education(resume.getEducation())
                .projects(resume.getProjects())
                .createdAt(resume.getCreatedAt() != null ? resume.getCreatedAt().toString() : null)
                .updatedAt(resume.getUpdatedAt() != null ? resume.getUpdatedAt().toString() : null)
                .build();
    }
}