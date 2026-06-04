package com.kcserver.audit.dto;

import java.time.LocalDateTime;

public record AuditSessionDTO(
        Long id,
        String username,
        String fullName,
        String email,
        String tenant,
        LocalDateTime loginTime,
        LocalDateTime logoutTime,
        LocalDateTime lastSeen,
        String ipAddress,
        String userAgent
) {}
