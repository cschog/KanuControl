package com.kcserver.tenancy;

import com.kcserver.controller.PersonController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

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

                        // Startmenü / Anzeige
                        || path.equals("/api/active-schema")

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

        logger.debug("➡️ TenantFilter ENTER  {} {}", request.getMethod(), request.getRequestURI());

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.debug("❌ No Authentication in SecurityContext");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No JWT found");
            return;
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            logger.debug("❌ Authentication principal is {}", authentication.getPrincipal().getClass());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No JWT found");
            return;
        }

        String tenantId = extractTenant(jwt);
        logger.debug("🔑 Extracted tenant from JWT = {}", tenantId);

        if (tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No tenant in token");
            return;
        }

        tenantSchemaService.initializeTenant(tenantId);

        try {
            TenantContext.setCurrentTenant(tenantId);
            logger.debug("🟢 TenantContext SET = {}", tenantId);

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
            logger.debug("🧹 TenantContext CLEARED");
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