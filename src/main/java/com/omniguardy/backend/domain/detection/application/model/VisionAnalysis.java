package com.omniguardy.backend.domain.detection.application.model;

import java.util.Map;

public record VisionAnalysis(String prediction, double confidence, Map<String, Double> classProbabilities,
                             int personCount, double videoDurationSeconds, String analyzedAt) {}

