package com.kcserver.audit.dto;

public record AuditDashboardDTO(
        long activeSessions,
        long loginsToday,
        long externalSessions,
        long activeTenants
) {
}