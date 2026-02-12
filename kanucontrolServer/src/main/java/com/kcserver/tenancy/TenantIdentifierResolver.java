package com.kcserver.tenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String> {

    private static final String BOOTSTRAP_TENANT = "kanu";   // ← NICHT ekc_test

    @Override
    public String resolveCurrentTenantIdentifier() {

        String tenant = TenantContext.getCurrentTenant();

        if (tenant == null || tenant.isBlank()) {
            // Bootstrap / Bean creation / Repository init
            return BOOTSTRAP_TENANT;
        }

        return tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}