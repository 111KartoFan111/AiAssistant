package com.zharkyn.aiassistant_backend.model;

import com.google.cloud.Timestamp;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    private String id;
    private String userId;
    private PersonalInfo personalInfo;
    private String summary;
    private List<String> skills;
    private List<Experience> experience;
    private List<Education> education;
    private List<Project> projects;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfo {
        @NotBlank(message = "Name is required")
        private String name;
        
        private String title;
        
        @Email(message = "Invalid email format")
        private String email;
        
        private String phone;
        private String location;
        private String linkedin;
        private String github;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Experience {
        private String company;
        private String position;
        private String period;
        private String location;
        private List<String> achievements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Education {
        private String degree;
        private String institution;
        private String period;
        private String gpa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {
        private String name;
        private String description;
        private List<String> technologies;
    }
}