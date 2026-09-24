package com.omniguardy.backend.domain.detection.infrastructure.fastapi;

import com.omniguardy.backend.domain.detection.application.model.*;
import com.omniguardy.backend.domain.detection.domain.error.DetectionErrorCode;
import com.omniguardy.backend.global.error.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class VisionFastApiAdapterTest {
    private final VisionFastApiAdapter adapter = new VisionFastApiAdapter();
    private final MediaFile file = new MediaFile("video.mp4", "video/mp4", new byte[]{1});
    private final VideoTriggerContext context = new VideoTriggerContext(
            "event", "original-trigger", TriggerType.AUDIO, "event", "2026-09-23T12:00:00+09:00");
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(adapter, "restClient", builder.build());
        ReflectionTestUtils.setField(adapter, "baseUrl", "http://vision.test");
        ReflectionTestUtils.setField(adapter, "endpoint", "/analyze");
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", """
            {"triggerId":"different", "triggerType":"INVALID", "securityEventId":"other",
             "triggeredAt":"2020-01-01T00:00:00Z"}
            """})
    void usesContextWhilePreservingAnalysisResults(String trigger) {
        respond("""
                {"result":{"trigger":%s,"analyzedAt":"2026-09-23T12:01:00+09:00",
                  "behavior":{"prediction":"A18","behaviorName":"test","confidence":0.9,
                              "classProbabilities":{"A18":0.9}},
                  "video":{"fileName":"video.mp4","durationSeconds":3.0,"fps":30.0,"frameCount":90},
                  "observations":{"trackedPersonCount":2,"hasTracking":true,"hasPose":true},
                  "visionEvents":[{"eventType":"SILENT_SIGNAL","confidence":0.8,"detectedFrame":5,"details":{}}]}}
                """.formatted(trigger));

        VisionAnalysis analysis = adapter.analyze(file, context);

        assertEquals(context.triggerId(), analysis.triggerId());
        assertEquals(context.triggerType(), analysis.triggerType());
        assertEquals(context.securityEventId(), analysis.securityEventId());
        assertEquals(OffsetDateTime.parse(context.triggeredAt()), analysis.triggeredAt());
        assertEquals(OffsetDateTime.parse("2026-09-23T12:01:00+09:00").toInstant(),
                analysis.analyzedAt().toInstant());
        assertEquals("A18", analysis.prediction());
        assertEquals(0.9, analysis.confidence());
        assertEquals(Map.of("A18", 0.9), analysis.classProbabilities());
        assertEquals(3.0, analysis.videoDurationSeconds());
        assertEquals(2, analysis.personCount());
        assertTrue(analysis.hasSilentSignal());
        server.verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void preservesOptionalKeypadFields(String triggeredAt) {
        respond("{\"result\":{}}");
        VisionAnalysis analysis = adapter.analyze(file,
                new VideoTriggerContext(null, "keypad", TriggerType.KEYPAD, null, triggeredAt));
        assertEquals("keypad", analysis.triggerId());
        assertEquals(TriggerType.KEYPAD, analysis.triggerType());
        assertNull(analysis.securityEventId());
        assertNull(analysis.triggeredAt());
        assertNull(analysis.behavior());
        assertFalse(analysis.hasVisionEvents());
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{}", "{\"result\":null}",
            "{\"result\":{\"analyzedAt\":\"invalid\"}}", "{\"result\":{\"visionEvents\":[null]}}"})
    void convertsInvalidResponsesToBusinessException(String response) {
        respond(response);
        BusinessException exception = assertThrows(BusinessException.class, () -> adapter.analyze(file, context));
        assertEquals(DetectionErrorCode.VISION_ANALYSIS_FAILED, exception.getErrorCode());
        server.verify();
    }

    @Test
    void convertsInvalidContextTimestampToBusinessException() {
        respond("{\"result\":{}}");
        BusinessException exception = assertThrows(BusinessException.class, () -> adapter.analyze(file,
                new VideoTriggerContext(null, "keypad", TriggerType.KEYPAD, null, "invalid")));
        assertEquals(DetectionErrorCode.VISION_ANALYSIS_FAILED, exception.getErrorCode());
        assertNotNull(exception.getCause());
        server.verify();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void sendsKeypadMultipartWithoutSecurityEventId(String securityEventId) {
        VideoTriggerContext keypad = new VideoTriggerContext(
                null, "keypad-trigger", TriggerType.KEYPAD, securityEventId, "2026-09-24T12:00:00+09:00");
        expectMultipart(Map.of(
                "triggerId", "keypad-trigger",
                "triggerType", "KEYPAD",
                "triggeredAt", "2026-09-24T12:00:00+09:00"));

        adapter.analyze(file, keypad);

        server.verify();
    }

    @Test
    void sendsAudioMultipartWithSecurityEventId() {
        expectMultipart(Map.of(
                "triggerId", "original-trigger",
                "triggerType", "AUDIO",
                "triggeredAt", "2026-09-23T12:00:00+09:00",
                "securityEventId", "event"));

        adapter.analyze(file, context);

        server.verify();
    }

    private void expectMultipart(Map<String, String> fields) {
        server.expect(requestTo("http://vision.test/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    MediaType contentType = request.getHeaders().getContentType();
                    assertNotNull(contentType);
                    assertTrue(MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType));
                    String boundary = contentType.getParameter("boundary");
                    assertNotNull(boundary);
                    // ISO-8859-1 preserves every byte, including the binary file part.
                    String body = new String(((MockClientHttpRequest) request).getBodyAsBytes(),
                            StandardCharsets.ISO_8859_1);
                    String[] parts = body.split(Pattern.quote("--" + boundary), -1);
                    assertEquals(fields.size() + 3, parts.length);
                    assertEquals("", parts[0]);
                    assertEquals("--\r\n", parts[parts.length - 1]);
                    fields.forEach((name, value) -> assertMultipartPart(body, boundary,
                            "name=\"" + name + "\"", value));
                    if (!fields.containsKey("securityEventId")) {
                        assertFalse(body.contains("name=\"securityEventId\""));
                    }
                    assertMultipartPart(body, boundary,
                            "name=\"file\"; filename=\"video.mp4\"",
                            new String(file.bytes(), StandardCharsets.ISO_8859_1));
                })
                .andRespond(withSuccess("{\"result\":{}}", MediaType.APPLICATION_JSON));
    }

    private void assertMultipartPart(String body, String boundary, String disposition, String value) {
        String pattern = Pattern.quote("--" + boundary + "\r\nContent-Disposition: form-data; "
                + disposition + "\r\n")
                + "(?:[^\\r\\n]+\\r\\n)*\\r\\n"
                + Pattern.quote(value + "\r\n--" + boundary);
        assertTrue(Pattern.compile(pattern).matcher(body).find(), "Missing or incorrect multipart part: " + disposition);
    }

    private void respond(String response) {
        server.expect(requestTo("http://vision.test/analyze"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    }
}
