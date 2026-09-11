package com.omniguardy.backend.domain.notification.application.usecase;

import com.omniguardy.backend.domain.notification.domain.model.DeviceToken;
import com.omniguardy.backend.domain.notification.domain.model.Notification;
import com.omniguardy.backend.domain.notification.domain.model.NotificationType;
import com.omniguardy.backend.domain.notification.domain.repository.DeviceTokenRepository;
import com.omniguardy.backend.domain.notification.domain.repository.NotificationRepository;
import com.omniguardy.backend.domain.notification.application.port.out.PushNotificationPort;
import com.omniguardy.backend.domain.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationPort pushNotificationPort;

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
                pushNotificationPort.send(
                        deviceToken.getToken(),
                        title,
                        message,
                        "SECURITY_EVENT",
                        String.valueOf(riskLevel)
                );
            } catch (Exception e) {
                System.out.println("FCM ?꾩넚 ?ㅽ뙣: " + e.getMessage());
            }
        }
    }

    private String makeTitle(int riskLevel) {
        if (riskLevel >= 3) {
            return "湲닿툒 ?꾪뿕 ?뚮┝";
        }

        if (riskLevel == 2) {
            return "二쇱쓽 ?곹솴 媛먯?";
        }

        return "?꾧? ?대깽??媛먯?";
    }

    private String makeMessage(int riskLevel, String eventType) {
        if (riskLevel >= 3) {
            return "?꾪뿕 ?곹솴??媛먯??섏뿀?듬땲?? 利됱떆 ?꾧? ?곹솴???뺤씤?댁＜?몄슂.";
        }

        if (riskLevel == 2) {
            return "?꾧? ?욎뿉 ?섏떖 ?곹솴??媛먯??섏뿀?듬땲??";
        }

        return "?꾧? ???대깽?멸? 媛먯??섏뿀?듬땲??";
    }
}

