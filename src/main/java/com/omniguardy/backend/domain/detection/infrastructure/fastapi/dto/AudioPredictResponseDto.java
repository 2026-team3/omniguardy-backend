package com.omniguardy.backend.domain.detection.infrastructure.fastapi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AudioPredictResponseDto {

    private String status;
    private double probability;
}
