package com.kcserver.service;

import com.kcserver.config.SchemaMultiTenantConnectionProvider;
import com.kcserver.tenancy.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private SchemaMultiTenantConnectionProvider schemaMultiTenantConnectionProvider;

    public void handleLogin(String tenantId) {
        try {
            TenantContext.setTenantId(tenantId); // Set the tenant in the context
            schemaMultiTenantConnectionProvider.getTenantConnection(tenantId); // Ensure schema exists and switch
        } catch (Exception e) {
            throw new RuntimeException("Failed to prepare tenant schema for tenant: " + tenantId, e);
        }
    }
}