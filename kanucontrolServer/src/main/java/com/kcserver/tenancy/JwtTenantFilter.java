package com.kcserver.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Profile("!test") // ✅ nur außerhalb von Tests
public class JwtTenantFilter
        extends OncePerRequestFilter
        implements TenantFilter {

    private final TenantSchemaService tenantSchemaService;

    public JwtTenantFilter(TenantSchemaService tenantSchemaService) {
        this.tenantSchemaService = tenantSchemaService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No JWT found");
            return;
        }

        Jwt jwt = jwtAuth.getToken();
        String tenant = extractTenant(jwt);

        if (tenant == null || tenant.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant missing in JWT");
            return;
        }

        tenantSchemaService.initializeTenant(tenant);

        try {
            TenantContext.setCurrentTenant(tenant);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.equals("/api/csv-import/mapping-template")
                || path.startsWith("/actuator")
                || path.startsWith("/error");
    }

    private String extractTenant(Jwt jwt) {
        // 1️⃣ expliziter Claim
        String tenant = jwt.getClaimAsString("tenant");
        if (tenant != null) return tenant;

        // 2️⃣ Fallback: Keycloak-Gruppen
        List<String> groups = jwt.getClaimAsStringList("groups");
        if (groups != null && !groups.isEmpty()) {
            return groups.get(0)
                    .replace("/", "")
                    .toLowerCase();
        }

        return null;
    }
}