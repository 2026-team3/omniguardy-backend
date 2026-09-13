package com.omniguardy.backend.domain.ai.application.model;

public record AnalysisResult(VisionResult vision, AudioResult audio) {
    public record VisionResult(String module, String video, String events, int riskScore,
                               String riskLevel, String annotatedVideo) {}
    public record AudioResult(String status, double probability) {}
}
