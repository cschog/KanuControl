package com.kcserver.core.audit.service;

import com.kcserver.core.audit.dto.AuditDashboardDTO;
import com.kcserver.core.audit.dto.AuditSessionDTO;
import com.kcserver.core.audit.entity.AuditSession;
import com.kcserver.core.audit.repository.AuditSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditQueryService {

    private final AuditSessionRepository repository;

    public List<AuditSessionDTO> getActiveSessions() {

        return repository.findActiveSessions()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public Page<AuditSessionDTO> getHistory(Pageable pageable) {

        return repository.findHistory(pageable)
                .map(this::toDto);
    }

    private AuditSessionDTO toDto(AuditSession a) {

        return new AuditSessionDTO(
                a.getId(),
                a.getUsername(),
                a.getFullName(),
                a.getEmail(),
                a.getTenant(),
                a.getLoginTime(),
                a.getLogoutTime(),
                a.getLastSeen(),
                a.getIpAddress(),
                a.getUserAgent()
        );
    }
    public AuditDashboardDTO getDashboard() {

        return new AuditDashboardDTO(
                repository.countByLogoutTimeIsNull(),
                repository.countByLoginTimeAfter(
                        LocalDateTime.now().toLocalDate().atStartOfDay()
                ),
                repository.countExternalSessions(),
                repository.countActiveTenants()
        );
    }
}