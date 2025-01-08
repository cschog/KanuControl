package com.kcserver.config;

import com.kcserver.tenancy.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SchemaTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static final Logger logger = LoggerFactory.getLogger(SchemaTenantIdentifierResolver.class);

    private static final String DEFAULT_TENANT = "kanu"; // Static tenant for initialization
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            logger.debug("No tenant ID found. Using default tenant: {}", DEFAULT_TENANT);
            return DEFAULT_TENANT;
        }
        logger.info("Resolved tenant identifier: {}", tenantId);
        return tenantId;
    }

    public static void setTenant(String tenant) {
        logger.info("Setting tenant ID: {}", tenant);
        currentTenant.set(tenant);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}