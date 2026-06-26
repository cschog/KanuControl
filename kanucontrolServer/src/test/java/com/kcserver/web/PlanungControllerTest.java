package com.kcserver.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.integration.AbstractFinanzIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlanungControllerTest extends AbstractFinanzIntegrationTest {

    @Autowired
    ObjectMapper objectMapper;

    /* =========================================================
       GET PLANUNG
       ========================================================= */

    @Test
    void shouldCreateAndGetPlanung() throws Exception {

        Long veranstaltungId = createTestVeranstaltung();

        mockMvc.perform(
                        get("/api/veranstaltungen/{id}/planung", veranstaltungId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.veranstaltungId").value(veranstaltungId));
    }

    /* =========================================================
       POSITION ANLEGEN
       ========================================================= */

    @Test
    void shouldAddPlanungPosition() throws Exception {

        Long veranstaltungId = createTestVeranstaltung();

        PlanungPositionCreateDTO dto = new PlanungPositionCreateDTO();
        dto.setKategorie(FinanzKategorie.VERPFLEGUNG);
        dto.setBetrag(new BigDecimal("100.00"));

        mockMvc.perform(
                        post("/api/veranstaltungen/{id}/planung/positionen", veranstaltungId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.kategorie").value("VERPFLEGUNG"))
                .andExpect(jsonPath("$.data.betrag").value(100.00));
    }
}