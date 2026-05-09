package com.kcserver.audit.repository;

import com.kcserver.audit.entity.AuditSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditSessionRepository
        extends JpaRepository<AuditSession, Long> {

    Optional<AuditSession> findBySessionId(String sessionId);
}