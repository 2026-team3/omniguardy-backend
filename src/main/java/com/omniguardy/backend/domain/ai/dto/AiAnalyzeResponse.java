package com.omniguardy.backend.domain.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiAnalyzeResponse {

    private VisionAnalyzeResponse vision;
    private AudioAnalyzeResponse audio;
}
