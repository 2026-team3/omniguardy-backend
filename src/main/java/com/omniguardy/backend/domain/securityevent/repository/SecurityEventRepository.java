package com.omniguardy.backend.domain.securityevent.repository;

import com.omniguardy.backend.domain.securityevent.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long> {

    Optional<SecurityEvent> findByEventId(String eventId);
}
