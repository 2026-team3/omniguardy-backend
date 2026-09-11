package com.omniguardy.backend.detection.application.usecase;

import com.omniguardy.backend.detection.application.model.MediaFile;
import com.omniguardy.backend.detection.application.model.VisionAnalysis;
import com.omniguardy.backend.detection.application.port.out.MediaStoragePort;
import com.omniguardy.backend.detection.application.port.out.VisionAnalysisPort;
import com.omniguardy.backend.securityevent.application.usecase.AnalyzeAgentRiskUseCase;
import com.omniguardy.backend.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.securityevent.domain.repository.SecurityEventRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReceiveVideoUseCaseTest {
    @Test void updatesExistingEventWithVisionAndInvokesAgentUsingSameEventId() {
        VisionAnalysisPort visionPort = mock(VisionAnalysisPort.class);
        MediaStoragePort storage = mock(MediaStoragePort.class);
        SecurityEventRepository repository = mock(SecurityEventRepository.class);
        AnalyzeAgentRiskUseCase agent = mock(AnalyzeAgentRiskUseCase.class);
        ReceiveVideoUseCase useCase = new ReceiveVideoUseCase(visionPort, storage, repository, agent, new ObjectMapper());
        SecurityEvent event = SecurityEvent.builder().eventId("same-event").status("CAMERA_REQUESTED").build();
        when(repository.findByEventId("same-event")).thenReturn(Optional.of(event));
        when(storage.saveVideo(eq("same-event"), any())).thenReturn(new MediaStoragePort.StoredMedia(
                "same-event_uuid_video.mp4", "./uploads/video/same-event_uuid_video.mp4"));
        when(visionPort.analyze(any())).thenReturn(new VisionAnalysis("A18", 0.464551,
                Map.of("N1", 0.035310, "A17", 0.222806, "A18", 0.464551), 0, 10.0, "now"));

        var receipt = useCase.receive(new MediaFile("video.mp4", "video/mp4", new byte[]{1}), "same-event");

        assertEquals("same-event", receipt.eventId());
        assertEquals("A18", event.getVisionPrediction());
        assertTrue(event.getClassProbabilities().contains("A17"));
        assertEquals("VISION_ANALYZED", event.getStatus());
        verify(agent).analyze("same-event");
        verify(repository, atLeast(2)).save(same(event));
    }

    @Test void rejectsUnknownEventWithoutCreatingOne() {
        SecurityEventRepository repository = mock(SecurityEventRepository.class);
        when(repository.findByEventId("missing")).thenReturn(Optional.empty());
        ReceiveVideoUseCase useCase = new ReceiveVideoUseCase(mock(VisionAnalysisPort.class),
                mock(MediaStoragePort.class), repository, mock(AnalyzeAgentRiskUseCase.class), new ObjectMapper());
        assertThrows(IllegalArgumentException.class,
                () -> useCase.receive(new MediaFile("video.mp4", "video/mp4", new byte[]{1}), "missing"));
        verify(repository, never()).save(any());
    }
}
