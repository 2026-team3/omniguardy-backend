package com.omniguardy.backend.detection.presentation;

import com.omniguardy.backend.detection.application.model.MediaFile;
import com.omniguardy.backend.detection.application.model.VideoReceipt;
import com.omniguardy.backend.detection.application.usecase.ReceiveVideoUseCase;
import com.omniguardy.backend.domain.edge.dto.response.VideoUploadResponseDto;
import com.omniguardy.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/edge")
public class EdgeVideoController {
    private final ReceiveVideoUseCase receiveVideoUseCase;

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VideoUploadResponseDto> uploadVideo(@RequestPart("file") MultipartFile file,
                                                            @RequestParam("eventId") String eventId) throws IOException {
        MediaFile media = new MediaFile(file == null ? null : file.getOriginalFilename(),
                file == null ? null : file.getContentType(), file == null ? null : file.getBytes());
        VideoReceipt result = receiveVideoUseCase.receive(media, eventId);
        return ApiResponse.success("영상 분석이 완료되었습니다.",
                new VideoUploadResponseDto(result.eventId(), result.filename(), result.vision()));
    }
}
