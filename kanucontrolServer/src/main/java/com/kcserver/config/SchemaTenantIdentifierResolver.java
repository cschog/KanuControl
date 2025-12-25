package com.kcserver.config;

import com.kcserver.tenancy.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchemaTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String tenant = "kanu"; // Default

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            List<String> groups = jwtAuth.getToken().getClaimAsStringList("groups");

            if (groups != null) {
                if (groups.contains("/EKC_EschweilerKanuClub")) tenant = "ekc";
                else if (groups.contains("/OKC_OberhausenerKanuClub")) tenant = "okc";
                else if (groups.contains("/SVBT_SpielvereinigungBoichThum")) tenant = "svbt";
            }
        }

        TenantContext.setTenant(tenant);
        return tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}