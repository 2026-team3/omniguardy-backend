package com.omniguardy.backend.detection.application.port.out;
import com.omniguardy.backend.detection.application.model.MediaFile;
public interface MediaStoragePort {
    StoredMedia saveAudio(String eventId, MediaFile file);
    StoredMedia saveVideo(String eventId, MediaFile file);
    record StoredMedia(String filename, String path) {}
}
