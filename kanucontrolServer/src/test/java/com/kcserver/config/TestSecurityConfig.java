package com.kcserver.config;

import com.kcserver.tenancy.TenantFilter;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@Disabled("Security wird später getestet")
@Profile("test")
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            TenantFilter tenantFilter
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth ->
                        oauth.jwt(Customizer.withDefaults())
                )

                // 🔥 wichtig für dein Multi-Tenant Setup
                .addFilterAfter(
                        tenantFilter,
                        BearerTokenAuthenticationFilter.class
                );

        return http.build();
    }
}