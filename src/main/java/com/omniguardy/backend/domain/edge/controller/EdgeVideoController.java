package com.omniguardy.backend.domain.edge.controller;

import com.omniguardy.backend.domain.edge.dto.response.VideoUploadResponseDto;
import com.omniguardy.backend.domain.edge.service.EdgeVideoService;
import com.omniguardy.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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

            @RequestPart("file")
            MultipartFile file,

            @RequestParam("eventId")
            String eventId

    ) {

        VideoUploadResponseDto response =
                edgeVideoService.receiveVideo(
                        file,
                        eventId
                );


        return ApiResponse.success(
                "영상 분석이 완료되었습니다.",
                response
        );
    }
}
