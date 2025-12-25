package com.kcserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class Auditable {

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.lastModifiedDate = now;
        this.createdBy = resolveCurrentUser();
        this.lastModifiedBy = this.createdBy;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
        this.lastModifiedBy = resolveCurrentUser();
    }

    private String resolveCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String username =
                    jwtAuth.getToken().getClaimAsString("preferred_username");
            if (username != null) {
                return username;
            }
        }

        return "system";
    }

    // Getter / Setter (oder Lombok @Getter/@Setter)
}