package com.kcserver.tenancy;

import org.springframework.stereotype.Service;

@Service
public class TenantConfigService {
    public TenantConfig getConfigForCurrentTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant specified");
        }

        return loadConfigForTenant(tenantId); // Load config from DB, file, or cache
    }

    private TenantConfig loadConfigForTenant(String tenantId) {
        // Example: Load configuration from a database or file
        // For demonstration, returning a dummy config
        TenantConfig tenantConfig = new TenantConfig();
        tenantConfig.setTenantId(tenantId);
        tenantConfig.setDatabaseUrl("jdbc:mysql://localhost:3306/" + tenantId);
        // tenantConfig.setOtherConfig("Some other tenant-specific config");
        return tenantConfig;
    }
}