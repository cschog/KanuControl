package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinReadTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists(
                "EKC",
                "Eschweiler Kanu Club"
        );
    }

    @Test
    void getAll_returnsAllVereine() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        vereine.createIfNotExists(
                "OKC",
                "Oberhausener Kanu Club"
        );

        mockMvc.perform(
                        tenantRequest(get("/api/verein"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].abk").exists());
    }

    @Test
    void getById_returnsVerein() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/verein/{id}", vereinId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vereinId))
                .andExpect(jsonPath("$.name").value("Eschweiler Kanu Club"))
                .andExpect(jsonPath("$.abk").value("EKC"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {

        mockMvc.perform(
                        tenantRequest(get("/api/verein/{id}", 99999L))
                )
                .andExpect(status().isNotFound());
    }
}