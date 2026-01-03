package com.kcserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* Der Test prüft genau das, was ein SmokeTest soll:
        •	Spring Context startet
	•	Security greift
	•	Tenant-Header wird ausgewertet
	•	Filter-Kette funktioniert*/

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applicationStarts_and_securedEndpointRequiresAuth() throws Exception {
        mockMvc.perform(
                        get("/api/person")
                                .header("X-Tenant", "ekc_test")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withoutTenantHeader_returns400() throws Exception {
        mockMvc.perform(
                        get("/api/person")
                                .with(jwt())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void securedEndpoint_withJwt_andTenantHeader_isReachable() throws Exception {
        mockMvc.perform(
                        get("/api/person")
                                .header("X-Tenant", "ekc_test")
                                .with(jwt())
                )
                .andExpect(status().is2xxSuccessful());
    }
}