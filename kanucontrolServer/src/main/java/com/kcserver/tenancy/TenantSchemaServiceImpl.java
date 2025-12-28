package com.kcserver.tenancy;

import org.springframework.stereotype.Service;

@Service
public class TenantSchemaServiceImpl implements TenantSchemaService {

    private final TenantSchemaInitializer initializer;

    public TenantSchemaServiceImpl(TenantSchemaInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void initializeTenant(String tenantId) {
        // ✅ EXAKT die existierende Methode aufrufen
        initializer.initializeIfNeeded(tenantId);
    }
}