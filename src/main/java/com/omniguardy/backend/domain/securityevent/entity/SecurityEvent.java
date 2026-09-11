package com.omniguardy.backend.domain.securityevent.entity;

import com.omniguardy.backend.domain.user.entity.User;
import com.omniguardy.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "security_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SecurityEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 하나의 Audio → Video → Vision → Agent 사건을 묶는 ID
    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    // 해당 보안 이벤트의 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    // =========================
    // Audio AI 결과
    // =========================

    @Column(name = "audio_status", length = 20)
    private String audioStatus;

    @Column(name = "audio_probability")
    private Double audioProbability;

    // 현재 로컬 개발용
    @Column(name = "audio_path", length = 500)
    private String audioPath;


    // =========================
    // Video
    // =========================

    // 현재는 로컬 경로
    // 배포 시 S3 key 또는 URL 구조로 변경
    @Column(name = "video_path", length = 500)
    private String videoPath;


    // =========================
    // Vision AI 결과
    // =========================

    @Column(name = "vision_prediction", length = 20)
    private String visionPrediction;

    @Column(name = "vision_confidence")
    private Double visionConfidence;

    @Lob
    @Column(name = "class_probabilities", columnDefinition = "TEXT")
    private String classProbabilities;

    @Column(name = "person_count")
    private Integer personCount;

    @Column(name = "video_duration_seconds")
    private Double videoDurationSeconds;

    @Column(name = "vision_analyzed_at", length = 50)
    private String visionAnalyzedAt;


    // =========================
    // Agent AI 결과
    // =========================

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "agent_reason", length = 1000)
    private String agentReason;


    // 현재 처리 단계
    // AUDIO_DETECTED / CAMERA_REQUESTED /
    // VISION_ANALYZED / COMPLETED / FAILED
    @Column(name = "status", nullable = false, length = 30)
    private String status;


    public void updateStatus(String status) {
        this.status = status;
    }

    public void updateVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public void updateVisionResult(
            String prediction,
            Double confidence,
            String classProbabilities,
            Integer personCount,
            Double videoDurationSeconds,
            String analyzedAt
    ) {
        this.visionPrediction = prediction;
        this.visionConfidence = confidence;
        this.classProbabilities = classProbabilities;
        this.personCount = personCount;
        this.videoDurationSeconds = videoDurationSeconds;
        this.visionAnalyzedAt = analyzedAt;
        this.status = "VISION_ANALYZED";
    }

    public void updateAgentResult(
            String riskLevel,
            String agentReason
    ) {
        this.riskLevel = riskLevel;
        this.agentReason = agentReason;
        this.status = "COMPLETED";
    }

    public void fail() {
        this.status = "FAILED";
    }
}
