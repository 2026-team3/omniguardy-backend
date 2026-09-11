package com.omniguardy.backend.domain.detection.presentation;

import com.omniguardy.backend.domain.detection.application.model.AudioReceipt;
import com.omniguardy.backend.domain.detection.application.usecase.ReceiveAudioUseCase;
import com.omniguardy.backend.domain.detection.presentation.dto.response.AudioUploadResponseDto;
import com.omniguardy.backend.domain.detection.presentation.mapper.DetectionPresentationMapper;
import com.omniguardy.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/edge")
public class EdgeAudioController {
    private final ReceiveAudioUseCase receiveAudioUseCase;
    private final DetectionPresentationMapper mapper;

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AudioUploadResponseDto> uploadAudio(@RequestPart("file") MultipartFile file) throws IOException {
        AudioReceipt result = receiveAudioUseCase.receive(mapper.toMediaFile(file));
        return ApiResponse.success("?ㅻ뵒???뚯씪 ?섏떊???깃났?덉뒿?덈떎.",
                mapper.toResponse(result));
    }
}

