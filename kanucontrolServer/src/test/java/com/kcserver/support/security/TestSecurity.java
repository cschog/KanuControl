package com.kcserver.support.security;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public class TestSecurity {

    public static RequestPostProcessor tenantUser(String tenant) {
        return request -> {
            request.addHeader("X-Tenant", tenant);
            return user("test")
                    .roles("USER")
                    .postProcessRequest(request);
        };
    }
}