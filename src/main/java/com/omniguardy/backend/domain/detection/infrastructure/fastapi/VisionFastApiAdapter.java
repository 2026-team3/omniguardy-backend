package com.omniguardy.backend.domain.detection.infrastructure.fastapi;

import com.omniguardy.backend.domain.detection.application.model.*;
import com.omniguardy.backend.domain.detection.application.port.out.VisionAnalysisPort;
import com.omniguardy.backend.domain.detection.infrastructure.fastapi.dto.VisionApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class VisionFastApiAdapter implements VisionAnalysisPort {

    private final RestClient restClient =
            RestClient.create();

    @Value("${ai.fastapi.vision.base-url}")
    private String baseUrl;

    @Value("${ai.fastapi.vision.endpoint}")
    private String endpoint;

    @Override
    public VisionAnalysis analyze(
            MediaFile file,
            VideoTriggerContext triggerContext
    ) {

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        // 영상
        body.add(
                "file",
                resource(file)
        );

        // Trigger ID
        body.add(
                "triggerId",
                triggerContext.triggerId()
        );

        // AUDIO / KEYPAD
        body.add(
                "triggerType",
                triggerContext.triggerType().name()
        );

        // 촬영 트리거 발생 시각
        body.add(
                "triggeredAt",
                triggerContext.triggeredAt()
        );

        /*
         * AUDIO:
         * securityEventId 존재
         *
         * KEYPAD:
         * securityEventId 없음
         */
        if (triggerContext.securityEventId() != null
                && !triggerContext.securityEventId().isBlank()) {

            body.add(
                    "securityEventId",
                    triggerContext.securityEventId()
            );
        }

        VisionApiResponse response =
                restClient.post()

                        // 중요
                        .uri(baseUrl + endpoint)

                        .contentType(
                                MediaType.MULTIPART_FORM_DATA
                        )

                        .body(body)

                        .retrieve()

                        .body(
                                VisionApiResponse.class
                        );

        if (response == null
                || response.result() == null) {

            throw new IllegalStateException(
                    "Vision API response is empty"
            );
        }

        return toVisionAnalysis(
                response
        );
    }

    private VisionAnalysis toVisionAnalysis(
            VisionApiResponse response
    ) {

        VisionApiResponse.Result result =
                response.result();

        // ==============================
        // Behavior
        // KEYPAD에서는 null 가능
        // ==============================

        VisionBehavior behavior = null;

        if (result.behavior() != null) {

            behavior =
                    new VisionBehavior(
                            result.behavior().prediction(),
                            result.behavior().behaviorName(),
                            result.behavior().confidence(),
                            result.behavior()
                                    .classProbabilities()
                    );
        }

        // ==============================
        // Video
        // ==============================

        VisionVideoInfo video = null;

        if (result.video() != null) {

            video =
                    new VisionVideoInfo(
                            result.video().fileName(),
                            result.video()
                                    .durationSeconds(),
                            result.video().fps(),
                            result.video().frameCount()
                    );
        }

        // ==============================
        // Observations
        // ==============================

        VisionObservations observations = null;

        if (result.observations() != null) {

            observations =
                    new VisionObservations(
                            result.observations()
                                    .trackedPersonCount(),

                            result.observations()
                                    .hasTracking(),

                            result.observations()
                                    .hasPose()
                    );
        }

        // ==============================
        // Vision Events
        // ==============================

        List<VisionEvent> visionEvents =
                result.visionEvents() == null
                        ? List.of()
                        : result.visionEvents()
                        .stream()
                        .map(event ->
                                new VisionEvent(
                                        event.eventType(),
                                        event.confidence(),
                                        event.detectedFrame(),
                                        event.details()
                                )
                        )
                        .toList();

        return new VisionAnalysis(
                result.trigger().triggerId(),

                TriggerType.valueOf(
                        result.trigger()
                                .triggerType()
                                .toUpperCase()
                ),

                result.trigger()
                        .securityEventId(),

                result.trigger()
                        .triggeredAt(),

                result.analyzedAt(),

                video,
                behavior,
                visionEvents,
                observations
        );
    }

    private ByteArrayResource resource(
            MediaFile file
    ) {

        return new ByteArrayResource(
                file.bytes()
        ) {

            @Override
            public String getFilename() {

                return file.originalFilename();
            }
        };
    }
}