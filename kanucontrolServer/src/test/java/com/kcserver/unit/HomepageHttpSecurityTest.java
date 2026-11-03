package com.kcserver.unit;

import com.kcserver.core.config.SecurityConfig;
import com.kcserver.core.tenancy.TenantFilter;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomepageHttpSecurityTest {
    @RestController static class Probe {
        @GetMapping({"/api/probe", "/api/active-schema", "/api/integrationen/homepage/probe"}) String ok() { return "ok"; }
    }
    @Configuration @EnableWebSecurity @EnableWebMvc @Import(SecurityConfig.class)
    static class Config {
        @Bean Probe probe() { return new Probe(); }
        @Bean com.kcserver.vermietung.ical.PublicAirbnbTenantFilter publicFeedFilter() {
            return new com.kcserver.vermietung.ical.PublicAirbnbTenantFilter(mock(com.kcserver.vermietung.ical.AirbnbExportRegistry.class));
        }
        @Bean JwtDecoder decoder() { return mock(JwtDecoder.class); }
        @Bean TenantFilter tenantFilter() { return (request,response,chain) -> chain.doFilter(request,response); }
        @Bean CorsConfigurationSource corsConfigurationSource() { return request -> new CorsConfiguration(); }
    }
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    @BeforeEach void setup() {
        context = new AnnotationConfigWebApplicationContext(); context.setServletContext(new MockServletContext());
        context.register(Config.class); context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    @AfterEach void close() { context.close(); }
    @Test void technischeRolleBleibtAufIntegrationspfadeBegrenzt() throws Exception {
        var auth = jwt().authorities(new SimpleGrantedAuthority("ROLE_HOMEPAGE_INTEGRATION"));
        mvc.perform(get("/api/integrationen/homepage/probe").with(auth)).andExpect(status().isOk());
        mvc.perform(get("/api/probe").with(auth)).andExpect(status().isForbidden());
        mvc.perform(get("/api/active-schema").with(auth)).andExpect(status().isForbidden());
    }
    @Test void normaleNutzerUndAnonymeErhaltenKeineIntegrationsrechte() throws Exception {
        mvc.perform(get("/api/integrationen/homepage/probe").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isForbidden());
        mvc.perform(get("/api/probe").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk());
        mvc.perform(get("/api/probe")).andExpect(status().isUnauthorized());
    }
}
