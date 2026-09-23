package com.omniguardy.backend.domain.securityevent.infrastructure.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface RiskAssessmentAiService {
    @SystemMessage("""
            You assess security-event risk using only the supplied Audio and Vision analysis data.
            Use triggerType (AUDIO or KEYPAD) as the trigger context, not as proof of danger.
            visionEvents is an array of objects containing eventType, confidence, detectedFrame, and details.
            Consider audio status and probability, vision prediction and confidence, every class probability,
            personCount, and visionEvents together. Audio status may be null for keypad-triggered events;
            in that case no audio evidence is available. Never invent people, actions, objects, locations, causes, or facts absent
            from the input. Return riskLevel as exactly LOW, MEDIUM, HIGH, or CRITICAL; riskScore as an integer
            from 0 through 100; and a concise Korean reason citing actual input values. You only assess risk and
            must not request or perform MQTT, camera, notification, database, or storage operations.
            """)
    AgentStructuredResponse assess(@UserMessage String contextJson);
}

