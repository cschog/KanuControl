package com.kcserver.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        schema = "audit",
        name = "audit_session",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_session",
                        columnNames = "session_id"
                )
        }
)
@Getter
@Setter
public class AuditSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    private String tenant;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    private String client;

    @Column(name = "login_success")
    private Boolean loginSuccess;
}