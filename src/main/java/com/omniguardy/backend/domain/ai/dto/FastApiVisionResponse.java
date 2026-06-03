package com.omniguardy.backend.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FastApiVisionResponse {

    private String status;
    private Result result;

    @Getter
    @NoArgsConstructor
    public static class Result {

        private String video;

        private String events;

        @JsonProperty("risk_score")
        private int riskScore;

        @JsonProperty("risk_level")
        private String riskLevel;
    }
}
