package com.kcserver.audit.service;

import com.kcserver.audit.entity.AuditSession;
import com.kcserver.audit.repository.AuditSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

        AuditSession s = repository
                .findFirstBySessionId(sessionId)
                .orElseGet(() -> {

                    AuditSession neu = new AuditSession();

                    neu.setUsername(username);
                    neu.setFullName(fullName);
                    neu.setEmail(email);
                    neu.setTenant(tenant);
                    neu.setSessionId(sessionId);
                    neu.setLoginTime(LocalDateTime.now());
                    neu.setIpAddress(ip);
                    neu.setUserAgent(userAgent);
                    neu.setLoginSuccess(true);

                    return neu;
                });

        s.setLastSeen(LocalDateTime.now());

        try {

            repository.saveAndFlush(s);

        } catch (DataIntegrityViolationException ex) {

            // Session wurde parallel angelegt
            AuditSession existing = repository
                    .findFirstBySessionId(sessionId)
                    .orElseThrow();

            existing.setLastSeen(LocalDateTime.now());

            repository.save(existing);
        }
    }
}