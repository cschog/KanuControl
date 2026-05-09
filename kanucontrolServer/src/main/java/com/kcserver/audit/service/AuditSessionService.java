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
                .findBySessionId(sessionId)
                .orElse(null);

        if (s == null) {

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
            neu.setLastSeen(LocalDateTime.now());

            try {

                tryInsert(neu);

                return;

            } catch (DataIntegrityViolationException ignored) {

                // andere Request war schneller
            }

            // Session jetzt erneut laden
            s = repository
                    .findBySessionId(sessionId)
                    .orElse(null);

            if (s == null) {
                return;
            }
        }

        s.setLastSeen(LocalDateTime.now());

        repository.save(s);
    }
    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    protected void tryInsert(AuditSession session) {

        repository.save(session);
    }
}