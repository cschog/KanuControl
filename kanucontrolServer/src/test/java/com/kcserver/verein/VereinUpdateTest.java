package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.verein.VereinDTO;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.api.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinUpdateTest extends AbstractTenantIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper);

        vereinId = vereine.createIfNotExists(
                "EKC",
                "Eschweiler Kanu Club"
        );
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void updateVerein_updatesFieldsSuccessfully() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Eschweiler KC");
        update.setAbk("EKC");
        update.setCountryCode("DE");

        mockMvc.perform(

                                put("/api/verein/{id}", vereinId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))

                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(vereinId))
                .andExpect(jsonPath("$.data.name").value("Eschweiler KC"))
                .andExpect(jsonPath("$.data.abk").value("EKC"));
    }

    @Test
    void updateVerein_notFound_returns404() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Ghost Club");
        update.setAbk("GC");
        update.setCountryCode("DE");

        mockMvc.perform(

                                put("/api/verein/{id}", 99999L)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(update))

                )
                .andExpect(status().isNotFound());
    }
}