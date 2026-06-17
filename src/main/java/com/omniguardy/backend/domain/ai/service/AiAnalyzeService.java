package com.omniguardy.backend.domain.ai.service;

import com.omniguardy.backend.domain.ai.dto.*;
import com.omniguardy.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AiAnalyzeService {

    @Value("${ai.fastapi.vision.base-url}")
    private String visionBaseUrl;

    @Value("${ai.fastapi.vision.endpoint}")
    private String visionEndpoint;

    @Value("${ai.fastapi.audio.base-url}")
    private String audioBaseUrl;

    @Value("${ai.fastapi.audio.endpoint}")
    private String audioEndpoint;

    public AiAnalyzeResponse analyze(MultipartFile file, User user) {
        try {
            byte[] fileBytes = file.getBytes();
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();

            VisionAnalyzeResponse visionResult = callVisionFastApi(
                    fileBytes,
                    filename,
                    contentType
            );

            AudioAnalyzeResponse audioResult = callAudioFastApi(
                    fileBytes,
                    filename,
                    contentType
            );

            return AiAnalyzeResponse.builder()
                    .vision(visionResult)
                    .audio(audioResult)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("영상 파일 처리 중 오류가 발생했습니다.", e);
        }
    }

    private VisionAnalyzeResponse callVisionFastApi(
            byte[] fileBytes,
            String filename,
            String contentType
    ) {
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(visionBaseUrl)
                    .build();

            FastApiVisionResponse fastApiResponse = restClient.post()
                    .uri(visionEndpoint)
                    .header("ngrok-skip-browser-warning", "true")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(createMultipartBody(fileBytes, filename, contentType))
                    .retrieve()
                    .body(FastApiVisionResponse.class);

            if (fastApiResponse == null) {
                throw new RuntimeException("Vision FastAPI 응답이 비어 있습니다.");
            }

            if (!"success".equals(fastApiResponse.getStatus())) {
                throw new RuntimeException("Vision FastAPI 분석 요청에 실패했습니다.");
            }

            if (fastApiResponse.getResult() == null) {
                throw new RuntimeException("Vision FastAPI 분석 결과가 비어 있습니다.");
            }

            FastApiVisionResponse.Result result = fastApiResponse.getResult();

            String annotatedVideoUrl = toAccessibleVideoUrl(result.getAnnotatedVideo());

            return VisionAnalyzeResponse.builder()
                    .module(result.getModule())
                    .video(result.getVideo())
                    .events(result.getEvents())
                    .riskScore(result.getRiskScore())
                    .riskLevel(result.getRiskLevel())
                    .annotatedVideo(annotatedVideoUrl)
                    .build();

        } catch (RestClientException e) {
            throw new RuntimeException("Vision FastAPI 호출 중 오류가 발생했습니다.", e);
        }
    }

    private AudioAnalyzeResponse callAudioFastApi(
            byte[] fileBytes,
            String filename,
            String contentType
    ) {
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(audioBaseUrl)
                    .build();

            FastApiAudioResponse fastApiResponse = restClient.post()
                    .uri(audioEndpoint)
                    .header("ngrok-skip-browser-warning", "true")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(createMultipartBody(fileBytes, filename, contentType))
                    .retrieve()
                    .body(FastApiAudioResponse.class);

            if (fastApiResponse == null) {
                throw new RuntimeException("Audio FastAPI 응답이 비어 있습니다.");
            }

            return AudioAnalyzeResponse.builder()
                    .status(fastApiResponse.getStatus())
                    .probability(fastApiResponse.getProbability())
                    .build();

        } catch (RestClientException e) {
            throw new RuntimeException("Audio FastAPI 호출 중 오류가 발생했습니다.", e);
        }
    }

    private MultiValueMap<String, HttpEntity<?>> createMultipartBody(
            byte[] fileBytes,
            String filename,
            String contentType
    ) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (contentType != null) {
            mediaType = MediaType.parseMediaType(contentType);
        }

        bodyBuilder.part("file", fileResource)
                .contentType(mediaType);

        return bodyBuilder.build();
    }

    private String toAccessibleVideoUrl(String annotatedVideo) {
        if (annotatedVideo == null || annotatedVideo.isBlank()) {
            return null;
        }

        if (annotatedVideo.startsWith("http://") || annotatedVideo.startsWith("https://")) {
            return annotatedVideo;
        }

        if (annotatedVideo.startsWith("/")) {
            return visionBaseUrl + annotatedVideo;
        }

        return visionBaseUrl + "/" + annotatedVideo;
    }
}