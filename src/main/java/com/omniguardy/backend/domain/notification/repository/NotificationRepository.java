package com.omniguardy.backend.domain.notification.repository;

import com.omniguardy.backend.domain.notification.entity.Notification;
import com.omniguardy.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);
}
