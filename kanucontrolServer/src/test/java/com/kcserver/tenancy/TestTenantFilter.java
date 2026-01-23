package com.kcserver.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 🔧 Test-only TenantFilter
 * → Tenant wird bereits im AbstractTenantIntegrationTest gesetzt
 */
@Component
@Profile("test")
public class TestTenantFilter extends OncePerRequestFilter
        implements TenantFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ⛔ KEINE Tenant-Logik hier!
        // TenantContext wird im Test gesetzt
        filterChain.doFilter(request, response);
    }
}