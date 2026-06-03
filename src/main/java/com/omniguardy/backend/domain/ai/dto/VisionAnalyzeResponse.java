package com.omniguardy.backend.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VisionAnalyzeResponse {

    private String video;
    private String events;
    private int riskScore;
    private String riskLevel;
}
