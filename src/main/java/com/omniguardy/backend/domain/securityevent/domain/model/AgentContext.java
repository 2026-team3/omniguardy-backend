package com.omniguardy.backend.domain.securityevent.domain.model;

import java.util.Map;

public record AgentContext(String eventId, Audio audio, Vision vision) {
    public AgentContext {
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId is required");
        if (audio == null || vision == null) throw new IllegalArgumentException("audio and vision are required");
    }

    public record Audio(String status, double probability) {}
    public record Vision(String prediction, double confidence,
                         Map<String, Double> classProbabilities, int personCount) {
        public Vision {
            classProbabilities = classProbabilities == null ? Map.of() : Map.copyOf(classProbabilities);
        }
    }
}

