package com.omniguardy.backend.domain.edge.service;

import com.omniguardy.backend.domain.edge.dto.response.AudioUploadResponseDto;
import com.omniguardy.backend.global.mqtt.MqttConfig.MqttGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    private static final String AUDIO_DIR = "./uploads/audio";

    private final MqttGateway mqttGateway;

    @Value("${mqtt.topic.camera}")
    private String cameraTopic;


    public AudioUploadResponseDto receiveAudio(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("오디오 파일이 비어 있습니다.");
        }

        try {

            Path uploadDir = Paths.get(AUDIO_DIR);

            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();

            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "audio.wav";
            }

            String filename =
                    UUID.randomUUID()
                            + "_"
                            + originalFilename;

            Path savePath = uploadDir.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    savePath,
                    StandardCopyOption.REPLACE_EXISTING
            );


            System.out.println("==============================");
            System.out.println("[EDGE AUDIO]");
            System.out.println("오디오 파일 수신 완료");
            System.out.println("파일명: " + filename);
            System.out.println("크기: " + file.getSize());
            System.out.println(
                    "저장 위치: "
                            + savePath.toAbsolutePath()
            );
            System.out.println("==============================");


            /*
             * =========================================
             * 현재는 Audio AI 모델이 완성되지 않았기 때문에
             * 테스트를 위해 오디오가 들어오면
             * 무조건 위험 상황이라고 가정한다.
             *
             * 나중에는 이 부분을
             *
             * Audio AI 호출
             *      ↓
             * 위험 여부 확인
             *      ↓
             * 위험일 때만 CAMERA_START
             *
             * 로 변경하면 된다.
             * =========================================
             */

            String cameraCommand = """
                    {
                      "type": "CAMERA_START",
                      "duration": 10
                    }
                    """;

            mqttGateway.sendToMqtt(
                    cameraCommand,
                    cameraTopic
            );

            System.out.println(
                    "[TEST] Audio 위험 감지로 가정"
            );

            System.out.println(
                    "[MQTT] CAMERA_START 발행 -> "
                            + cameraTopic
            );

            return new AudioUploadResponseDto(
                    filename,
                    file.getSize()
            );


        } catch (IOException e) {

            throw new RuntimeException(
                    "오디오 파일 저장 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}
