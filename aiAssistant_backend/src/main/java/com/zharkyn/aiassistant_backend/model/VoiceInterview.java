package com.zharkyn.aiassistant_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceInterview {
    private String id;
    private String userId;
    private String positionTitle;
    private String companyName;
    private String status;
    private Long createdAt;
    private Long updatedAt;
}