package com.zharkyn.aiassistant_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTOs for Gemini API request and response
public class GeminiDtos {

    // Request structures
    @Data
    @Builder
    public static class GeminiRequest {
        private List<Content> contents;
    }

    @Data
    @Builder
    public static class Content {
        private String role; // "user" or "model"
        private List<Part> parts;
    }

    @Data
    @Builder
    public static class Part {
        private String text;
    }

    // Response structures
    @Data
    @NoArgsConstructor
    public static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
        @JsonProperty("finishReason")
        private String finishReason;
    }
}
