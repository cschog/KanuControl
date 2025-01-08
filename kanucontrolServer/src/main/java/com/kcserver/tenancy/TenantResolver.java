package com.kcserver.tenancy;

import com.kcserver.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;

import java.util.List;

@Component
public class TenantResolver {

    private static final Logger logger = LoggerFactory.getLogger(TenantResolver.class);
    private final LoginService loginService;

    public TenantResolver(LoginService loginService) {
        this.loginService = loginService;
        logger.info("TenantResolver initialized.");
    }

    public void resolveTenantFromAuthentication(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Jwt)) {
            logger.error("Authentication principal is not a Jwt: {}", authentication.getPrincipal());
            throw new IllegalStateException("Authentication principal is not a Jwt.");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        // logger.info("JWT Claims: {}", jwt.getClaims());

        // Extract groups or other tenant information
        List<String> groups = jwt.getClaimAsStringList("groups");
        List<String> groupsKc = jwt.getClaimAsStringList("groups-kc");

        logger.info("TenantResolver: groups: {}", groups);
        // logger.info("TenantResolver: groups-kc: {}", groupsKc);

        String tenantId = null;
        if (groupsKc != null && !groupsKc.isEmpty()) {
            tenantId = groupsKc.get(0);
        } else if (groups != null && !groups.isEmpty()) {
            tenantId = groups.get(0);
        }

        if (tenantId == null || tenantId.isEmpty()) {
            throw new IllegalArgumentException("Tenant ID not found in token");
        }

        logger.info("Resolved tenant ID: {}", tenantId);

        // Set the tenant in the context
        TenantContext.setTenantId(tenantId);

        // Handle login and schema preparation
        loginService.handleLogin(tenantId);
    }

    public String resolveTenantFromToken() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.error("SecurityContext Authentication is null.");
            throw new IllegalStateException("Authentication is null in SecurityContext.");
        }

        if (!(authentication.getPrincipal() instanceof Jwt)) {
            logger.error("Authentication principal is not a Jwt: {}", authentication.getPrincipal());
            throw new IllegalStateException("Authentication principal is not a Jwt.");
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        // logger.info("JWT Claims: {}", jwt.getClaims());

        // Get the authenticated JWT from SecurityContext
        jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // logger.info("TenantResolver: JWT Claims: {}", jwt.getClaims());

        // Extract groups and groups-kc claims
        List<String> groups = jwt.getClaimAsStringList("groups");
        List<String> groupsKc = jwt.getClaimAsStringList("groups-kc");

        // logger.info("TenantResolver: groups: {}", groups);
        // logger.info("TenantResolver: groups-kc: {}", groupsKc);

        // Prioritize groups-kc if present
        String tenantId = null;

        if (groupsKc != null && !groupsKc.isEmpty()) {
            tenantId = groupsKc.get(0);
        } else if (groups != null && !groups.isEmpty()) {
            tenantId = groups.get(0);
        }

        if (tenantId == null || tenantId.isEmpty()) {
            throw new IllegalArgumentException("Tenant ID not found in token");
        }

        logger.info("TenantResolver: Resolved tenant ID: {}", tenantId);

        TenantContext.setTenantId(tenantId);

        // Handle login and schema preparation
        loginService.handleLogin(tenantId);

        return tenantId;
    }
}