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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinDeleteTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        vereinId = vereine.createIfNotExists(
                "EKC_DELETE",
                "Eschweiler Kanu Club"
        );
    }

    @Test
    void deleteVerein_existing_returns204_andRemovesVerein() throws Exception {

        mockMvc.perform(
                tenantRequest(delete("/api/verein/{id}", vereinId))
        ).andExpect(status().isNoContent());

        mockMvc.perform(
                tenantRequest(get("/api/verein/{id}", vereinId))
        ).andExpect(status().isNotFound());
    }

    @Test
    void deleteVerein_notFound_returns404() throws Exception {

        mockMvc.perform(
                tenantRequest(delete("/api/verein/{id}", 99999L))
        ).andExpect(status().isNotFound());
    }
}