package com.omniguardy.backend.domain.notification.dto.request;

import com.omniguardy.backend.domain.notification.entity.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DeviceTokenSaveRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String token;

    @NotNull(message = "디바이스 타입은 필수입니다.")
    private DeviceType deviceType;
}
