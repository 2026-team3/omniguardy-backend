package com.omniguardy.backend.domain.ai.service;

import com.omniguardy.backend.domain.ai.dto.VisionPredictResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class VisionAiService {

    private final RestClient restClient;

    @Value("${ai.fastapi.vision.base-url}")
    private String baseUrl;

    @Value("${ai.fastapi.vision.endpoint}")
    private String endpoint;

    public VisionAiService() {
        this.restClient = RestClient.create();
    }

    public VisionPredictResponseDto analyze(MultipartFile file) {

        try {

            MultipartBodyBuilder bodyBuilder =
                    new MultipartBodyBuilder();

            ByteArrayResource videoResource =
                    new ByteArrayResource(file.getBytes()) {

                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    };

            bodyBuilder
                    .part("file", videoResource)
                    .contentType(MediaType.parseMediaType("video/mp4"));

            System.out.println(
                    "[VISION AI] 요청 URL: "
                            + baseUrl
                            + endpoint
            );

            System.out.println(
                    "[VISION AI] 파일명: "
                            + file.getOriginalFilename()
            );

            System.out.println(
                    "[VISION AI] 파일 크기: "
                            + file.getSize()
            );

            VisionPredictResponseDto response =
                    restClient
                            .post()
                            .uri(baseUrl + endpoint)
                            .contentType(
                                    MediaType.MULTIPART_FORM_DATA
                            )
                            .body(bodyBuilder.build())
                            .retrieve()
                            .body(VisionPredictResponseDto.class);

            if (response == null) {
                throw new RuntimeException(
                        "Vision AI 응답이 비어 있습니다."
                );
            }

            return response;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Vision AI 전송을 위한 영상 파일 처리에 실패했습니다.",
                    e
            );
        }
    }
}
