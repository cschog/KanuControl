package com.kcserver.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FinanzGruppeControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getFinanzgruppen_shouldReturn200() throws Exception {

        mockMvc.perform(
                        get("/api/veranstaltungen/1/finanzgruppen")
                                .with(tenantJwt())
                )
                .andExpect(status().isOk());
    }
    protected RequestPostProcessor tenantJwt() {
        return jwt().jwt(jwt -> jwt
                .claim("tenant", "ekc_test")
        );
    }
}