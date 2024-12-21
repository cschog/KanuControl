package com.kcserver.tenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class TenantFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TenantFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Retrieve the current Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if Authentication is null or invalid
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            logger.warn("No valid authentication found in SecurityContext.");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the "groups" claim from the JWT
        Object groupsClaim = jwt.getClaim("groups");

        if (groupsClaim instanceof List<?> groups && !groups.isEmpty()) {
            String tenantName = groups.get(0).toString(); // Use the first group as the tenant name
            TenantContext.setTenantId(tenantName);
            logger.info("Tenant extracted: {}", tenantName);
        } else {
            logger.warn("No 'groups' claim found in JWT.");
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}