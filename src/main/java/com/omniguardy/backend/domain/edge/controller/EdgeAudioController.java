package com.omniguardy.backend.domain.edge.controller;

import com.omniguardy.backend.domain.edge.dto.response.AudioUploadResponseDto;
import com.omniguardy.backend.domain.edge.service.EdgeAudioService;
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
public class EdgeAudioController {

    private final EdgeAudioService edgeAudioService;

    @PostMapping(
            value = "/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<AudioUploadResponseDto> uploadAudio(
            @RequestPart("file") MultipartFile file
    ) {
        AudioUploadResponseDto response =
                edgeAudioService.receiveAudio(file);

        return ApiResponse.success(
                "오디오 파일 수신에 성공했습니다.",
                response
        );
    }
}