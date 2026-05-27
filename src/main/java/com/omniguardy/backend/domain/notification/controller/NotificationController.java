package com.omniguardy.backend.domain.notification.controller;

import com.omniguardy.backend.domain.notification.service.NotificationService;
import com.omniguardy.backend.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/test")
    public void sendTestNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        notificationService.sendSecurityAlert(
                userDetails.getUser(),
                2,
                "PERSON_DETECTED"
        );
    }
}
