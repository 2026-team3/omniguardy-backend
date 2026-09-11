package com.omniguardy.backend.domain.edge.dto.response;

import com.omniguardy.backend.detection.application.model.VisionAnalysis;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoUploadResponseDto {

    private String eventId;

    private String filename;

    private VisionAnalysis vision;
}
