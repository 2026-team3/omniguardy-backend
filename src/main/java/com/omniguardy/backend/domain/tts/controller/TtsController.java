package com.omniguardy.backend.domain.tts.controller;

import com.omniguardy.backend.domain.tts.dto.request.TtsRequestDto;
import com.omniguardy.backend.domain.tts.service.TtsService;
import com.omniguardy.backend.global.response.ApiResponse;
import com.omniguardy.backend.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tts")
public class TtsController {

    private final TtsService ttsService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> publishTts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TtsRequestDto requestDto
    ) {
        ttsService.publishTtsMessage(requestDto.getMessage());

        return ResponseEntity.ok(
                ApiResponse.success("TTS 메시지가 전송되었습니다.", null)
        );
    }
}
