package com.kcserver.vermietung.zaehlerimport;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.List;
import java.util.HexFormat;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@Profile("!junit")
public class MeterImportSecurity {
    public static final String PATH = "/api/integrationen/zaehler-import";

    @Bean @Order(1)
    SecurityFilterChain meterImportChain(HttpSecurity http, MeterImportConfig config) throws Exception {
        return http.securityMatcher(PATH, PATH + "/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.requestMatchers(org.springframework.http.HttpMethod.POST, PATH)
                .hasRole("METER_IMPORT").anyRequest().denyAll())
            .addFilterBefore(new KeyFilter(config), AnonymousAuthenticationFilter.class)
            .build();
    }

    static class KeyFilter extends OncePerRequestFilter {
        private final MeterImportConfig config;
        KeyFilter(MeterImportConfig config) { this.config = config; }
        @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            response.setHeader("Cache-Control", "no-store");
            String key = request.getHeader("X-KC-Import-Key");
            if (!config.configured() || key == null || key.length() < 32 || key.length() > 256
                    || !MessageDigest.isEqual(hash(key), HexFormat.of().parseHex(config.getKeyHash()))) {
                response.setStatus(401);
                return;
            }
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken("ha-meter-import", null,
                List.of(new SimpleGrantedAuthority("ROLE_METER_IMPORT"))));
            SecurityContextHolder.setContext(context);
            try { chain.doFilter(request, response); }
            finally { SecurityContextHolder.clearContext(); }
        }
        private byte[] hash(String value) {
            try { return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); }
            catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        }
    }
}
