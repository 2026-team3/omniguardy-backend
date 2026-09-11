package com.omniguardy.backend.detection.application.usecase;

import com.omniguardy.backend.detection.application.model.MediaFile;
import com.omniguardy.backend.detection.application.model.VideoReceipt;
import com.omniguardy.backend.detection.application.model.VisionAnalysis;
import com.omniguardy.backend.detection.application.port.out.MediaStoragePort;
import com.omniguardy.backend.detection.application.port.out.VisionAnalysisPort;
import com.omniguardy.backend.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.securityevent.domain.repository.SecurityEventRepository;
import com.omniguardy.backend.securityevent.application.usecase.AnalyzeAgentRiskUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ReceiveVideoUseCase {
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;
    private final VisionAnalysisPort visionAnalysisPort;
    private final MediaStoragePort mediaStoragePort;
    private final SecurityEventRepository securityEventRepository;
    private final AnalyzeAgentRiskUseCase analyzeAgentRiskUseCase;
    private final ObjectMapper objectMapper;

    public VideoReceipt receive(MediaFile file, String eventId) {
        if (file == null || file.bytes().length == 0) throw new IllegalArgumentException("영상 파일이 비어 있습니다.");
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId는 필수입니다.");
        if (file.size() > MAX_VIDEO_SIZE) throw new IllegalArgumentException("영상 파일은 100MB 이하만 가능합니다.");
        SecurityEvent event = securityEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 eventId입니다: " + eventId));
        try {
            MediaStoragePort.StoredMedia stored = mediaStoragePort.saveVideo(eventId, file);
            event.updateVideoPath(stored.path());
            securityEventRepository.save(event);
            VisionAnalysis vision = visionAnalysisPort.analyze(file);
            event.updateVisionResult(vision.prediction(), vision.confidence(),
                    objectMapper.writeValueAsString(vision.classProbabilities()), vision.personCount(),
                    vision.videoDurationSeconds(), vision.analyzedAt());
            securityEventRepository.save(event);
            analyzeAgentRiskUseCase.analyze(eventId);
            return new VideoReceipt(eventId, stored.filename(), vision);
        } catch (RuntimeException exception) {
            event.fail();
            securityEventRepository.save(event);
            throw exception;
        }
    }
}
