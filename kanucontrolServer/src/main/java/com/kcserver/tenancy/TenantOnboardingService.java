package com.kcserver.tenancy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantOnboardingService {

    @Autowired
    private TenantDatabaseService tenantDatabaseService;

    public void onboardTenant(String tenantName) {
        tenantDatabaseService.ensureSchemaExists(tenantName);
    }
}
