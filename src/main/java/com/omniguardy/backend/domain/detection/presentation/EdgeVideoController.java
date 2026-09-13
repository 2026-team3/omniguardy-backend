package com.omniguardy.backend.domain.detection.presentation;

import com.omniguardy.backend.domain.detection.application.model.VideoReceipt;
import com.omniguardy.backend.domain.detection.application.usecase.ReceiveVideoUseCase;
import com.omniguardy.backend.domain.detection.presentation.dto.response.VideoUploadResponseDto;
import com.omniguardy.backend.domain.detection.presentation.mapper.DetectionPresentationMapper;
import com.omniguardy.backend.domain.detection.presentation.success.DetectionSuccessCode;
import com.omniguardy.backend.global.response.ApiResponse;
import com.omniguardy.backend.global.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/edge")
public class EdgeVideoController {
    private final ReceiveVideoUseCase receiveVideoUseCase;
    private final DetectionPresentationMapper mapper;

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VideoUploadResponseDto>> uploadVideo(
            @RequestPart("file") MultipartFile file, @RequestParam("eventId") String eventId) throws IOException {
        VideoReceipt result = receiveVideoUseCase.receive(mapper.toMediaFile(file), eventId);
        return SuccessResponse.of(DetectionSuccessCode.VIDEO_ANALYZED, mapper.toResponse(result));
    }
}
