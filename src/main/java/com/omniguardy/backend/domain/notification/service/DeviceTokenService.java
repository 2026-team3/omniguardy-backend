package com.omniguardy.backend.domain.notification.service;

import com.omniguardy.backend.domain.notification.dto.request.DeviceTokenSaveRequest;
import com.omniguardy.backend.domain.notification.entity.DeviceToken;
import com.omniguardy.backend.domain.notification.repository.DeviceTokenRepository;
import com.omniguardy.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void saveDeviceToken(User user, DeviceTokenSaveRequest request) {
        deviceTokenRepository.findByToken(request.getToken())
                .ifPresentOrElse(
                        DeviceToken::activate,
                        () -> deviceTokenRepository.save(
                                DeviceToken.builder()
                                        .user(user)
                                        .token(request.getToken())
                                        .deviceType(request.getDeviceType())
                                        .active(true)
                                        .build()
                        )
                );
    }
}
