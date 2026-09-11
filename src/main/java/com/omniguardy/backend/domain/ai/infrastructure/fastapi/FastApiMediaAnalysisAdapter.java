package com.omniguardy.backend.domain.ai.infrastructure.fastapi;

import com.omniguardy.backend.domain.ai.application.model.AnalysisResult;
import com.omniguardy.backend.domain.ai.application.port.out.MediaAnalysisPort;
import com.omniguardy.backend.domain.ai.infrastructure.fastapi.dto.FastApiAudioResponse;
import com.omniguardy.backend.domain.ai.infrastructure.fastapi.dto.FastApiVisionResponse;
import com.omniguardy.backend.domain.detection.application.model.MediaFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class FastApiMediaAnalysisAdapter implements MediaAnalysisPort {
    private final RestClient visionClient;
    private final RestClient audioClient;
    private final String visionEndpoint;
    private final String audioEndpoint;
    private final String visionBaseUrl;

    public FastApiMediaAnalysisAdapter(RestClient.Builder builder,
            @Value("${ai.fastapi.vision.base-url}") String visionBaseUrl,
            @Value("${ai.fastapi.vision.endpoint}") String visionEndpoint,
            @Value("${ai.fastapi.audio.base-url}") String audioBaseUrl,
            @Value("${ai.fastapi.audio.endpoint}") String audioEndpoint) {
        this.visionClient = builder.clone().baseUrl(visionBaseUrl).build();
        this.audioClient = builder.clone().baseUrl(audioBaseUrl).build();
        this.visionBaseUrl = visionBaseUrl;
        this.visionEndpoint = visionEndpoint;
        this.audioEndpoint = audioEndpoint;
    }

    @Override
    public AnalysisResult analyze(MediaFile file) {
        FastApiVisionResponse vision = visionClient.post().uri(visionEndpoint)
                .header("ngrok-skip-browser-warning", "true")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(multipart(file)).retrieve()
                .body(FastApiVisionResponse.class);
        FastApiAudioResponse audio = audioClient.post().uri(audioEndpoint)
                .header("ngrok-skip-browser-warning", "true")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(multipart(file)).retrieve()
                .body(FastApiAudioResponse.class);
        if (vision == null || vision.getResult() == null || !"success".equals(vision.getStatus())) {
            throw new IllegalStateException("Vision FastAPI 분석 결과가 올바르지 않습니다.");
        }
        if (audio == null) throw new IllegalStateException("Audio FastAPI 응답이 비어 있습니다.");
        FastApiVisionResponse.Result value = vision.getResult();
        return new AnalysisResult(
                new AnalysisResult.VisionResult(value.getModule(), value.getVideo(), value.getEvents(),
                        value.getRiskScore(), value.getRiskLevel(), accessibleUrl(value.getAnnotatedVideo())),
                new AnalysisResult.AudioResult(audio.getStatus(), audio.getProbability()));
    }

    private MultiValueMap<String, HttpEntity<?>> multipart(MediaFile file) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        ByteArrayResource resource = new ByteArrayResource(file.bytes()) {
            @Override public String getFilename() { return file.originalFilename(); }
        };
        MediaType type = file.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(file.contentType());
        body.part("file", resource).contentType(type);
        return body.build();
    }

    private String accessibleUrl(String path) {
        if (path == null || path.isBlank() || path.startsWith("http://") || path.startsWith("https://")) return path;
        return visionBaseUrl + (path.startsWith("/") ? path : "/" + path);
    }
}
