package com.omniguardy.backend.domain.detection.application.usecase;

import com.omniguardy.backend.domain.detection.application.model.MediaFile;
import com.omniguardy.backend.domain.detection.application.model.VideoReceipt;
import com.omniguardy.backend.domain.detection.application.model.VideoTriggerContext;
import com.omniguardy.backend.domain.detection.application.model.VisionAnalysis;
import com.omniguardy.backend.domain.detection.application.port.in.ReceiveVideoInputPort;
import com.omniguardy.backend.domain.detection.application.port.out.MediaStoragePort;
import com.omniguardy.backend.domain.detection.application.port.out.VisionAnalysisPort;
import com.omniguardy.backend.domain.detection.domain.error.DetectionErrorCode;
import com.omniguardy.backend.domain.securityevent.application.usecase.AnalyzeAgentRiskUseCase;
import com.omniguardy.backend.domain.securityevent.domain.error.SecurityEventErrorCode;
import com.omniguardy.backend.domain.securityevent.domain.model.SecurityEvent;
import com.omniguardy.backend.domain.securityevent.domain.repository.SecurityEventRepository;
import com.omniguardy.backend.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import java.time.OffsetDateTime;
import com.omniguardy.backend.domain.detection.application.model.TriggerType;
import com.omniguardy.backend.domain.securityevent.application.port.in.RecordKeypadVisionInputPort;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ReceiveVideoUseCase implements ReceiveVideoInputPort {

    private static final long MAX_VIDEO_SIZE =
            100L * 1024 * 1024;

    private final VisionAnalysisPort visionAnalysisPort;
    private final MediaStoragePort mediaStoragePort;

    // 기존 AUDIO 흐름 유지
    private final SecurityEventRepository securityEventRepository;
    private final AnalyzeAgentRiskUseCase analyzeAgentRiskUseCase;

    private final ObjectMapper objectMapper;
    private final RecordKeypadVisionInputPort recordKeypadVisionInputPort;

    @Override
    public VideoReceipt receive(
            MediaFile file,
            VideoTriggerContext triggerContext
    ) {

        validateFile(file);

        if (triggerContext == null
                || triggerContext.triggerType() == null) {

            throw new IllegalArgumentException(
                    "triggerType is required"
            );
        }

        return switch (triggerContext.triggerType()) {

            case AUDIO ->
                    receiveAudioTriggeredVideo(
                            file,
                            triggerContext
                    );

            case KEYPAD ->
                    receiveKeypadTriggeredVideo(
                            file,
                            triggerContext
                    );
        };
    }

    // ============================================
    // AUDIO
    // ============================================

    private VideoReceipt receiveAudioTriggeredVideo(
            MediaFile file,
            VideoTriggerContext triggerContext
    ) {

        String eventId =
                resolveAudioEventId(triggerContext);

        if (eventId == null || eventId.isBlank()) {

            throw new BusinessException(
                    DetectionErrorCode.EVENT_ID_REQUIRED
            );
        }

        SecurityEvent event =
                securityEventRepository
                        .findByEventId(eventId)
                        .orElseThrow(
                                () -> new BusinessException(
                                        SecurityEventErrorCode
                                                .SECURITY_EVENT_NOT_FOUND
                                )
                        );

        // Legacy uploads only carry eventId. Reuse the trigger recorded before camera execution.
        event.connectAudioTrigger(
                eventId,
                triggerContext.triggeredAt() == null || triggerContext.triggeredAt().isBlank()
                        ? null : OffsetDateTime.parse(triggerContext.triggeredAt()));
        VideoTriggerContext audioContext = new VideoTriggerContext(
                eventId, event.getTriggerId(), TriggerType.AUDIO, eventId,
                event.getTriggeredAt() == null ? null : event.getTriggeredAt().toString());

        try {

            MediaStoragePort.StoredMedia stored =
                    mediaStoragePort.saveVideo(
                            eventId,
                            file
                    );

            event.updateVideoPath(
                    stored.path()
            );

            securityEventRepository.save(event);

            /*
             * 수정:
             * file만 보내지 않고 Trigger 정보도 전달.
             */
            VisionAnalysis vision =
                    visionAnalysisPort.analyze(
                            file,
                            audioContext
                    );

            event.updateVisionResult(
                    vision.prediction(),
                    vision.confidence(),
                    objectMapper.writeValueAsString(
                            vision.classProbabilities()
                    ),
                    objectMapper.writeValueAsString(vision.visionEvents()),
                    vision.personCount(),
                    vision.videoDurationSeconds(),
                    vision.analyzedAt()
            );

            securityEventRepository.save(event);

            if (!vision.hasSilentSignal()) {
                analyzeAgentRiskUseCase.analyze(eventId);
            }

            return new VideoReceipt(
                    eventId,
                    stored.filename(),
                    vision
            );

        } catch (RuntimeException exception) {

            event.fail();

            securityEventRepository.save(event);

            throw exception;
        }
    }

    // ============================================
    // KEYPAD
    // ============================================

    private VideoReceipt receiveKeypadTriggeredVideo(
            MediaFile file,
            VideoTriggerContext triggerContext
    ) {

        String triggerId =
                triggerContext.triggerId();

        if (triggerId == null
                || triggerId.isBlank()) {

            throw new IllegalArgumentException(
                    "triggerId is required for KEYPAD trigger"
            );
        }

        /*
         * KEYPAD 자체는 SecurityEvent가 아니다.
         */
        MediaStoragePort.StoredMedia stored =
                mediaStoragePort.saveVideo(
                        triggerId,
                        file
                );

        /*
         * 수정:
         * Trigger 정보도 Vision으로 전달.
         */
        VisionAnalysis vision =
                visionAnalysisPort.analyze(
                        file,
                        triggerContext
                );

        if (!vision.hasVisionEvents()) {
            return new VideoReceipt(null, stored.filename(), vision);
        }

        OffsetDateTime triggeredAt = triggerContext.triggeredAt() == null
                || triggerContext.triggeredAt().isBlank()
                ? vision.triggeredAt() : OffsetDateTime.parse(triggerContext.triggeredAt());
        String eventId = recordKeypadVisionInputPort.record(new RecordKeypadVisionInputPort.Command(
                triggerId, triggeredAt, stored.path(), vision.prediction(), vision.confidence(),
                objectMapper.writeValueAsString(vision.classProbabilities()),
                objectMapper.writeValueAsString(vision.visionEvents()), vision.personCount(),
                vision.videoDurationSeconds(), vision.analyzedAt()));

        // SILENT_SIGNAL action handling is a separate, subsequent implementation.
        if (!vision.hasSilentSignal()) {
            analyzeAgentRiskUseCase.analyze(eventId);
        }
        return new VideoReceipt(eventId, stored.filename(), vision);
    }

    // ============================================
    // Validation
    // ============================================

    private void validateFile(
            MediaFile file
    ) {

        if (file == null
                || file.bytes().length == 0) {

            throw new BusinessException(
                    DetectionErrorCode.EMPTY_VIDEO_FILE
            );
        }

        if (file.size() > MAX_VIDEO_SIZE) {

            throw new BusinessException(
                    DetectionErrorCode.VIDEO_FILE_TOO_LARGE
            );
        }
    }

    // ============================================
    // 기존 AUDIO 호환
    // ============================================

    private String resolveAudioEventId(
            VideoTriggerContext triggerContext
    ) {

        if (triggerContext.securityEventId() != null
                && !triggerContext.securityEventId().isBlank()) {

            return triggerContext.securityEventId();
        }

        return triggerContext.eventId();
    }
}

