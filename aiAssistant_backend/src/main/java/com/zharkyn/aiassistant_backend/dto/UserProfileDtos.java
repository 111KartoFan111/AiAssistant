package com.zharkyn.aiassistant_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO классы для профиля пользователя
 */
public class UserProfileDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileResponse {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String bio;
        private String avatarBase64;
        private String linkedInProfile;
        private String githubProfile;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        @Size(max = 500, message = "Bio must be less than 500 characters")
        private String bio;
        private String linkedInProfile;
        private String githubProfile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvatarResponse {
        private String avatarUrl;
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotEmpty(message = "Old password is required")
        private String oldPassword;
        
        @NotEmpty(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;
    }
}