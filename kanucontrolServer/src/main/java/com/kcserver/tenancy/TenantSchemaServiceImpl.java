package com.kcserver.tenancy;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class TenantSchemaServiceImpl implements TenantSchemaService {

    private final TenantSchemaInitializer initializer;

    public TenantSchemaServiceImpl(TenantSchemaInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void initializeTenant(String tenantId) {
        // bewusst leer
        // Tenant-Schemas werden aktuell nur in Tests erzeugt
    }
}