package com.kcserver.tenancy;

import com.kcserver.controller.PersonController;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String> {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    private final AtomicBoolean loggedOnce = new AtomicBoolean(false);
    private static final String BOOTSTRAP_TENANT = "kanu";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getTenant();

        logger.debug("🧭 TenantIdentifierResolver.resolve() → TenantContext = {}", tenant);

        if (tenant == null) {
            // 🟢 Bootstrap / Repository-Init
            if (loggedOnce.compareAndSet(false, true)) {
                logger.debug("No tenant in context → using DEFAULT_TENANT={}", BOOTSTRAP_TENANT);
            }
            return BOOTSTRAP_TENANT;
        }

        // 🔐 Runtime-Sicherheit
        if (BOOTSTRAP_TENANT.equals(tenant)) {
            throw new IllegalStateException(
                    "Refusing to use bootstrap tenant 'kanu' during runtime"
            );
        }

        return tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}