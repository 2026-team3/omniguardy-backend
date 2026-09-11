package com.omniguardy.backend.domain.auth.presentation;

import com.omniguardy.backend.domain.auth.application.model.AuthSession;
import com.omniguardy.backend.domain.auth.application.usecase.AuthUseCase;
import com.omniguardy.backend.domain.auth.presentation.dto.request.LoginRequestDto;
import com.omniguardy.backend.domain.auth.presentation.dto.request.SignupRequestDto;
import com.omniguardy.backend.domain.auth.presentation.dto.response.LoginResponseDto;
import com.omniguardy.backend.domain.auth.presentation.dto.response.ReissueResponseDto;
import com.omniguardy.backend.domain.auth.presentation.dto.response.SignupResponseDto;
import com.omniguardy.backend.domain.auth.presentation.mapper.AuthPresentationMapper;
import com.omniguardy.backend.global.response.ApiResponse;
import com.omniguardy.backend.global.security.auth.CustomUserDetails;
import com.omniguardy.backend.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthUseCase authUseCase;
    private final AuthPresentationMapper mapper;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto request,
                                                                  HttpServletResponse response) {
        AuthSession session = authUseCase.signup(mapper.toCommand(request));
        addRefreshCookie(response, session);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", mapper.toSignupResponse(session)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request,
                                                                HttpServletResponse response) {
        AuthSession session = authUseCase.login(mapper.toCommand(request));
        addRefreshCookie(response, session);
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", mapper.toLoginResponse(session)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<ReissueResponseDto>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        AuthSession session = authUseCase.reissue(refreshToken);
        addRefreshCookie(response, session);
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", mapper.toReissueResponse(session)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     HttpServletResponse response) {
        authUseCase.logout(userDetails.getUserId());
        CookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다.", null));
    }

    private void addRefreshCookie(HttpServletResponse response, AuthSession session) {
        CookieUtil.addRefreshTokenCookie(response, session.refreshToken(), session.refreshTokenMaxAgeSeconds());
    }
}
