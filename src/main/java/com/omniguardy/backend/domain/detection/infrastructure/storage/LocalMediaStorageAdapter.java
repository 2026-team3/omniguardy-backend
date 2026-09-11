package com.omniguardy.backend.domain.detection.infrastructure.storage;

import com.omniguardy.backend.domain.detection.application.model.MediaFile;
import com.omniguardy.backend.domain.detection.application.port.out.MediaStoragePort;
import com.omniguardy.backend.domain.detection.domain.error.DetectionErrorCode;
import com.omniguardy.backend.global.error.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalMediaStorageAdapter implements MediaStoragePort {
    private static final Path AUDIO_DIR = Paths.get("./uploads/audio");
    private static final Path VIDEO_DIR = Paths.get("./uploads/video");

    @Override public StoredMedia saveAudio(String eventId, MediaFile file) {
        String original = fallback(file.originalFilename(), "audio.wav");
        return save(AUDIO_DIR, eventId + "_" + original, file);
    }

    @Override public StoredMedia saveVideo(String eventId, MediaFile file) {
        String original = fallback(file.originalFilename(), "video.mp4");
        return save(VIDEO_DIR, eventId + "_" + UUID.randomUUID() + "_" + original, file);
    }

    private StoredMedia save(Path directory, String filename, MediaFile file) {
        try {
            Files.createDirectories(directory);
            Path path = directory.resolve(filename);
            Files.write(path, file.bytes());
            return new StoredMedia(filename, path.toString());
        } catch (IOException exception) {
            throw new BusinessException(DetectionErrorCode.MEDIA_STORAGE_FAILED, exception);
        }
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

