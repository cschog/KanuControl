package com.kcserver.audit;

import com.kcserver.audit.service.AuditSessionService;
import com.kcserver.tenancy.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import org.springframework.lang.NonNull;

@Component
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

    private final AuditSessionService auditService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (auth != null && auth.isAuthenticated()) {

            if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            String username =
                    jwt.getClaimAsString(
                            "preferred_username"
                    );

            if (username == null || username.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            String fullName =
                    jwt.getClaimAsString(
                            "name"
                    );
            String email =
                    jwt.getClaimAsString(
                            "email"
                            );

            String tenant =
                    TenantContext.getCurrentTenant();

            String sessionId =
                    jwt.getClaimAsString("sid");

            String ip = getClientIp(request);

            String userAgent =
                    request.getHeader("User-Agent");

            auditService.registerOrUpdate(
                    username,
                    fullName,
                    email,
                    tenant,
                    sessionId,
                    ip,
                    userAgent
            );
        }

        filterChain.doFilter(request, response);
    }
    private String getClientIp(HttpServletRequest request) {

        String xForwardedFor =
                request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp =
                request.getHeader("X-Real-IP");

        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}