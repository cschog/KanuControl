package com.kcserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void applicationStarts_and_securedEndpointIsReachable() throws Exception {
        mockMvc.perform(get("/api/person")
                        .header("X-Tenant", "ekc_test")
                        .header("Authorization", "Bearer dummy"))
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
    void securedEndpoint_withJwt_andTenantHeader_returns200() throws Exception {

        mockMvc.perform(
                        get("/api/person")
                                .header("X-Tenant", "ekc_test")
                                .with(jwt())
                )
                .andExpect(status().isOk());
    }
}
