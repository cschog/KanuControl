package com.kcserver.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.integration.AbstractFinanzIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlanungWorkflowTest extends AbstractFinanzIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldCreatePlanungBySavingSimulation() throws Exception {

        Long veranstaltungId = createTestVeranstaltung();

        // Simulation laden
        String response = mockMvc.perform(
                        get("/api/simulation/{id}", veranstaltungId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode simulation = objectMapper
                .readTree(response)
                .get("data");

        // Simulation speichern -> erzeugt Planung
        mockMvc.perform(
                        put("/api/simulation/{id}", veranstaltungId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(simulation)))
                .andExpect(status().isNoContent());

        // Planung muss jetzt existieren
        mockMvc.perform(
                        get("/api/veranstaltungen/{id}/planung", veranstaltungId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.veranstaltungId").value(veranstaltungId));
    }
}