package com.kcserver.tenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TenantResolver {

    private static final Logger logger = LoggerFactory.getLogger(TenantResolver.class);


    public String resolveTenantFromToken() {
        // Get the authenticated JWT from SecurityContext
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
        return tenantId;
    }
}