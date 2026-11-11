package kc.isolated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.vermietung.dto.VeranstalterDTO;
import com.kcserver.core.config.SecurityConfig;
import com.kcserver.vermietung.controller.VeranstalterController;
import com.kcserver.vermietung.service.VeranstalterService;
import com.kcserver.core.tenancy.TenantFilter;
import com.kcserver.vermietung.ical.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Echte HTTP- und Methoden-Security am Veranstaltercontroller ohne Datenbank oder Keycloak. */
@Tag("veranstalter-security")
class VeranstalterSecurityTest {
    @Configuration @EnableWebSecurity @EnableWebMvc @Import(SecurityConfig.class)
    static class Config {
        @Bean VeranstalterService vereinService() { return mock(VeranstalterService.class); }
        @Bean VeranstalterController vereinController(VeranstalterService service) { return new VeranstalterController(service); }
        @Bean JwtDecoder decoder() { return mock(JwtDecoder.class); }
        @Bean TenantFilter tenantFilter() { return (request, response, chain) -> chain.doFilter(request, response); }
        @Bean PublicAirbnbTenantFilter publicFeedFilter() {
            return new PublicAirbnbTenantFilter(mock(AirbnbExportRegistry.class));
        }
        @Bean CorsConfigurationSource corsConfigurationSource() { return request -> new CorsConfiguration(); }
    }
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private VeranstalterService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach void setup() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(Config.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        service = context.getBean(VeranstalterService.class);
    }
    @AfterEach void close() { context.close(); }

    @Test
    void getAll_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/vermietung/veranstalter"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void getById_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/vermietung/veranstalter/{id}", 1L))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void create_withoutJwt_returns401() throws Exception {

        VeranstalterDTO dto = new VeranstalterDTO();
        dto.setName("Test Verein");
        dto.setAbk("TV");
        dto.setCountryCode("DE");

        mockMvc.perform(
                        post("/api/vermietung/veranstalter")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void update_withoutJwt_returns401() throws Exception {

        VeranstalterDTO dto = new VeranstalterDTO();
        dto.setName("Updated Verein");
        dto.setAbk("UV");
        dto.setCountryCode("DE");

        mockMvc.perform(
                        put("/api/vermietung/veranstalter/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    private List<MockHttpServletRequestBuilder> requests() throws Exception {
        VeranstalterDTO dto = new VeranstalterDTO();
        dto.setName("Test Verein"); dto.setAbk("TV"); dto.setCountryCode("DE");
        String body = objectMapper.writeValueAsString(dto);
        return List.of(get("/api/vermietung/veranstalter"), get("/api/vermietung/veranstalter/{id}", 1L),
                post("/api/vermietung/veranstalter").contentType(MediaType.APPLICATION_JSON).content(body),
                put("/api/vermietung/veranstalter/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "VERMIETUNG"})
    void berechtigteRollenErreichenAlleAktionen(String role) throws Exception {
        int[] expected = {200, 200, 201, 200};
        var requests = requests();
        for (int i = 0; i < requests.size(); i++) {
            mockMvc.perform(requests.get(i).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role))))
                    .andExpect(status().is(expected[i]));
        }
        verify(service).suchen(null);
        verify(service).lesen(1L);
        verify(service).speichern(isNull(), any());
        verify(service).speichern(eq(1L), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "KJFP", "HOMEPAGE_INTEGRATION"})
    void andereRollenErhalten403OhneServiceAufruf(String role) throws Exception {
        for (var request : requests()) {
            mockMvc.perform(request.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role))))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }
}
