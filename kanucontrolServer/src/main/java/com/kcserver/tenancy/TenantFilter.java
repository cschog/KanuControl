package com.kcserver.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final TenantSchemaService tenantSchemaService;

    public TenantFilter(TenantSchemaService tenantSchemaService) {
        this.tenantSchemaService = tenantSchemaService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String tenant = request.getHeader("X-Tenant");

        if (tenant == null || tenant.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing X-Tenant header");
            return;
        }

        try {
            TenantContext.setTenant(tenant);
            tenantSchemaService.ensureSchemaExists(tenant);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}