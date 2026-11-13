package kc.isolated;

import com.kcserver.core.config.SecurityConfig;
import com.kcserver.core.tenancy.*;
import com.kcserver.vermietung.ical.*;
import com.kcserver.vermietung.zaehlerimport.*;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.HexFormat;
import java.security.MessageDigest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MeterImportHttpSecurityTest {
    static final String KEY = "a".repeat(43), PATH = MeterImportSecurity.PATH;
    static final String BODY = """
        {"tenant":"ekc_eschweilerkanuclub","meterId":1,"measuredAt":"2026-11-12T12:00:00Z",
        "value":123.456,"unit":"kWh","remark":"Automatischer Import von Home Assistant"}
        """;
    @Configuration @EnableWebSecurity @EnableWebMvc
    @Import({SecurityConfig.class, MeterImportSecurity.class})
    static class Config {
        @Bean MeterImportConfig settings() throws Exception {
            var c = new MeterImportConfig(); c.setEnabled(true); c.setTenant("ekc_eschweilerkanuclub");
            c.setMeterId(1L); c.setMeterNumber("9728276");
            c.setKeyHash(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
            return c;
        }
        @Bean MeterImportService service() { return mock(MeterImportService.class); }
        @Bean MeterImportController controller(MeterImportService s) { return new MeterImportController(s); }
        @Bean AirbnbExportRegistry registry() { return mock(AirbnbExportRegistry.class); }
        @Bean TenantSchemaService schemas() { return mock(TenantSchemaService.class); }
        @Bean PublicAirbnbTenantFilter feed(AirbnbExportRegistry r) { return new PublicAirbnbTenantFilter(r); }
        @Bean TenantFilter tenantFilter(TenantSchemaService s) { return new JwtTenantFilter(s); }
        @Bean JwtDecoder decoder() { return mock(JwtDecoder.class); }
        @Bean CorsConfigurationSource corsConfigurationSource() { return request -> new CorsConfiguration(); }
    }
    AnnotationConfigWebApplicationContext ctx;
    MockMvc mvc;
    @BeforeEach void setup() throws Exception {
        ctx = new AnnotationConfigWebApplicationContext(); ctx.setServletContext(new MockServletContext());
        ctx.register(Config.class); ctx.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(ctx).apply(springSecurity()).build();
        when(ctx.getBean(MeterImportService.class).ingest(any())).thenReturn(new MeterImportController.Result(10, true));
    }
    @AfterEach void close() { ctx.close(); }
    @Test void scopedKeyNeedsNoJwt() throws Exception {
        mvc.perform(post(PATH).header("X-KC-Import-Key",KEY).contentType("application/json").content(BODY))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(10)).andExpect(header().string("Cache-Control","no-store"));
        verifyNoInteractions(ctx.getBean(TenantSchemaService.class));
        assertNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
    }
    @Test void missingWrongAndDisabledKeyFailClosed() throws Exception {
        mvc.perform(post(PATH).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized());
        mvc.perform(post(PATH).header("X-KC-Import-Key","b".repeat(43))).andExpect(status().isUnauthorized());
        ctx.getBean(MeterImportConfig.class).setEnabled(false);
        mvc.perform(post(PATH).header("X-KC-Import-Key",KEY)).andExpect(status().isUnauthorized());
        verifyNoInteractions(ctx.getBean(MeterImportService.class));
    }
    @Test void keyCannotReadOrAccessOtherApi() throws Exception {
        mvc.perform(get(PATH).header("X-KC-Import-Key",KEY)).andExpect(status().isForbidden());
        mvc.perform(post(PATH+"/other").header("X-KC-Import-Key",KEY)).andExpect(status().isForbidden());
        mvc.perform(get("/api/vermietung/zaehler").header("X-KC-Import-Key",KEY)).andExpect(status().isUnauthorized());
    }
    @Test void invalidSensorValuesAndMissingOffsetAreRejectedBeforeService() throws Exception {
        for (String value : new String[]{"null", "-1", "\"unknown\"", "\"unavailable\"", "\"NaN\"", "\"Infinity\"", "1000000000000", "0.0001"})
            mvc.perform(post(PATH).header("X-KC-Import-Key",KEY).contentType("application/json").content(BODY.replace("123.456",value))).andExpect(status().isBadRequest());
        mvc.perform(post(PATH).header("X-KC-Import-Key",KEY).contentType("application/json").content(BODY.replace("12:00:00Z","12:00:00"))).andExpect(status().isBadRequest());
        verifyNoInteractions(ctx.getBean(MeterImportService.class));
    }
}
