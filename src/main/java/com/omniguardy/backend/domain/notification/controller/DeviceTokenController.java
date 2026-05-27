package com.omniguardy.backend.domain.notification.controller;

import com.omniguardy.backend.domain.notification.dto.request.DeviceTokenSaveRequest;
import com.omniguardy.backend.domain.notification.service.DeviceTokenService;
import com.omniguardy.backend.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public void saveDeviceToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid DeviceTokenSaveRequest request
    ) {
        deviceTokenService.saveDeviceToken(userDetails.getUser(), request);
    }
}
