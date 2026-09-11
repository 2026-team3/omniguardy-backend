package com.omniguardy.backend.domain.securityevent.infrastructure.agent;

import com.omniguardy.backend.domain.securityevent.application.port.out.RiskAssessmentPort;
import com.omniguardy.backend.domain.securityevent.domain.model.AgentAssessment;
import com.omniguardy.backend.domain.securityevent.domain.model.AgentContext;
import com.omniguardy.backend.domain.securityevent.domain.model.RiskLevel;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class LangChainRiskAssessmentAdapter implements RiskAssessmentPort {
    private final RiskAssessmentAiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public AgentAssessment assess(AgentContext context) {
        try {
            AgentStructuredResponse response = aiService.assess(objectMapper.writeValueAsString(context));
            if (response == null) throw new IllegalStateException("Agent AI ?묐떟??鍮꾩뼱 ?덉뒿?덈떎.");
            return new AgentAssessment(RiskLevel.valueOf(response.riskLevel), response.riskScore, response.reason);
        } catch (JacksonException | IllegalArgumentException exception) {
            throw new IllegalStateException("Agent AI Structured Output???щ컮瑜댁? ?딆뒿?덈떎.", exception);
        }
    }
}

