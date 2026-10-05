package com.kcserver.core.audit.dto;

public record AuditDashboardDTO(
        long activeSessions,
        long loginsToday,
        long externalSessions,
        long activeTenants
) {
}