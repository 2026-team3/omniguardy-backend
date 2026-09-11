package com.omniguardy.backend.domain.detection.presentation;

import com.omniguardy.backend.domain.detection.application.model.VideoReceipt;
import com.omniguardy.backend.domain.detection.application.usecase.ReceiveVideoUseCase;
import com.omniguardy.backend.domain.detection.presentation.dto.response.VideoUploadResponseDto;
import com.omniguardy.backend.domain.detection.presentation.mapper.DetectionPresentationMapper;
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
    private final DetectionPresentationMapper mapper;

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VideoUploadResponseDto> uploadVideo(@RequestPart("file") MultipartFile file,
                                                            @RequestParam("eventId") String eventId) throws IOException {
        VideoReceipt result = receiveVideoUseCase.receive(mapper.toMediaFile(file), eventId);
        return ApiResponse.success("?곸긽 遺꾩꽍???꾨즺?섏뿀?듬땲??",
                mapper.toResponse(result));
    }
}

