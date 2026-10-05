package com.kcserver.core.audit.controller;

import com.kcserver.core.audit.dto.AuditDashboardDTO;
import com.kcserver.core.audit.dto.AuditSessionDTO;
import com.kcserver.core.audit.service.AuditQueryService;
import com.kcserver.core.audit.service.AuditSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit")
public class AuditController {

    private final AuditQueryService auditQueryService;
    private final AuditSessionService auditSessionService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active-sessions")
    public List<AuditSessionDTO> getActiveSessions() {
        return auditQueryService.getActiveSessions();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/history")
    public Page<AuditSessionDTO> getHistory(
            @PageableDefault(
                    size = 50,
                    sort = "loginTime",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return auditQueryService.getHistory(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public AuditDashboardDTO getDashboard() {
        return auditQueryService.getDashboard();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sessionId = jwt.getClaimAsString("sid");

        if (sessionId != null) {
            auditSessionService.registerLogout(sessionId);
        }

        return ResponseEntity.ok().build();
    }
}