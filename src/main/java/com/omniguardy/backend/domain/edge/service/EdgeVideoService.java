package com.omniguardy.backend.domain.edge.service;

import com.omniguardy.backend.domain.ai.dto.VisionPredictResponseDto;
import com.omniguardy.backend.domain.ai.dto.VisionResultDto;
import com.omniguardy.backend.domain.ai.service.VisionAiService;
import com.omniguardy.backend.domain.edge.dto.response.VideoUploadResponseDto;
import com.omniguardy.backend.domain.securityevent.entity.SecurityEvent;
import com.omniguardy.backend.domain.securityevent.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EdgeVideoService {

    private static final String VIDEO_DIR =
            "./uploads/video";

    private static final long MAX_VIDEO_SIZE =
            100L * 1024 * 1024;


    private final VisionAiService visionAiService;

    private final SecurityEventRepository securityEventRepository;

    private final ObjectMapper objectMapper;


    public VideoUploadResponseDto receiveVideo(
            MultipartFile file,
            String eventId
    ) {

        // =========================
        // 1. 요청 검증
        // =========================

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "영상 파일이 비어 있습니다."
            );
        }


        if (eventId == null ||
                eventId.isBlank()) {

            throw new IllegalArgumentException(
                    "eventId는 필수입니다."
            );
        }


        if (file.getSize() > MAX_VIDEO_SIZE) {

            throw new IllegalArgumentException(
                    "영상 파일은 100MB 이하만 가능합니다."
            );
        }


        // =========================
        // 2. Audio 때 만들어진 Event 조회
        // =========================

        SecurityEvent event =
                securityEventRepository
                        .findByEventId(eventId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "존재하지 않는 "
                                                        + "eventId입니다: "
                                                        + eventId
                                        )
                        );


        System.out.println(
                "=============================="
        );

        System.out.println(
                "[EDGE VIDEO]"
        );

        System.out.println(
                "영상 파일 수신"
        );

        System.out.println(
                "eventId: "
                        + eventId
        );

        System.out.println(
                "원본 파일명: "
                        + file.getOriginalFilename()
        );

        System.out.println(
                "크기: "
                        + file.getSize()
        );


        String filename;


        try {

            // =========================
            // 3. 영상 로컬 저장
            // =========================

            Path uploadDir =
                    Paths.get(VIDEO_DIR);


            Files.createDirectories(
                    uploadDir
            );


            String originalFilename =
                    file.getOriginalFilename();


            if (originalFilename == null ||
                    originalFilename.isBlank()) {

                originalFilename =
                        "video.mp4";
            }


            filename =
                    eventId
                            + "_"
                            + UUID.randomUUID()
                            + "_"
                            + originalFilename;


            Path savePath =
                    uploadDir.resolve(
                            filename
                    );


            Files.copy(
                    file.getInputStream(),
                    savePath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            String videoPath =
                    savePath.toString();


            // 같은 SecurityEvent에 영상 경로 저장
            event.updateVideoPath(
                    videoPath
            );


            securityEventRepository.save(
                    event
            );


            System.out.println(
                    "[EDGE VIDEO] 로컬 저장 완료"
            );

            System.out.println(
                    "저장 위치: "
                            + savePath.toAbsolutePath()
            );

            System.out.println(
                    "[SECURITY EVENT] "
                            + "videoPath DB 저장 완료"
            );


        } catch (IOException e) {

            event.fail();

            securityEventRepository.save(
                    event
            );


            throw new RuntimeException(
                    "영상 파일 저장 중 오류가 발생했습니다.",
                    e
            );
        }


        try {

            // =========================
            // 4. Vision AI 분석
            // =========================

            System.out.println(
                    "[VISION AI] 분석 요청"
            );

            System.out.println(
                    "[VISION AI] eventId: "
                            + eventId
            );


            VisionPredictResponseDto response =
                    visionAiService.analyze(
                            file
                    );


            // =========================
            // 5. Vision 실패 응답 확인
            // =========================

            if (!"success".equalsIgnoreCase(
                    response.getStatus()
            )) {

                event.fail();

                securityEventRepository.save(
                        event
                );


                throw new RuntimeException(
                        "Vision AI 분석 실패: "
                                + response.getMessage()
                                + " ("
                                + response.getCode()
                                + ")"
                );
            }


            VisionResultDto result =
                    response.getResult();


            if (result == null) {

                event.fail();

                securityEventRepository.save(
                        event
                );


                throw new RuntimeException(
                        "Vision AI 분석 결과가 없습니다."
                );
            }


            // =========================
            // 6. classProbabilities
            // Map → JSON String
            // =========================

            String probabilitiesJson;

            try {

                probabilitiesJson =
                        objectMapper.writeValueAsString(
                                result.getClassProbabilities()
                        );

            } catch (JacksonException e) {

                event.fail();

                securityEventRepository.save(
                        event
                );

                throw new RuntimeException(
                        "Vision 확률 데이터 변환에 실패했습니다.",
                        e
                );
            }


            // =========================
            // 7. 동일 SecurityEvent에
            // Vision 결과 UPDATE
            // =========================

            event.updateVisionResult(

                    result.getPrediction(),

                    result.getConfidence(),

                    probabilitiesJson,

                    result.getPersonCount(),

                    result.getVideoDurationSeconds(),

                    result.getAnalyzedAt()
            );


            securityEventRepository.save(
                    event
            );


            // =========================
            // 8. 로그
            // =========================

            System.out.println(
                    "[VISION AI] 분석 완료"
            );

            System.out.println(
                    "[VISION AI] eventId: "
                            + eventId
            );

            System.out.println(
                    "prediction: "
                            + result.getPrediction()
            );

            System.out.println(
                    "confidence: "
                            + result.getConfidence()
            );

            System.out.println(
                    "personCount: "
                            + result.getPersonCount()
            );

            System.out.println(
                    "duration: "
                            + result.getVideoDurationSeconds()
            );

            System.out.println(
                    "classProbabilities: "
                            + result.getClassProbabilities()
            );


            System.out.println(
                    "[SECURITY EVENT] "
                            + "Audio + Video + Vision 연결 완료"
            );

            System.out.println(
                    "[SECURITY EVENT] status: "
                            + event.getStatus()
            );

            System.out.println(
                    "=============================="
            );


            // =========================
            // 9. Raspberry Pi 응답
            // =========================

            return new VideoUploadResponseDto(
                    eventId,
                    filename,
                    result
            );


        } catch (RuntimeException e) {

            // 이미 FAILED라면 그대로,
            // 아니더라도 실패 상태로 변경
            event.fail();

            securityEventRepository.save(
                    event
            );

            throw e;
        }
    }
}
