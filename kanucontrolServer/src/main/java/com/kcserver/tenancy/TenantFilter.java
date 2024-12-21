package com.kcserver.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.servlet.Filter;

import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    @Autowired
    private TenantDatabaseService tenantDatabaseService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            // Determine tenant from Keycloak group
            String tenant = resolveTenantFromKeycloak();

            // Dynamically create the schema if it doesn't exist
            tenantDatabaseService.createSchemaIfNotExists(tenant);

            // Set the current tenant
            TenantAwareDataSource.setCurrentTenant(tenant);

            chain.doFilter(request, response);
        } finally {
            TenantAwareDataSource.setCurrentTenant(null);
        }
    }

    private String resolveTenantFromKeycloak() {
        // Fetch Keycloak groups and resolve to tenant schema
        // Similar to the earlier resolveTenantFromKeycloak implementation
        return "dynamic_schema_name";
    }
}