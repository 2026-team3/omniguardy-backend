package com.omniguardy.backend.domain.detection.application.model;

public record AudioAnalysis(String status, double probability) {
    public boolean isAbnormal() { return "abnormal".equalsIgnoreCase(status); }
}

