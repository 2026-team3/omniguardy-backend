package com.omniguardy.backend.domain.notification.infrastructure.fcm;

import com.omniguardy.backend.domain.notification.application.port.out.PushNotificationPort;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FirebasePushAdapter implements PushNotificationPort {

    public void send(
            String token,
            String title,
            String body,
            String type,
            String riskLevel
    ) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .putData("type", type)
                    .putData("riskLevel", riskLevel)
                    .build();

            FirebaseMessaging.getInstance().send(message);

        } catch (Exception e) {
            throw new RuntimeException("FCM ?꾩넚 ?ㅽ뙣", e);
        }
    }
}

