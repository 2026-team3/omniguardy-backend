package com.omniguardy.backend.domain.auth.dto.internal;

import com.omniguardy.backend.domain.auth.dto.response.ReissueResponseDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReissueResult {

    private ReissueResponseDto responseDto;
    private String refreshToken;
}
