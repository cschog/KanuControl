package com.kcserver.config;

import com.kcserver.tenancy.TenantContext; // Import the TenantContext class
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SchemaTenantIdentifierResolver:
 * - Implements CurrentTenantIdentifierResolver from Hibernate.
 * - Reads the tenant ID from TenantContext.
 * - Returns it to Hibernate so that SchemaMultiTenantConnectionProvider
 *   can switch to the correct schema.
 */
@Component
public class SchemaTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static final Logger logger = LoggerFactory.getLogger(SchemaTenantIdentifierResolver.class);

    private static final String DEFAULT_TENANT_SCHEMA = "kanu"; // Replace with your default schema

    public SchemaTenantIdentifierResolver() {
        logger.info("SchemaTenantIdentifierResolver initialized.");
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        // Retrieve the tenant identifier from the TenantContext
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            logger.warn("SchemaTenantIdentifierResolver: No tenant ID found, using default schema.");
            return DEFAULT_TENANT_SCHEMA;
        }
        logger.info("SchemaTenantIdentifierResolver: Resolved tenant identifier: {}", tenantId);
        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}