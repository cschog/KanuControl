package com.kcserver.audit.service;

import com.kcserver.audit.repository.AuditSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditSessionService {

    private final AuditSessionRepository repository;

    @Transactional
    public void registerOrUpdate(
            String username,
            String fullName,
            String email,
            String tenant,
            String sessionId,
            String ip,
            String userAgent
    ) {

        LocalDateTime now = LocalDateTime.now();

        repository.upsertSession(
                username,
                fullName,
                email,
                tenant,
                sessionId,
                now,
                ip,
                userAgent
        );
    }
}