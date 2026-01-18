package com.kcserver.config;

import com.kcserver.tenancy.TenantFilter;
import com.kcserver.tenancy.TenantSchemaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public TenantFilter tenantFilter(TenantSchemaService tenantSchemaService) {
        return new TenantFilter(tenantSchemaService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            TenantFilter tenantFilter
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ✅ MUSS authenticated sein
                        .requestMatchers("/api/active-schema").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))

                // ✅ RICHTIG für Spring Boot 3.2.x
                .addFilterAfter(
                        tenantFilter,
                        BearerTokenAuthenticationFilter.class
                );

        return http.build();
    }
}