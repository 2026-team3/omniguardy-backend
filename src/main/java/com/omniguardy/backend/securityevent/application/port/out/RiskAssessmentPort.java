package com.omniguardy.backend.securityevent.application.port.out;

import com.omniguardy.backend.securityevent.domain.model.AgentAssessment;
import com.omniguardy.backend.securityevent.domain.model.AgentContext;

public interface RiskAssessmentPort {
    AgentAssessment assess(AgentContext context);
}
