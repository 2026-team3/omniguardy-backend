package com.omniguardy.backend.domain.detection.application.port.out;

import com.omniguardy.backend.domain.detection.application.model.CameraStartCommand;

public interface CameraCommandPort {

    // 기존 Audio Trigger용
    void startCamera(String eventId);

    // 신규 Keypad Trigger용
    void startCamera(CameraStartCommand command);
}