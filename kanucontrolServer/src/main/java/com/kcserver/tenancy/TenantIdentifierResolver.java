package com.kcserver.tenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver {

    private static final String DEFAULT_TENANT = "kanu";
    private boolean loggedOnce = false;

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getTenant();

        if (tenant == null) {
            if (!loggedOnce) {
                log.debug("No tenant in context → using DEFAULT_TENANT={}", DEFAULT_TENANT);
                loggedOnce = true;
            }
            return DEFAULT_TENANT;
        }

        return tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}