package com.omniguardy.backend.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    public void sendMessage(
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
            throw new RuntimeException("FCM 전송 실패", e);
        }
    }
}
