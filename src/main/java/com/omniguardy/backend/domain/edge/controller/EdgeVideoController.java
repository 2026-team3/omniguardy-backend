package com.omniguardy.backend.domain.edge.controller;

import com.omniguardy.backend.domain.edge.dto.response.VideoUploadResponseDto;
import com.omniguardy.backend.domain.edge.service.EdgeVideoService;
import com.omniguardy.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/edge")
public class EdgeVideoController {

    private final EdgeVideoService edgeVideoService;

    @PostMapping(
            value = "/video",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<VideoUploadResponseDto> uploadVideo(
            @RequestPart("file") MultipartFile file
    ) {

        VideoUploadResponseDto response =
                edgeVideoService.receiveVideo(file);

        return ApiResponse.success(
                "영상 파일 수신에 성공했습니다.",
                response
        );
    }
}
