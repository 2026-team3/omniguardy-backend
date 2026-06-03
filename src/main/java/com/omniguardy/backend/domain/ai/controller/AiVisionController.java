package com.omniguardy.backend.domain.ai.controller;

import com.omniguardy.backend.domain.ai.dto.VisionAnalyzeResponse;
import com.omniguardy.backend.domain.ai.service.AiVisionService;
import com.omniguardy.backend.global.response.ApiResponse;
import com.omniguardy.backend.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiVisionController {

    private final AiVisionService aiVisionService;

    @PostMapping("/vision")
    public ApiResponse<VisionAnalyzeResponse> analyzeVision(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("영상 파일은 필수입니다.");
        }

        VisionAnalyzeResponse response = aiVisionService.analyzeVision(
                file,
                userDetails.getUser()
        );

        return ApiResponse.success("비전 분석이 완료되었습니다.", response);
    }
}
