package com.omniguardy.backend.domain.securityevent.domain.model;

import com.omniguardy.backend.domain.securityevent.domain.error.SecurityEventErrorCode;
import com.omniguardy.backend.global.error.exception.BusinessException;
import java.util.Map;

public record AgentContext(String eventId, Audio audio, Vision vision) {
    public AgentContext {
        if (eventId == null || eventId.isBlank()) throw new BusinessException(SecurityEventErrorCode.INVALID_AGENT_CONTEXT);
        if (audio == null || vision == null) throw new BusinessException(SecurityEventErrorCode.INVALID_AGENT_CONTEXT);
    }

    public record Audio(String status, double probability) {}
    public record Vision(String prediction, double confidence,
                         Map<String, Double> classProbabilities, int personCount) {
        public Vision {
            classProbabilities = classProbabilities == null ? Map.of() : Map.copyOf(classProbabilities);
        }
    }
}

