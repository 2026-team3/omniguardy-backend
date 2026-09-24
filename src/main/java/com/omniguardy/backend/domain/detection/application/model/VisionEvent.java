package com.omniguardy.backend.domain.detection.application.model;

import java.util.Map;

public record VisionEvent(
        String eventType,
        Double confidence,
        Integer detectedFrame,
        Map<String, Object> details
) {

    public boolean isSilentSignal() {
        return "SILENT_SIGNAL".equalsIgnoreCase(eventType);
    }
}