package com.kcserver.audit.service;

import com.kcserver.audit.entity.AuditSession;
import com.kcserver.audit.repository.AuditSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditSessionService {

    private final AuditSessionRepository repository;

    public void registerOrUpdate(
            String username,
            String fullName,
            String email,
            String tenant,
            String sessionId,
            String ip,
            String userAgent
    ) {

        AuditSession s =
                repository
                        .findBySessionId(sessionId)
                        .orElse(null);

        if (s == null) {

            s = new AuditSession();

            s.setUsername(username);

            s.setFullName(fullName);

            s.setEmail(email);

            s.setTenant(tenant);

            s.setSessionId(sessionId);

            s.setLoginTime(LocalDateTime.now());

            s.setIpAddress(ip);

            s.setUserAgent(userAgent);

            s.setLoginSuccess(true);
        }

        s.setLastSeen(LocalDateTime.now());

        repository.save(s);
    }
}