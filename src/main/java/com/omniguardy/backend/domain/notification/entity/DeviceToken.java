package com.omniguardy.backend.domain.notification.entity;

import com.omniguardy.backend.domain.user.entity.User;
import com.omniguardy.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "device_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 한 명의 사용자가 여러 기기에서 로그인할 수 있으므로 ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType;

    @Column(nullable = false)
    private boolean active;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
