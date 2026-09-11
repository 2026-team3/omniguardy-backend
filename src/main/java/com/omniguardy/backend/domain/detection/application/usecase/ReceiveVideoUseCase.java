package com.omniguardy.backend.domain.detection.application.usecase;

import com.omniguardy.backend.domain.detection.application.model.MediaFile;
import com.omniguardy.backend.domain.detection.application.model.VideoReceipt;
import com.omniguardy.backend.domain.detection.application.model.VisionAnalysis;
import com.omniguardy.backend.domain.detection.application.port.out.MediaStoragePort;
import com.omniguardy.backend.domain.detection.application.port.out.VisionAnalysisPort;
import com.omniguardy.backend.domain.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.domain.securityevent.domain.repository.SecurityEventRepository;
import com.omniguardy.backend.domain.securityevent.application.usecase.AnalyzeAgentRiskUseCase;
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
        if (file == null || file.bytes().length == 0) throw new IllegalArgumentException("?곸긽 ?뚯씪??鍮꾩뼱 ?덉뒿?덈떎.");
        if (eventId == null || eventId.isBlank()) throw new IllegalArgumentException("eventId???꾩닔?낅땲??");
        if (file.size() > MAX_VIDEO_SIZE) throw new IllegalArgumentException("?곸긽 ?뚯씪? 100MB ?댄븯留?媛?ν빀?덈떎.");
        SecurityEvent event = securityEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalArgumentException("議댁옱?섏? ?딅뒗 eventId?낅땲?? " + eventId));
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

