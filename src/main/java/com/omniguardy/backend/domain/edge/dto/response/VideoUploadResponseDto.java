package com.omniguardy.backend.domain.edge.dto.response;

import com.omniguardy.backend.domain.ai.dto.VisionResultDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoUploadResponseDto {

    private String eventId;

    private String filename;

    private VisionResultDto vision;
}
