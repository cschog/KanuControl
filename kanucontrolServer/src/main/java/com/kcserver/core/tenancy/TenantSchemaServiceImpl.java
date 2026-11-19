package com.kcserver.core.tenancy;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Production implementation.
 *
 * Valid JWT tenants are initialized on first use.
 * Existing schemas are migrated at startup.
 */
@Service
@Profile("!junit")
public class TenantSchemaServiceImpl implements TenantSchemaService {

    private final TenantSchemaProvisioner provisioner;

    public TenantSchemaServiceImpl(TenantSchemaProvisioner provisioner) {
        this.provisioner = provisioner;
    }

    @Override
    public void initializeTenant(String tenantId) {
        provisioner.createFromBaselineIfNeeded(tenantId);
    }
}
