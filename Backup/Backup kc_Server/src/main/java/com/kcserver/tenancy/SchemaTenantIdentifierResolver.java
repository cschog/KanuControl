package com.kcserver.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class SchemaTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        // Get the tenant identifier from the TenantContext
        String tenantId = TenantContext.getCurrentTenant();
        return tenantId != null ? tenantId : "default"; // Default tenant schema
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
