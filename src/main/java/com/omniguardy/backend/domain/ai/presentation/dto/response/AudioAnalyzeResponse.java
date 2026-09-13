package com.omniguardy.backend.domain.ai.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AudioAnalyzeResponse {

    private String status;
    private double probability;
}
