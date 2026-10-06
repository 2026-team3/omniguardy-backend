package com.omniguardy.backend.domain.detection.application.model;

public record AudioAnalysis(String status, double probability) {
    public boolean isAbnormal() {
        return "KNOCK_EVENT".equals(status) || "HANDLE_EVENT".equals(status);
    }
}

