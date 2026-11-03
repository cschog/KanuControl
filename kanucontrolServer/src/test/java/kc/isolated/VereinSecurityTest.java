package kc.isolated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.core.dto.verein.VereinDTO;
import com.kcserver.core.config.SecurityConfig;
import com.kcserver.core.controller.VereinController;
import com.kcserver.core.service.VereinService;
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

/** Echte HTTP- und Methoden-Security am Vereinscontroller ohne Datenbank oder Keycloak. */
@Tag("verein-security")
class VereinSecurityTest {
    @Configuration @EnableWebSecurity @EnableWebMvc @Import(SecurityConfig.class)
    static class Config {
        @Bean VereinService vereinService() { return mock(VereinService.class); }
        @Bean VereinController vereinController(VereinService service) { return new VereinController(service); }
        @Bean JwtDecoder decoder() { return mock(JwtDecoder.class); }
        @Bean TenantFilter tenantFilter() { return (request, response, chain) -> chain.doFilter(request, response); }
        @Bean PublicAirbnbTenantFilter publicFeedFilter() {
            return new PublicAirbnbTenantFilter(mock(AirbnbExportRegistry.class));
        }
        @Bean CorsConfigurationSource corsConfigurationSource() { return request -> new CorsConfiguration(); }
    }
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private VereinService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach void setup() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(Config.class);
        context.refresh();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        service = context.getBean(VereinService.class);
    }
    @AfterEach void close() { context.close(); }

    @Test
    void getAll_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/verein"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void getById_withoutJwt_returns401() throws Exception {
        mockMvc.perform(get("/api/verein/{id}", 1L))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void create_withoutJwt_returns401() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setName("Test Verein");
        dto.setAbk("TV");
        dto.setCountryCode("DE");

        mockMvc.perform(
                        post("/api/verein")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void update_withoutJwt_returns401() throws Exception {

        VereinDTO dto = new VereinDTO();
        dto.setName("Updated Verein");
        dto.setAbk("UV");
        dto.setCountryCode("DE");

        mockMvc.perform(
                        put("/api/verein/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void delete_withoutJwt_returns401() throws Exception {
        mockMvc.perform(delete("/api/verein/{id}", 1L))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    private List<MockHttpServletRequestBuilder> requests() throws Exception {
        VereinDTO dto = new VereinDTO();
        dto.setName("Test Verein"); dto.setAbk("TV"); dto.setCountryCode("DE");
        String body = objectMapper.writeValueAsString(dto);
        return List.of(get("/api/verein"), get("/api/verein/{id}", 1L),
                post("/api/verein").contentType(MediaType.APPLICATION_JSON).content(body),
                put("/api/verein/{id}", 1L).contentType(MediaType.APPLICATION_JSON).content(body),
                delete("/api/verein/{id}", 1L));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "KJFP"})
    void berechtigteRollenErreichenAlleFuenfAktionen(String role) throws Exception {
        int[] expected = {200, 200, 201, 200, 204};
        var requests = requests();
        for (int i = 0; i < requests.size(); i++) {
            mockMvc.perform(requests.get(i).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role))))
                    .andExpect(status().is(expected[i]));
        }
        verify(service).getAll();
        verify(service).getById(1L);
        verify(service).create(any());
        verify(service).update(eq(1L), any());
        verify(service).delete(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "VERMIETUNG", "HOMEPAGE_INTEGRATION"})
    void andereRollenErhalten403OhneServiceAufruf(String role) throws Exception {
        for (var request : requests()) {
            mockMvc.perform(request.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role))))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }
}
