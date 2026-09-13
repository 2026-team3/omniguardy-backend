package com.omniguardy.backend.domain.tts.infrastructure.delivery;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import com.omniguardy.backend.domain.tts.infrastructure.mqtt.dto.TtsAudioMessage;
import com.omniguardy.backend.domain.tts.application.port.out.TtsDeliveryPort;
import com.omniguardy.backend.domain.tts.domain.error.TtsErrorCode;
import com.omniguardy.backend.global.error.exception.BusinessException;
import com.omniguardy.backend.global.mqtt.MqttConfig.MqttGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TtsService implements TtsDeliveryPort {

    private final MqttGateway mqttGateway;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.topic.tts}")
    private String ttsTopic;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.tts.audio-dir}")
    private String audioDir;

    @Value("${app.tts.audio-url-prefix}")
    private String audioUrlPrefix;

    public void synthesizeAndPublish(String message) {
        try {
            String fileName = UUID.randomUUID() + ".mp3";

            Path dirPath = Path.of(audioDir);
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(fileName);

            byte[] audioBytes = synthesizeSpeech(message);
            Files.write(filePath, audioBytes);

            String audioUrl = baseUrl + audioUrlPrefix + "/" + fileName;

            TtsAudioMessage mqttMessage = new TtsAudioMessage("TTS_AUDIO", audioUrl);
            String payload = objectMapper.writeValueAsString(mqttMessage);

            mqttGateway.sendToMqtt(payload, ttsTopic);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(TtsErrorCode.DELIVERY_FAILED, e);
        }
    }

    private byte[] synthesizeSpeech(String text) throws Exception {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("ko-KR")
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(
                    input,
                    voice,
                    audioConfig
            );

            ByteString audioContents = response.getAudioContent();
            return audioContents.toByteArray();
        }
    }
}

