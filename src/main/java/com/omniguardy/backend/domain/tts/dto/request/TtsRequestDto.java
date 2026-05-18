package com.omniguardy.backend.domain.tts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TtsRequestDto {

    @NotBlank(message = "출력할 문구는 필수입니다.")
    @Size(max = 100, message = "TTS 문구는 100자 이하로 입력해주세요.")
    private String message;
}