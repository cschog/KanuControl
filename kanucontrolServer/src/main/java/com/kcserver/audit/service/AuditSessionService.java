package com.kcserver.audit.service;

import com.kcserver.audit.enumtype.SessionEndReason;
import com.kcserver.audit.repository.AuditSessionRepository;
import com.kcserver.tenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

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
    @Transactional
    public void registerLogout(String sessionId) {
        repository.updateLogoutTime(
                sessionId,
                LocalDateTime.now(),
                SessionEndReason.LOGOUT
        );
    }

    @Transactional(readOnly = true)
    public List<String> getOnlineUsers() {

        String tenant = TenantContext.getCurrentTenant();

        String currentUsername =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        return repository.findOtherOnlineUsers(
                tenant,
                currentUsername,
                LocalDateTime.now().minusMinutes(5)
        );
    }

}