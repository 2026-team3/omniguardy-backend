package com.omniguardy.backend.detection.infrastructure.mqtt;

import com.omniguardy.backend.detection.application.port.out.CameraCommandPort;
import com.omniguardy.backend.global.mqtt.MqttConfig.MqttGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MqttCameraCommandAdapter implements CameraCommandPort {
    private final MqttGateway mqttGateway;
    @Value("${mqtt.topic.camera}") private String cameraTopic;

    @Override public void startCamera(String eventId) {
        String payload = """
                {
                  "type": "CAMERA_START",
                  "duration": 10,
                  "eventId": "%s"
                }
                """.formatted(eventId);
        mqttGateway.sendToMqtt(payload, cameraTopic);
    }
}
