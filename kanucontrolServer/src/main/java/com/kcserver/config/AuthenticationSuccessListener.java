package com.kcserver.config;

import com.kcserver.tenancy.TenantResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final TenantResolver tenantResolver;
    private static final Logger logger = LoggerFactory.getLogger(SchemaTenantIdentifierResolver.class);


    public AuthenticationSuccessListener(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            if (authentication == null) {
                logger.error("Authentication is null in AuthenticationSuccessEvent.");
                throw new IllegalStateException("Authentication is null.");
            }

            // Pass the authentication directly to the TenantResolver
            tenantResolver.resolveTenantFromAuthentication(authentication);

        } catch (Exception e) {
            logger.error("Error resolving tenant after login: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to resolve tenant after login", e);
        }
    }
}
