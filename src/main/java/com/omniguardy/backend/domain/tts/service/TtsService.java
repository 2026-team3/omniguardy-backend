package com.omniguardy.backend.domain.tts.service;

import com.omniguardy.backend.global.exception.CustomException;
import com.omniguardy.backend.global.exception.ErrorCode;
import com.omniguardy.backend.global.mqtt.MqttConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TtsService {

    private final MqttConfig.MqttGateway mqttGateway;

    @Value("${mqtt.topic.tts}")
    private String ttsTopic;

    public void publishTtsMessage(String message) {
        try {
            mqttGateway.sendToMqtt(message, ttsTopic);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MQTT_PUBLISH_FAILED);
        }
    }
}
