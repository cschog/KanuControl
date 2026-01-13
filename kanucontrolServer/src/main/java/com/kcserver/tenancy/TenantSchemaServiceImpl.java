package com.kcserver.tenancy;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Production implementation.
 *
 * Tenant schemas are NOT created automatically.
 * Provisioning is currently handled explicitly in tests only.
 */
@Service
@Profile("!test")
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