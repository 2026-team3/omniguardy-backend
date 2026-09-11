package com.omniguardy.backend.detection.application.port.out;
import com.omniguardy.backend.detection.application.model.AudioAnalysis;
import com.omniguardy.backend.detection.application.model.MediaFile;
public interface AudioAnalysisPort { AudioAnalysis analyze(MediaFile file); }
