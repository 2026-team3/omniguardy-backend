package com.omniguardy.backend.securityevent.application.usecase;

import com.omniguardy.backend.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.securityevent.domain.repository.SecurityEventRepository;
import com.omniguardy.backend.securityevent.application.port.out.RiskAssessmentPort;
import com.omniguardy.backend.securityevent.domain.model.AgentAssessment;
import com.omniguardy.backend.securityevent.domain.model.AgentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyzeAgentRiskUseCase {
    private final SecurityEventRepository securityEventRepository;
    private final RiskAssessmentPort riskAssessmentPort;
    private final ObjectMapper objectMapper;

    public AgentAssessment analyze(String eventId) {
        SecurityEvent event = securityEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 eventId입니다: " + eventId));
        if (!event.isVisionAnalyzed()) {
            throw new IllegalStateException("Agent 분석은 VISION_ANALYZED 상태에서만 가능합니다.");
        }
        try {
            AgentAssessment assessment = riskAssessmentPort.assess(toContext(event));
            event.updateAgentResult(assessment.riskLevel().name(), assessment.reason());
            securityEventRepository.save(event);
            return assessment;
        } catch (RuntimeException exception) {
            event.fail();
            securityEventRepository.save(event);
            throw exception;
        }
    }

    private AgentContext toContext(SecurityEvent event) {
        return new AgentContext(event.getEventId(),
                new AgentContext.Audio(event.getAudioStatus(), value(event.getAudioProbability())),
                new AgentContext.Vision(event.getVisionPrediction(), value(event.getVisionConfidence()),
                        probabilities(event.getClassProbabilities()),
                        event.getPersonCount() == null ? 0 : event.getPersonCount()));
    }

    private Map<String, Double> probabilities(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (JacksonException exception) {
            throw new IllegalStateException("classProbabilities를 Agent Context로 변환할 수 없습니다.", exception);
        }
    }

    private double value(Double value) { return value == null ? 0.0 : value; }
}
