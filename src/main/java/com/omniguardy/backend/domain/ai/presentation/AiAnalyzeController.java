package com.omniguardy.backend.domain.ai.presentation;

import com.omniguardy.backend.domain.ai.application.model.AnalysisResult;
import com.omniguardy.backend.domain.ai.application.usecase.AnalyzeMediaUseCase;
import com.omniguardy.backend.domain.ai.presentation.dto.response.AiAnalyzeResponse;
import com.omniguardy.backend.domain.ai.presentation.mapper.AiPresentationMapper;
import com.omniguardy.backend.global.response.ApiResponse;
import com.omniguardy.backend.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiAnalyzeController {
    private final AnalyzeMediaUseCase analyzeMediaUseCase;
    private final AiPresentationMapper mapper;

    @PostMapping("/analyze")
    public ApiResponse<AiAnalyzeResponse> analyze(@RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        if (userDetails == null) throw new IllegalArgumentException("로그인이 필요합니다.");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("영상 파일은 필수입니다.");
        AnalysisResult result = analyzeMediaUseCase.analyze(mapper.toMediaFile(file));
        return ApiResponse.success("AI 분석이 완료되었습니다.", mapper.toResponse(result));
    }
}
