package com.omniguardy.backend.domain.auth.service;

import com.omniguardy.backend.domain.auth.dto.internal.LoginResult;
import com.omniguardy.backend.domain.auth.dto.internal.ReissueResult;
import com.omniguardy.backend.domain.auth.dto.internal.SignupResult;
import com.omniguardy.backend.domain.auth.dto.request.LoginRequestDto;
import com.omniguardy.backend.domain.auth.dto.request.SignupRequestDto;
import com.omniguardy.backend.domain.auth.dto.response.LoginResponseDto;
import com.omniguardy.backend.domain.auth.dto.response.ReissueResponseDto;
import com.omniguardy.backend.domain.auth.dto.response.SignupResponseDto;
import com.omniguardy.backend.domain.user.entity.User;
import com.omniguardy.backend.domain.user.repository.UserRepository;
import com.omniguardy.backend.global.exception.CustomException;
import com.omniguardy.backend.global.exception.ErrorCode;
import com.omniguardy.backend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResult signup(SignupRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .name(requestDto.getName())
                .phoneNumber(requestDto.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtProvider.createAccessToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(savedUser.getId(), savedUser.getEmail());

        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now()
                .plusSeconds(jwtProvider.getRefreshTokenExpirationSeconds());

        savedUser.updateRefreshToken(refreshToken, refreshTokenExpiredAt);

        SignupResponseDto responseDto = SignupResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .build();

        return SignupResult.builder()
                .responseDto(responseDto)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public LoginResult login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN_INFO));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_INFO);
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());

        LocalDateTime refreshTokenExpiredAt = LocalDateTime.now()
                .plusSeconds(jwtProvider.getRefreshTokenExpirationSeconds());

        user.updateRefreshToken(refreshToken, refreshTokenExpiredAt);

        LoginResponseDto responseDto = LoginResponseDto.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();

        return LoginResult.builder()
                .responseDto(responseDto)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public ReissueResult reissue(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        jwtProvider.validateToken(refreshToken);

        Long userId = jwtProvider.getUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRefreshToken() == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        if (!user.getRefreshToken().equals(refreshToken)) {
            user.clearRefreshToken();
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        if (user.getRefreshTokenExpiredAt().isBefore(LocalDateTime.now())) {
            user.clearRefreshToken();
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId(), user.getEmail());

        LocalDateTime newRefreshTokenExpiredAt = LocalDateTime.now()
                .plusSeconds(jwtProvider.getRefreshTokenExpirationSeconds());

        user.updateRefreshToken(newRefreshToken, newRefreshTokenExpiredAt);

        ReissueResponseDto responseDto = ReissueResponseDto.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .build();

        return ReissueResult.builder()
                .responseDto(responseDto)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.clearRefreshToken();
    }
}
