package com.omniguardy.backend.securityevent.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgentAssessmentTest {
    @Test void acceptsBoundaryScores() {
        assertEquals(0, new AgentAssessment(RiskLevel.LOW, 0, "근거").riskScore());
        assertEquals(100, new AgentAssessment(RiskLevel.CRITICAL, 100, "근거").riskScore());
    }
    @Test void rejectsOutOfRangeScore() {
        assertThrows(IllegalArgumentException.class, () -> new AgentAssessment(RiskLevel.HIGH, 101, "근거"));
    }
}
