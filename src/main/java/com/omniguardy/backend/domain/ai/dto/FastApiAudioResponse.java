package com.omniguardy.backend.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FastApiAudioResponse {

    private String status;
    private double probability;
}
