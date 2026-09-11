package com.omniguardy.backend.domain.securityevent.domain.model;

public record AgentAssessment(RiskLevel riskLevel, int riskScore, String reason) {
    public AgentAssessment {
        if (riskLevel == null) throw new IllegalArgumentException("riskLevel is required");
        if (riskScore < 0 || riskScore > 100) throw new IllegalArgumentException("riskScore must be between 0 and 100");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason is required");
    }
}

