package com.zharkyn.aiassistant_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interview {
    private String id;
    private String userId;
    private String position;
    private String company;
    private String jobDescription;
    private Integer difficulty;
    private String status;
    private String feedback;
    private Long createdAt;
    private Long updatedAt;
}