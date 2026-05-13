package com.kcserver.audit.repository;

import com.kcserver.audit.entity.AuditSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface AuditSessionRepository
        extends JpaRepository<AuditSession, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO audit.audit_session (
            username,
            full_name,
            email,
            tenant,
            session_id,
            login_time,
            last_seen,
            ip_address,
            user_agent,
            login_success
        )
        VALUES (
            :username,
            :fullName,
            :email,
            :tenant,
            :sessionId,
            :now,
            :now,
            :ip,
            :userAgent,
            true
        )
        ON CONFLICT (session_id)
        DO UPDATE SET
            last_seen = EXCLUDED.last_seen
        """, nativeQuery = true)
    void upsertSession(
            String username,
            String fullName,
            String email,
            String tenant,
            String sessionId,
            LocalDateTime now,
            String ip,
            String userAgent
    );
}