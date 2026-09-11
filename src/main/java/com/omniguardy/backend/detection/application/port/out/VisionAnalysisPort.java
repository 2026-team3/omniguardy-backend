package com.omniguardy.backend.detection.application.port.out;
import com.omniguardy.backend.detection.application.model.MediaFile;
import com.omniguardy.backend.detection.application.model.VisionAnalysis;
public interface VisionAnalysisPort { VisionAnalysis analyze(MediaFile file); }
