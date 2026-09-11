package com.omniguardy.backend.domain.edge.service;

import com.omniguardy.backend.domain.ai.dto.AudioPredictResponseDto;
import com.omniguardy.backend.domain.ai.service.AudioAiService;
import com.omniguardy.backend.domain.edge.dto.response.AudioUploadResponseDto;
import com.omniguardy.backend.domain.securityevent.entity.SecurityEvent;
import com.omniguardy.backend.domain.securityevent.repository.SecurityEventRepository;
import com.omniguardy.backend.global.mqtt.MqttConfig.MqttGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EdgeAudioService {

    private static final String AUDIO_DIR =
            "./uploads/audio";

    // 카메라 촬영 10초 + 약간의 여유시간
    private static final long CAMERA_COOLDOWN_MS =
            12_000L;

    // 마지막으로 카메라 실행 이벤트를 발생시킨 시간
    private long lastCameraTriggeredAt = 0L;

    private final MqttGateway mqttGateway;
    private final AudioAiService audioAiService;
    private final SecurityEventRepository securityEventRepository;

    @Value("${mqtt.topic.camera}")
    private String cameraTopic;


    @Transactional
    public AudioUploadResponseDto receiveAudio(
            MultipartFile file
    ) {

        // =========================
        // 1. 파일 검증
        // =========================

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "오디오 파일이 비어 있습니다."
            );
        }


        String originalFilename =
                file.getOriginalFilename();

        if (originalFilename == null ||
                originalFilename.isBlank()) {

            originalFilename =
                    "audio.wav";
        }


        System.out.println(
                "=============================="
        );

        System.out.println(
                "[EDGE AUDIO]"
        );

        System.out.println(
                "오디오 파일 수신"
        );

        System.out.println(
                "파일명: " + originalFilename
        );

        System.out.println(
                "크기: " + file.getSize()
        );


        // =========================
        // 2. Audio AI 분석
        // =========================

        System.out.println(
                "[AUDIO AI] 분석 요청"
        );


        AudioPredictResponseDto audioResult =
                audioAiService.analyze(file);


        System.out.println(
                "[AUDIO AI] 분석 결과"
        );

        System.out.println(
                "status: "
                        + audioResult.getStatus()
        );

        System.out.println(
                "probability: "
                        + audioResult.getProbability()
        );


        // =========================
        // 3. normal
        // =========================

        if (!"abnormal".equalsIgnoreCase(
                audioResult.getStatus()
        )) {

            System.out.println(
                    "[AUDIO AI] 정상 오디오"
            );

            System.out.println(
                    "[SECURITY EVENT] 생성 안 함"
            );

            System.out.println(
                    "[MQTT] 카메라 실행 안 함"
            );

            System.out.println(
                    "=============================="
            );


            return new AudioUploadResponseDto(
                    null,
                    originalFilename,
                    file.getSize(),
                    audioResult.getStatus(),
                    audioResult.getProbability()
            );
        }


        // =========================
        // 4. abnormal
        // 카메라 cooldown 확인
        // =========================

        long now =
                System.currentTimeMillis();


        synchronized (this) {

            long elapsed =
                    now - lastCameraTriggeredAt;


            if (elapsed < CAMERA_COOLDOWN_MS) {

                long remainingMs =
                        CAMERA_COOLDOWN_MS
                                - elapsed;

                double remainingSeconds =
                        remainingMs / 1000.0;


                System.out.println(
                        "[AUDIO AI] abnormal 감지"
                );

                System.out.println(
                        "[SECURITY EVENT] "
                                + "카메라 촬영 중 또는 cooldown 상태"
                );

                System.out.println(
                        "[SECURITY EVENT] "
                                + "중복 이벤트 생성 안 함"
                );

                System.out.println(
                        "[SECURITY EVENT] "
                                + "남은 cooldown: "
                                + remainingSeconds
                                + "초"
                );

                System.out.println(
                        "=============================="
                );


                return new AudioUploadResponseDto(
                        null,
                        originalFilename,
                        file.getSize(),
                        audioResult.getStatus(),
                        audioResult.getProbability()
                );
            }


            // 이번 abnormal을 하나의 SecurityEvent로 인정
            lastCameraTriggeredAt = now;
        }


        // =========================
        // 5. eventId 생성
        // =========================

        String eventId =
                UUID.randomUUID().toString();


        System.out.println(
                "[SECURITY EVENT] "
                        + "새로운 보안 이벤트 감지"
        );

        System.out.println(
                "eventId: " + eventId
        );


        // =========================
        // 6. abnormal 오디오 저장
        // =========================

        String savedFilename =
                saveAudio(
                        eventId,
                        originalFilename,
                        file
                );


        String audioPath =
                Paths.get(
                        AUDIO_DIR,
                        savedFilename
                ).toString();


        // =========================
        // 7. SecurityEvent DB 저장
        // =========================

        SecurityEvent event =
                SecurityEvent.builder()
                        .eventId(eventId)

                        // 사용자 연결은
                        // Device 인증 구현 후 추가
                        // .user(user)

                        .audioStatus(
                                audioResult.getStatus()
                        )

                        .audioProbability(
                                audioResult.getProbability()
                        )

                        .audioPath(
                                audioPath
                        )

                        .status(
                                "AUDIO_DETECTED"
                        )

                        .build();


        securityEventRepository.save(event);


        System.out.println(
                "[SECURITY EVENT] "
                        + "DB 저장 완료"
        );

        System.out.println(
                "eventId: " + eventId
        );

        System.out.println(
                "audioPath: " + audioPath
        );


        // =========================
        // 8. MQTT 카메라 실행
        // eventId 같이 전달
        // =========================

        String cameraCommand = """
                {
                  "type": "CAMERA_START",
                  "duration": 10,
                  "eventId": "%s"
                }
                """.formatted(eventId);


        mqttGateway.sendToMqtt(
                cameraCommand,
                cameraTopic
        );


        // =========================
        // 9. Event 상태 변경
        // =========================

        event.updateStatus(
                "CAMERA_REQUESTED"
        );


        System.out.println(
                "[AUDIO AI] 위험 신호 감지"
        );

        System.out.println(
                "[MQTT] CAMERA_START 발행 -> "
                        + cameraTopic
        );

        System.out.println(
                "[MQTT] eventId: "
                        + eventId
        );

        System.out.println(
                "[SECURITY EVENT] status: "
                        + event.getStatus()
        );

        System.out.println(
                "=============================="
        );


        // =========================
        // 10. Raspberry Pi 응답
        // =========================

        return new AudioUploadResponseDto(
                eventId,
                savedFilename,
                file.getSize(),
                audioResult.getStatus(),
                audioResult.getProbability()
        );
    }


    // =========================
    // abnormal 오디오 저장
    // =========================

    private String saveAudio(
            String eventId,
            String originalFilename,
            MultipartFile file
    ) {

        try {

            Path uploadDir =
                    Paths.get(AUDIO_DIR);

            Files.createDirectories(
                    uploadDir
            );


            String filename =
                    eventId
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


            System.out.println(
                    "[EDGE AUDIO] "
                            + "abnormal 오디오 저장 완료"
            );

            System.out.println(
                    "저장 위치: "
                            + savePath.toAbsolutePath()
            );


            return filename;


        } catch (IOException e) {

            throw new RuntimeException(
                    "오디오 파일 저장 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}
