package com.omniguardy.backend.domain.ai.service;

import com.omniguardy.backend.domain.ai.dto.FastApiVisionResponse;
import com.omniguardy.backend.domain.ai.dto.VisionAnalyzeResponse;
import com.omniguardy.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AiVisionService {

    @Value("${ai.fastapi.base-url}")
    private String fastApiBaseUrl;

    public VisionAnalyzeResponse analyzeVision(MultipartFile file, User user) {
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(fastApiBaseUrl)
                    .build();

            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MediaType fileContentType = MediaType.APPLICATION_OCTET_STREAM;

            if (file.getContentType() != null) {
                fileContentType = MediaType.parseMediaType(file.getContentType());
            }

            bodyBuilder.part("file", fileResource)
                    .contentType(fileContentType);

            FastApiVisionResponse fastApiResponse = restClient.post()
                    .uri("/analyze/vision")
                    .header("ngrok-skip-browser-warning", "true")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(bodyBuilder.build())
                    .retrieve()
                    .body(FastApiVisionResponse.class);

            if (fastApiResponse == null) {
                throw new RuntimeException("FastAPI 응답이 비어 있습니다.");
            }

            if (!"success".equals(fastApiResponse.getStatus())) {
                throw new RuntimeException("FastAPI 분석 요청에 실패했습니다.");
            }

            if (fastApiResponse.getResult() == null) {
                throw new RuntimeException("FastAPI 분석 결과가 비어 있습니다.");
            }

            FastApiVisionResponse.Result result = fastApiResponse.getResult();

            return VisionAnalyzeResponse.builder()
                    .video(result.getVideo())
                    .events(result.getEvents())
                    .riskScore(result.getRiskScore())
                    .riskLevel(result.getRiskLevel())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("영상 파일 처리 중 오류가 발생했습니다.", e);
        } catch (RestClientException e) {
            throw new RuntimeException("AI 서버 호출 중 오류가 발생했습니다.", e);
        }
    }
}