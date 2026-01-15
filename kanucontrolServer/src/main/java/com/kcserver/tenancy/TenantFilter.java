package com.kcserver.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class TenantFilter extends OncePerRequestFilter {

    private final TenantSchemaService tenantSchemaService;

    public TenantFilter(TenantSchemaService tenantSchemaService) {
        this.tenantSchemaService = tenantSchemaService;
    }

    /* =========================
       🔥 WICHTIGER TEIL
       ========================= */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return
                // CORS Preflight
                "OPTIONS".equalsIgnoreCase(request.getMethod())

                        // Spring / Infra
                        || path.startsWith("/actuator")
                        || path.startsWith("/error")

                        // Swagger / OpenAPI (falls genutzt)
                        || path.startsWith("/v3/api-docs")
                        || path.startsWith("/swagger-ui")

                        // ggf. öffentliche Endpoints
                        || path.startsWith("/public");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No JWT found");
            return;
        }

        String tenantId = extractTenant(jwt);

        if (tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No tenant in token");
            return;
        }

        tenantSchemaService.initializeTenant(tenantId);

        try {
            TenantContext.setCurrentTenant(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractTenant(Jwt jwt) {
        // 🔹 Custom Claim (falls später)
        String tenant = jwt.getClaimAsString("tenant");
        if (tenant != null) {
            return tenant;
        }

        // 🔹 Groups (dein aktuelles Setup)
        List<String> groups = jwt.getClaimAsStringList("groups");
        if (groups != null && !groups.isEmpty()) {
            return normalize(groups.get(0));
        }

        return null;
    }

    private String normalize(String group) {
        // EKC_EschweilerKanuClub → ekc_eschweilerkanuclub
        return group
                .replace("/", "")
                .toLowerCase();
    }
}