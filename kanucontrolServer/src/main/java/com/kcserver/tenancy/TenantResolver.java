package com.kcserver.tenancy;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class TenantResolver {

    public String resolveTenantFromToken() {
        // Get the authenticated JWT from SecurityContext
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Extract tenant-related information (e.g., a custom claim)
        String tenantId = jwt.getClaimAsString("groups-kc"); // Replace "tenant_id" with your claim name

        if (tenantId == null || tenantId.isEmpty()) {
            throw new IllegalArgumentException("Tenant ID not found in token");
        }

        return tenantId;
    }
}