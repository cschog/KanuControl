package com.kcserver.verein;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.VereinDTO;
import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinUpdateTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    Long vereinId;

    @BeforeEach
    void setup() throws Exception {
        vereinId = createVerein("Eschweiler Kanu Club", "EKC");
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void updateVerein_updatesFieldsSuccessfully() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Eschweiler KC");
        update.setAbk("EKC");

        mockMvc.perform(
                        put("/api/verein/{id}", vereinId)
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vereinId))
                .andExpect(jsonPath("$.name").value("Eschweiler KC"))
                .andExpect(jsonPath("$.abk").value("EKC"));
    }

    @Test
    void updateVerein_notFound_returns404() throws Exception {

        VereinDTO update = new VereinDTO();
        update.setName("Ghost Club");
        update.setAbk("GC");

        mockMvc.perform(
                put("/api/verein/{id}", 99999L)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)));


    }
}