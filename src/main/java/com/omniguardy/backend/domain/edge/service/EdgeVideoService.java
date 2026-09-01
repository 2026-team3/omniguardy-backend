package com.omniguardy.backend.domain.edge.service;

import com.omniguardy.backend.domain.edge.dto.response.VideoUploadResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class EdgeVideoService {

    private static final String VIDEO_DIR = "./uploads/video";

    public VideoUploadResponseDto receiveVideo(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("영상 파일이 비어 있습니다.");
        }

        try {
            Path uploadDir = Paths.get(VIDEO_DIR);
            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();

            String filename =
                    UUID.randomUUID() + "_" +
                            (originalFilename != null
                                    ? originalFilename
                                    : "video.mp4");

            Path savePath = uploadDir.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    savePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println("==============================");
            System.out.println("[EDGE VIDEO]");
            System.out.println("파일명: " + filename);
            System.out.println("크기: " + file.getSize());
            System.out.println("저장 위치: " + savePath.toAbsolutePath());
            System.out.println("==============================");

            return new VideoUploadResponseDto(
                    filename,
                    file.getSize()
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "영상 파일 저장 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}
