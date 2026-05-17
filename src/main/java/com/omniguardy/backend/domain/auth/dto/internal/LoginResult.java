package com.omniguardy.backend.domain.auth.dto.internal;

import com.omniguardy.backend.domain.auth.dto.response.LoginResponseDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResult {

    private LoginResponseDto responseDto;
    private String refreshToken;
}
