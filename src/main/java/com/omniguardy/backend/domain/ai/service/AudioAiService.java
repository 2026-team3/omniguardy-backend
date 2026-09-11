package com.omniguardy.backend.domain.ai.service;

import com.omniguardy.backend.domain.ai.dto.AudioPredictResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AudioAiService {

    private final RestClient restClient;

    @Value("${ai.fastapi.audio.base-url}")
    private String baseUrl;

    @Value("${ai.fastapi.audio.endpoint}")
    private String endpoint;

    public AudioAiService() {
        this.restClient = RestClient.create();
    }

    public AudioPredictResponseDto analyze(MultipartFile file) {

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

            ByteArrayResource audioResource =
                    new ByteArrayResource(file.getBytes()) {

                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };

            bodyBuilder
                    .part("file", audioResource)
                    .contentType(MediaType.parseMediaType("audio/wav"));

            System.out.println("[AUDIO AI] 요청 URL: " + baseUrl + endpoint);
            System.out.println("[AUDIO AI] 파일명: " + file.getOriginalFilename());
            System.out.println("[AUDIO AI] 파일 크기: " + file.getSize());

            AudioPredictResponseDto response =
                    restClient
                            .post()
                            .uri(baseUrl + endpoint)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(bodyBuilder.build())
                            .retrieve()
                            .body(AudioPredictResponseDto.class);

            if (response == null) {
                throw new RuntimeException(
                        "Audio AI 응답이 비어 있습니다."
                );
            }

            System.out.println("[AUDIO AI] 분석 완료");
            System.out.println("[AUDIO AI] status: " + response.getStatus());
            System.out.println("[AUDIO AI] probability: " + response.getProbability());

            return response;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Audio AI 전송을 위한 파일 처리에 실패했습니다.",
                    e
            );
        }
    }
}
