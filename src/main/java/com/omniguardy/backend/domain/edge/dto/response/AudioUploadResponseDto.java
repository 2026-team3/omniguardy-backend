package com.omniguardy.backend.domain.edge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AudioUploadResponseDto {

    private String filename;
    private long size;
}
