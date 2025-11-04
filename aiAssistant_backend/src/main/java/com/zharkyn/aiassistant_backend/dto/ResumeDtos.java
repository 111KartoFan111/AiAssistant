package com.zharkyn.aiassistant_backend.dto;

import com.zharkyn.aiassistant_backend.model.Resume;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ResumeDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateResumeRequest {
        @NotNull(message = "Personal info is required")
        @Valid
        private Resume.PersonalInfo personalInfo;
        
        private String summary;
        private List<String> skills;
        private List<Resume.Experience> experience;
        private List<Resume.Education> education;
        private List<Resume.Project> projects;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateResumeRequest {
        @Valid
        private Resume.PersonalInfo personalInfo;
        private String summary;
        private List<String> skills;
        private List<Resume.Experience> experience;
        private List<Resume.Education> education;
        private List<Resume.Project> projects;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeResponse {
        private String id;
        private Resume.PersonalInfo personalInfo;
        private String summary;
        private List<String> skills;
        private List<Resume.Experience> experience;
        private List<Resume.Education> education;
        private List<Resume.Project> projects;
        private String createdAt;
        private String updatedAt;
    }
}