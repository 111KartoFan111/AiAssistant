package com.zharkyn.aiassistant_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpRequest {
        @NotEmpty(message = "Full name cannot be empty")
        private String fullName;

        @NotEmpty(message = "Email cannot be empty")
        @Email(message = "Please provide a valid email address")
        private String email;

        @NotEmpty(message = "Password cannot be empty")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignInRequest {
        @NotEmpty(message = "Email cannot be empty")
        @Email
        private String email;

        @NotEmpty(message = "Password cannot be empty")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtAuthenticationResponse {
        private String token;
    }
}
