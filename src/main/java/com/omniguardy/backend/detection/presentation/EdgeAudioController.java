package com.omniguardy.backend.detection.presentation;

import com.omniguardy.backend.detection.application.model.AudioReceipt;
import com.omniguardy.backend.detection.application.model.MediaFile;
import com.omniguardy.backend.detection.application.usecase.ReceiveAudioUseCase;
import com.omniguardy.backend.domain.edge.dto.response.AudioUploadResponseDto;
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

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AudioUploadResponseDto> uploadAudio(@RequestPart("file") MultipartFile file) throws IOException {
        MediaFile media = new MediaFile(file == null ? null : file.getOriginalFilename(),
                file == null ? null : file.getContentType(), file == null ? null : file.getBytes());
        AudioReceipt result = receiveAudioUseCase.receive(media);
        return ApiResponse.success("오디오 파일 수신에 성공했습니다.",
                new AudioUploadResponseDto(result.eventId(), result.filename(), result.size(), result.status(), result.probability()));
    }
}
