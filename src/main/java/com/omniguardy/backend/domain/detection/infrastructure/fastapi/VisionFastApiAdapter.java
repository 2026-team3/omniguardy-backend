package com.omniguardy.backend.domain.detection.infrastructure.fastapi;

import com.omniguardy.backend.domain.detection.application.model.MediaFile;
import com.omniguardy.backend.domain.detection.application.model.VisionAnalysis;
import com.omniguardy.backend.domain.detection.application.port.out.VisionAnalysisPort;
import com.omniguardy.backend.domain.detection.infrastructure.fastapi.dto.VisionPredictResponseDto;
import com.omniguardy.backend.domain.detection.infrastructure.fastapi.dto.VisionResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VisionFastApiAdapter implements VisionAnalysisPort {
    private final RestClient restClient = RestClient.create();
    @Value("${ai.fastapi.vision.base-url}") private String baseUrl;
    @Value("${ai.fastapi.vision.endpoint}") private String endpoint;

    @Override
    public VisionAnalysis analyze(MediaFile file) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", resource(file)).contentType(MediaType.parseMediaType("video/mp4"));
        VisionPredictResponseDto response = restClient.post().uri(baseUrl + endpoint)
                .contentType(MediaType.MULTIPART_FORM_DATA).body(body.build()).retrieve()
                .body(VisionPredictResponseDto.class);
        if (response == null) throw new IllegalStateException("Vision AI ?묐떟??鍮꾩뼱 ?덉뒿?덈떎.");
        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getResult() == null) {
            throw new IllegalStateException("Vision AI 遺꾩꽍 ?ㅽ뙣: " + response.getMessage() + " (" + response.getCode() + ")");
        }
        VisionResultDto result = response.getResult();
        return new VisionAnalysis(result.getPrediction(), result.getConfidence(), result.getClassProbabilities(),
                result.getPersonCount(), result.getVideoDurationSeconds(), result.getAnalyzedAt());
    }

    private ByteArrayResource resource(MediaFile file) {
        return new ByteArrayResource(file.bytes()) {
            @Override public String getFilename() { return file.originalFilename(); }
        };
    }
}


