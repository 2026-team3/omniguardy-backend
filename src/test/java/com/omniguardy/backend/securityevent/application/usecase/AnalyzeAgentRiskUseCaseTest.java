package com.omniguardy.backend.securityevent.application.usecase;

import com.omniguardy.backend.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.securityevent.domain.repository.SecurityEventRepository;
import com.omniguardy.backend.securityevent.application.port.out.RiskAssessmentPort;
import com.omniguardy.backend.securityevent.domain.model.AgentAssessment;
import com.omniguardy.backend.securityevent.domain.model.AgentContext;
import com.omniguardy.backend.securityevent.domain.model.RiskLevel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyzeAgentRiskUseCaseTest {
    private final SecurityEventRepository repository = mock(SecurityEventRepository.class);
    private final RiskAssessmentPort port = mock(RiskAssessmentPort.class);
    private final AnalyzeAgentRiskUseCase useCase = new AnalyzeAgentRiskUseCase(repository, port, new ObjectMapper());

    @Test void includesAudioAndAllVisionDataAndCompletesExistingEvent() {
        SecurityEvent event = visionAnalyzedEvent();
        when(repository.findByEventId("event-1")).thenReturn(Optional.of(event));
        when(port.assess(any())).thenReturn(new AgentAssessment(RiskLevel.HIGH, 85, "audio와 vision 값에 근거"));
        useCase.analyze("event-1");
        ArgumentCaptor<AgentContext> captor = ArgumentCaptor.forClass(AgentContext.class);
        verify(port).assess(captor.capture());
        assertEquals("abnormal", captor.getValue().audio().status());
        assertEquals(0.999342, captor.getValue().audio().probability());
        assertEquals("A18", captor.getValue().vision().prediction());
        assertEquals(0.464551, captor.getValue().vision().confidence());
        assertEquals(0.222806, captor.getValue().vision().classProbabilities().get("A17"));
        assertEquals(0, captor.getValue().vision().personCount());
        assertEquals("HIGH", event.getRiskLevel());
        assertEquals("COMPLETED", event.getStatus());
    }

    @Test void failurePreservesEvidenceAndMarksFailed() {
        SecurityEvent event = visionAnalyzedEvent();
        when(repository.findByEventId("event-1")).thenReturn(Optional.of(event));
        when(port.assess(any())).thenThrow(new IllegalStateException("OpenAI failure"));
        assertThrows(IllegalStateException.class, () -> useCase.analyze("event-1"));
        assertEquals("abnormal", event.getAudioStatus());
        assertEquals("A18", event.getVisionPrediction());
        assertEquals("FAILED", event.getStatus());
        verify(repository).save(event);
    }

    private SecurityEvent visionAnalyzedEvent() {
        SecurityEvent event = SecurityEvent.builder().eventId("event-1").audioStatus("abnormal")
                .audioProbability(0.999342).status("CAMERA_REQUESTED").build();
        event.updateVisionResult("A18", 0.464551, "{\"N1\":0.035310,\"A17\":0.222806}",
                0, 10.0, "2026-09-11T00:00:00Z");
        return event;
    }
}
