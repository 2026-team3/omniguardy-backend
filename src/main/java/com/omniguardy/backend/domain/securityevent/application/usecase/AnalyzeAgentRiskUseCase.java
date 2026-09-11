package com.omniguardy.backend.domain.securityevent.application.usecase;

import com.omniguardy.backend.domain.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.domain.securityevent.domain.repository.SecurityEventRepository;
import com.omniguardy.backend.domain.securityevent.application.port.out.RiskAssessmentPort;
import com.omniguardy.backend.domain.securityevent.domain.model.AgentAssessment;
import com.omniguardy.backend.domain.securityevent.domain.model.AgentContext;
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
                .orElseThrow(() -> new IllegalArgumentException("議댁옱?섏? ?딅뒗 eventId?낅땲?? " + eventId));
        if (!event.isVisionAnalyzed()) {
            throw new IllegalStateException("Agent 遺꾩꽍? VISION_ANALYZED ?곹깭?먯꽌留?媛?ν빀?덈떎.");
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
            throw new IllegalStateException("classProbabilities瑜?Agent Context濡?蹂?섑븷 ???놁뒿?덈떎.", exception);
        }
    }

    private double value(Double value) { return value == null ? 0.0 : value; }
}

