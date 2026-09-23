package com.omniguardy.backend.domain.detection.application.usecase;

import com.omniguardy.backend.domain.detection.application.model.AudioAnalysis;
import com.omniguardy.backend.domain.detection.application.model.AudioReceipt;
import com.omniguardy.backend.domain.detection.application.model.MediaFile;
import com.omniguardy.backend.domain.detection.application.port.out.AudioAnalysisPort;
import com.omniguardy.backend.domain.detection.application.port.out.CameraCommandPort;
import com.omniguardy.backend.domain.detection.application.port.out.MediaStoragePort;
import com.omniguardy.backend.domain.detection.domain.error.DetectionErrorCode;
import com.omniguardy.backend.global.error.exception.BusinessException;
import com.omniguardy.backend.domain.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.domain.securityevent.domain.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.OffsetDateTime;
import com.omniguardy.backend.domain.detection.application.port.in.ReceiveAudioInputPort;

@Service
@RequiredArgsConstructor
public class ReceiveAudioUseCase implements ReceiveAudioInputPort {
    private static final long CAMERA_COOLDOWN_MS = 12_000L;
    private long lastCameraTriggeredAt;

    private final AudioAnalysisPort audioAnalysisPort;
    private final MediaStoragePort mediaStoragePort;
    private final CameraCommandPort cameraCommandPort;
    private final SecurityEventRepository securityEventRepository;

    @Transactional
    public AudioReceipt receive(MediaFile file) {
        if (file == null || file.bytes().length == 0) throw new BusinessException(DetectionErrorCode.EMPTY_AUDIO_FILE);
        String original = file.originalFilename() == null || file.originalFilename().isBlank()
                ? "audio.wav" : file.originalFilename();
        AudioAnalysis analysis = audioAnalysisPort.analyze(file);
        if (!analysis.isAbnormal() || isCoolingDown()) {
            return new AudioReceipt(null, original, file.size(), analysis.status(), analysis.probability());
        }

        String eventId = UUID.randomUUID().toString();
        MediaStoragePort.StoredMedia stored = mediaStoragePort.saveAudio(eventId, file);
        SecurityEvent event = SecurityEvent.builder().eventId(eventId).audioStatus(analysis.status())
                .audioProbability(analysis.probability()).audioPath(stored.path()).status("AUDIO_DETECTED").build();
        event.connectAudioTrigger(eventId, OffsetDateTime.now());
        securityEventRepository.save(event);
        cameraCommandPort.startCamera(eventId);
        event.markCameraRequested();
        return new AudioReceipt(eventId, stored.filename(), file.size(), analysis.status(), analysis.probability());
    }

    private synchronized boolean isCoolingDown() {
        long now = System.currentTimeMillis();
        if (now - lastCameraTriggeredAt < CAMERA_COOLDOWN_MS) return true;
        lastCameraTriggeredAt = now;
        return false;
    }
}

