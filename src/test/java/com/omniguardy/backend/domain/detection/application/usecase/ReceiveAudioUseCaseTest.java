package com.omniguardy.backend.domain.detection.application.usecase;

import com.omniguardy.backend.domain.detection.application.model.AudioAnalysis;
import com.omniguardy.backend.domain.detection.application.model.MediaFile;
import com.omniguardy.backend.domain.detection.application.port.out.AudioAnalysisPort;
import com.omniguardy.backend.domain.detection.application.port.out.CameraCommandPort;
import com.omniguardy.backend.domain.detection.application.port.out.MediaStoragePort;
import com.omniguardy.backend.domain.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.domain.securityevent.domain.repository.SecurityEventRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReceiveAudioUseCaseTest {
    private final AudioAnalysisPort audio = mock(AudioAnalysisPort.class);
    private final MediaStoragePort storage = mock(MediaStoragePort.class);
    private final CameraCommandPort camera = mock(CameraCommandPort.class);
    private final SecurityEventRepository repository = mock(SecurityEventRepository.class);
    private final ReceiveAudioUseCase useCase = new ReceiveAudioUseCase(audio, storage, camera, repository);
    private final MediaFile file = new MediaFile("chunk.wav", "audio/wav", new byte[]{1, 2, 3});

    @Test void normalDoesNotCreateEventOrStartCamera() {
        when(audio.analyze(any())).thenReturn(new AudioAnalysis("normal", 0.1));
        var receipt = useCase.receive(file);
        assertNull(receipt.eventId());
        verifyNoInteractions(storage, camera, repository);
    }

    @Test void abnormalCreatesUuidEventAndStartsCamera() {
        when(audio.analyze(any())).thenReturn(new AudioAnalysis("abnormal", 0.999342));
        when(storage.saveAudio(anyString(), any())).thenAnswer(invocation ->
                new MediaStoragePort.StoredMedia(invocation.getArgument(0) + "_chunk.wav", "./uploads/audio/file"));
        var receipt = useCase.receive(file);
        assertDoesNotThrow(() -> java.util.UUID.fromString(receipt.eventId()));
        verify(repository).save(argThat(event -> receipt.eventId().equals(event.getEventId())
                && receipt.eventId().equals(event.getTriggerId())
                && event.getTriggerType() == com.omniguardy.backend.domain.securityevent.domain.model.EventTriggerType.AUDIO
                && event.getTriggeredAt() != null
                && "CAMERA_REQUESTED".equals(event.getStatus())));
        verify(camera).startCamera(receipt.eventId());
    }

    @Test void secondAbnormalInsideCooldownCreatesNothing() {
        when(audio.analyze(any())).thenReturn(new AudioAnalysis("abnormal", 0.9));
        when(storage.saveAudio(anyString(), any())).thenReturn(new MediaStoragePort.StoredMedia("a.wav", "p"));
        assertNotNull(useCase.receive(file).eventId());
        assertNull(useCase.receive(file).eventId());
        verify(camera, times(1)).startCamera(anyString());
        verify(repository, times(1)).save(any(SecurityEvent.class));
    }
}

