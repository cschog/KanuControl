package com.kcserver.core.config;

import com.kcserver.core.tenancy.TenantFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@EnableMethodSecurity
@Profile("!junit")
public class SecurityConfig {

    // Der Filter gehört nur in die Security-Chain, nicht zusätzlich in die Servlet-Filterkette.
    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<com.kcserver.vermietung.ical.PublicAirbnbTenantFilter> airbnbFilterRegistration(
            com.kcserver.vermietung.ical.PublicAirbnbTenantFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            TenantFilter tenantFilter,
            com.kcserver.vermietung.ical.PublicAirbnbTenantFilter publicAirbnbTenantFilter
    ) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter =
                new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new CustomJwtAuthenticationConverter()
        );

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger erlauben ✅
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // CSV Template öffentlich
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/csv-import/mapping-template"
                        ).permitAll()

                        .requestMatchers(new org.springframework.security.web.util.matcher.RegexRequestMatcher(
                                com.kcserver.vermietung.ical.AirbnbFeedPath.REGEX + "(?:\\?.*)?", "GET")).permitAll()
                        .requestMatchers(new org.springframework.security.web.util.matcher.RegexRequestMatcher(
                                com.kcserver.vermietung.ical.AirbnbFeedPath.REGEX + "(?:\\?.*)?", "HEAD")).permitAll()

                        .requestMatchers("/api/integrationen/homepage/**").hasRole("HOMEPAGE_INTEGRATION")

                        // 🔒 alles andere geschützt
                        .anyRequest().access(new org.springframework.security.web.access.expression.WebExpressionAuthorizationManager(
                                "isAuthenticated() and !hasRole('HOMEPAGE_INTEGRATION')"))
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )

                .addFilterAfter(publicAirbnbTenantFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(tenantFilter, com.kcserver.vermietung.ical.PublicAirbnbTenantFilter.class);

        return http.build();
    }
}