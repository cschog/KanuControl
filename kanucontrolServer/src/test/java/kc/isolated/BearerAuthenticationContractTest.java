package kc.isolated;

import com.kcserver.core.config.SecurityConfig;
import com.kcserver.core.tenancy.*;
import com.kcserver.vermietung.ical.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BearerAuthenticationContractTest {
    @RestController static class Probe {
        @PostMapping("/api/security-contract") String write() { return TenantContext.getCurrentTenant(); }
    }
    @Configuration @EnableWebSecurity @EnableWebMvc @Import(SecurityConfig.class)
    static class Config {
        @Bean Probe probe(){return new Probe();}
        @Bean JwtDecoder decoder(){return mock(JwtDecoder.class);}
        @Bean TenantSchemaService schemas(){return mock(TenantSchemaService.class);}
        @Bean TenantFilter tenants(TenantSchemaService s){return new JwtTenantFilter(s);}
        @Bean PublicAirbnbTenantFilter feed(){return new PublicAirbnbTenantFilter(mock(AirbnbExportRegistry.class));}
        @Bean CorsConfigurationSource corsConfigurationSource(){return r->new CorsConfiguration();}
    }
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    @BeforeEach void setup(){
        context=new AnnotationConfigWebApplicationContext();context.setServletContext(new MockServletContext());
        context.register(Config.class);context.refresh();
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        when(context.getBean(JwtDecoder.class).decode("valid-token")).thenReturn(
                Jwt.withTokenValue("valid-token").header("alg","RS256").subject("user").claim("tenant","club_a").build());
    }
    @AfterEach void close(){TenantContext.clear();context.close();}
    @Test void cookiesAndTokenParametersCannotAuthenticateWrite() throws Exception {
        mvc.perform(post("/api/security-contract").cookie(new Cookie("access_token","valid-token"),new Cookie("JSESSIONID","guess")))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/security-contract").param("access_token","valid-token"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(context.getBean(JwtDecoder.class),context.getBean(TenantSchemaService.class));
    }
    @Test void bearerWriteNeedsNoCsrfTokenAndCreatesNoAuthenticationSession() throws Exception {
        var result=mvc.perform(post("/api/security-contract").header("Authorization","Bearer valid-token"))
                .andExpect(status().isOk()).andExpect(content().string("club_a")).andReturn();
        assertNull(result.getRequest().getSession(false));
        assertNull(TenantContext.getCurrentTenant());
        verify(context.getBean(TenantSchemaService.class)).initializeTenant("club_a");
        mvc.perform(post("/api/security-contract")).andExpect(status().isUnauthorized());
    }
    @Test void invalidBearerIsRejectedBeforeTenantRouting() throws Exception {
        when(context.getBean(JwtDecoder.class).decode("invalid")).thenThrow(new BadJwtException("invalid signature"));
        mvc.perform(post("/api/security-contract").header("Authorization","Bearer invalid"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(context.getBean(TenantSchemaService.class));
    }
    @Test void crossOriginCookieOnlyWriteIsRejected() throws Exception {
        mvc.perform(post("/api/security-contract").header("Origin","https://untrusted.example")
                .cookie(new Cookie("access_token","valid-token"))).andExpect(status().isForbidden());
        verifyNoInteractions(context.getBean(TenantSchemaService.class));
    }
    @Test void priorSessionAuthenticationCannotReplaceExplicitBearer() throws Exception {
        var session = new MockHttpSession();
        var saved = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        saved.setAuthentication(new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(
                Jwt.withTokenValue("old-token").header("alg","RS256").subject("user").claim("tenant","club_a").build()));
        saved.getAuthentication().setAuthenticated(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", saved);
        mvc.perform(post("/api/security-contract").session(session)).andExpect(status().isUnauthorized());
        verifyNoInteractions(context.getBean(TenantSchemaService.class));
    }
}
