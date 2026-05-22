package com.omniguardy.backend.domain.tts.dto.mqtt;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TtsAudioMessage {

    private String type;
    private String audioUrl;
}
