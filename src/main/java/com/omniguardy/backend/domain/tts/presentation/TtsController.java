package com.omniguardy.backend.domain.tts.presentation;

import com.omniguardy.backend.domain.tts.presentation.dto.request.TtsRequestDto;
import com.omniguardy.backend.domain.tts.application.usecase.PublishTtsUseCase;
import com.omniguardy.backend.domain.tts.presentation.mapper.TtsPresentationMapper;
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

    private final PublishTtsUseCase publishTtsUseCase;
    private final TtsPresentationMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> publishTts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TtsRequestDto requestDto
    ) {
        publishTtsUseCase.publish(mapper.toCommand(requestDto));

        return ResponseEntity.ok(
                ApiResponse.success("TTS 硫붿떆吏媛 ?꾩넚?섏뿀?듬땲??", null)
        );
    }
}

