package com.omniguardy.backend.domain.detection.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AudioUploadResponseDto {

    private String eventId;
    private String filename;
    private long size;
    private String status;
    private double probability;
}
