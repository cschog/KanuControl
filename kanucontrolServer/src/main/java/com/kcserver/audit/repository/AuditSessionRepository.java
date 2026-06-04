package com.kcserver.audit.repository;

import com.kcserver.audit.entity.AuditSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.kcserver.audit.enumtype.SessionEndReason;

import java.time.LocalDateTime;
import java.util.List;

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
            login_success,
            end_reason
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
            true,     
            'ACTIVE'
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
    @Modifying
    @Query("""
    update AuditSession a
    set a.logoutTime = :logoutTime,
       a.endReason = :endReason
    where a.sessionId = :sessionId
    and a.logoutTime is null
""")
    int updateLogoutTime(
            @Param("sessionId") String sessionId,
            @Param("logoutTime") LocalDateTime logoutTime,
            @Param("endReason") SessionEndReason endReason
    );
    @Modifying
    @Query(value = """
    UPDATE audit.audit_session
       SET logout_time = last_seen,
         end_reason = 'TIMEOUT'
     WHERE logout_time IS NULL
        AND last_seen < :cutoff
""", nativeQuery = true)
    int closeInactiveSessions(
            @Param("cutoff") LocalDateTime cutoff
    );

    @Query("""
    select a
    from AuditSession a
    where a.logoutTime is null
    order by a.lastSeen desc
""")
    List<AuditSession> findActiveSessions();

    @Query("""
    select a
    from AuditSession a
    order by a.loginTime desc
""")
    Page<AuditSession> findHistory(Pageable pageable);

    long countByLogoutTimeIsNull();
    long countByLoginTimeAfter(LocalDateTime since);

    @Query("""
    select count(a)
    from AuditSession a
    where a.logoutTime is null
      and a.ipAddress not like '192.168.100.%'
""")
    long countExternalSessions();

    @Query("""
    select count(distinct a.tenant)
    from AuditSession a
    where a.logoutTime is null
""")
    long countActiveTenants();
}