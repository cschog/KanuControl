package com.kcserver.config;

import com.kcserver.filter.TenantFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwkSetUri("http://localhost:9080/realms/KanuControl/protocol/openid-connect/certs")
                        )
                );

        // Register the TenantFilter AFTER the BearerTokenAuthenticationFilter
        http.addFilterAfter(new TenantFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}