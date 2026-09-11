package com.omniguardy.backend.detection.infrastructure.fastapi;

import com.omniguardy.backend.detection.application.model.AudioAnalysis;
import com.omniguardy.backend.detection.application.model.MediaFile;
import com.omniguardy.backend.detection.application.port.out.AudioAnalysisPort;
import com.omniguardy.backend.domain.ai.dto.AudioPredictResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AudioFastApiAdapter implements AudioAnalysisPort {
    private final RestClient restClient = RestClient.create();
    @Value("${ai.fastapi.audio.base-url}") private String baseUrl;
    @Value("${ai.fastapi.audio.endpoint}") private String endpoint;

    @Override
    public AudioAnalysis analyze(MediaFile file) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", resource(file)).contentType(MediaType.parseMediaType("audio/wav"));
        AudioPredictResponseDto response = restClient.post().uri(baseUrl + endpoint)
                .contentType(MediaType.MULTIPART_FORM_DATA).body(body.build()).retrieve()
                .body(AudioPredictResponseDto.class);
        if (response == null) throw new IllegalStateException("Audio AI 응답이 비어 있습니다.");
        return new AudioAnalysis(response.getStatus(), response.getProbability());
    }

    private ByteArrayResource resource(MediaFile file) {
        return new ByteArrayResource(file.bytes()) {
            @Override public String getFilename() { return file.originalFilename(); }
        };
    }
}
