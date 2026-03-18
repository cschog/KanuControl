package com.kcserver.integration.support;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

public class TestJwtUtils {

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithTenant(
            String tenant
    ) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.claim("tenant", tenant));
    }
}