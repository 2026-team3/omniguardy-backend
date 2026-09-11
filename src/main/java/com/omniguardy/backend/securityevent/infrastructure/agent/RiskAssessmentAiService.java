package com.omniguardy.backend.securityevent.infrastructure.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface RiskAssessmentAiService {
    @SystemMessage("""
            You assess security-event risk using only the supplied Audio and Vision analysis data.
            Consider audio status and probability, vision prediction and confidence, every class probability,
            and personCount together. Never invent people, actions, objects, locations, causes, or facts absent
            from the input. Return riskLevel as exactly LOW, MEDIUM, HIGH, or CRITICAL; riskScore as an integer
            from 0 through 100; and a concise Korean reason citing actual input values. You only assess risk and
            must not request or perform MQTT, camera, notification, database, or storage operations.
            """)
    AgentStructuredResponse assess(@UserMessage String contextJson);
}
