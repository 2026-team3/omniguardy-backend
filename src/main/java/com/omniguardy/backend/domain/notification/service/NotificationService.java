package com.omniguardy.backend.domain.notification.service;

import com.omniguardy.backend.domain.notification.entity.DeviceToken;
import com.omniguardy.backend.domain.notification.entity.Notification;
import com.omniguardy.backend.domain.notification.entity.NotificationType;
import com.omniguardy.backend.domain.notification.repository.DeviceTokenRepository;
import com.omniguardy.backend.domain.notification.repository.NotificationRepository;
import com.omniguardy.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;

    @Transactional
    public void sendSecurityAlert(User user, int riskLevel, String eventType) {
        String title = makeTitle(riskLevel);
        String message = makeMessage(riskLevel, eventType);

        notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .type(NotificationType.SECURITY_EVENT)
                        .riskLevel(riskLevel)
                        .title(title)
                        .message(message)
                        .read(false)
                        .build()
        );

        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByUserAndActiveTrue(user);

        for (DeviceToken deviceToken : deviceTokens) {
            try {
                fcmService.sendMessage(
                        deviceToken.getToken(),
                        title,
                        message,
                        "SECURITY_EVENT",
                        String.valueOf(riskLevel)
                );
            } catch (Exception e) {
                System.out.println("FCM 전송 실패: " + e.getMessage());
            }
        }
    }

    private String makeTitle(int riskLevel) {
        if (riskLevel >= 3) {
            return "긴급 위험 알림";
        }

        if (riskLevel == 2) {
            return "주의 상황 감지";
        }

        return "현관 이벤트 감지";
    }

    private String makeMessage(int riskLevel, String eventType) {
        if (riskLevel >= 3) {
            return "위험 상황이 감지되었습니다. 즉시 현관 상황을 확인해주세요.";
        }

        if (riskLevel == 2) {
            return "현관 앞에 의심 상황이 감지되었습니다.";
        }

        return "현관 앞 이벤트가 감지되었습니다.";
    }
}
