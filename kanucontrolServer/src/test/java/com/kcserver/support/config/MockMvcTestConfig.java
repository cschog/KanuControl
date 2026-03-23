package com.kcserver.support.config;

import com.kcserver.tenancy.TenantContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestConfiguration
public class MockMvcTestConfig {

    @Bean
    public MockMvcBuilderCustomizer mockMvcBuilderCustomizer() {
        return builder -> builder.defaultRequest(
                get("/") // Dummy
                        .with(request -> {

                            String tenant = "ekc_test";

                            // 🔥 wichtig für Hibernate
                            TenantContext.setCurrentTenant(tenant);

                            // 🔥 Header
                            request.addHeader("X-Tenant", tenant);

                            // 🔥 Auth
                            return user("test")
                                    .roles("USER")
                                    .postProcessRequest(request);
                        })
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON) // 🔥 wichtig!
        );
    }
}