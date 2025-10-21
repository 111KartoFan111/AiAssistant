package com.zharkyn.aiassistant_backend.dto;

import com.zharkyn.aiassistant_backend.model.Resume;
import jakarta.validation.constraints.NotEmpty;
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
        @NotEmpty(message = "Personal info is required")
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