package com.kcserver.verein;

import com.kcserver.test.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("verein-crud")
class VereinReadTest extends AbstractIntegrationTest {

    Long vereinId;

    @BeforeEach
    void setup() throws Exception {
        vereinId = createVerein("Eschweiler Kanu Club", "EKC");
    }

    @Test
    void getAll_returnsAllVereine() throws Exception {

        createVerein("Oberhausener Kanu Club", "OKC");

        mockMvc.perform(get("/api/verein").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].abk").exists());
    }

    @Test
    void getById_returnsVerein() throws Exception {

        mockMvc.perform(get("/api/verein/{id}", vereinId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vereinId))
                .andExpect(jsonPath("$.name").value("Eschweiler Kanu Club"))
                .andExpect(jsonPath("$.abk").value("EKC"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {

        mockMvc.perform(get("/api/verein/{id}", 99999L).with(jwt()))
                .andExpect(status().isNotFound());
    }
}