package com.omniguardy.backend.domain.securityevent.infrastructure.agent;

import com.omniguardy.backend.global.error.exception.BusinessException;

import com.omniguardy.backend.domain.securityevent.domain.model.AgentContext;
import com.omniguardy.backend.domain.securityevent.domain.model.RiskLevel;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class LangChainRiskAssessmentAdapterTest {
    @Test void parsesAndValidatesStructuredOutput() {
        RiskAssessmentAiService service = mock(RiskAssessmentAiService.class);
        AgentStructuredResponse response = new AgentStructuredResponse();
        response.riskLevel = "HIGH";
        response.riskScore = 85;
        response.reason = "abnormal 0.99 諛?A18 0.46??洹쇨굅";
        when(service.assess(contains("classProbabilities"))).thenReturn(response);
        var adapter = new LangChainRiskAssessmentAdapter(service, new ObjectMapper());
        var result = adapter.assess(new AgentContext("event", new AgentContext.Audio("abnormal", 0.99),
                new AgentContext.Vision("A18", 0.46, Map.of("A18", 0.46), 0)));
        assertEquals(RiskLevel.HIGH, result.riskLevel());
        assertEquals(85, result.riskScore());
    }

    @Test void rejectsInvalidRiskScore() {
        RiskAssessmentAiService service = mock(RiskAssessmentAiService.class);
        AgentStructuredResponse response = new AgentStructuredResponse();
        response.riskLevel = "HIGH";
        response.riskScore = 101;
        response.reason = "洹쇨굅";
        when(service.assess(anyString())).thenReturn(response);
        var adapter = new LangChainRiskAssessmentAdapter(service, new ObjectMapper());
        assertThrows(BusinessException.class, () -> adapter.assess(new AgentContext("event",
                new AgentContext.Audio("abnormal", 1), new AgentContext.Vision("A18", 1, Map.of(), 0))));
    }
}

