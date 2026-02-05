package com.kcserver.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VeranstaltungTestFactory {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final RequestPostProcessor auth;

    public VeranstaltungTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            RequestPostProcessor auth
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.auth = auth;
    }

    /* =========================================================
       CREATE (immer aktiv)
       ========================================================= */

    public Long create(Long vereinId, Long leiterId, String name) throws Exception {

        VeranstaltungCreateDTO dto = new VeranstaltungCreateDTO();
        dto.setName(name);
        dto.setTyp(VeranstaltungTyp.JUGENDERHOLUNGSMASSNAHME);
        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);

        dto.setBeginnDatum(LocalDate.now().plusDays(10));
        dto.setBeginnZeit(LocalTime.of(10, 0));
        dto.setEndeDatum(LocalDate.now().plusDays(20));
        dto.setEndeZeit(LocalTime.of(18, 0));

        String json = mockMvc.perform(
                        post("/api/veranstaltung")
                                .with(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(json)
                .get("id")
                .asLong();
    }
    public Long createWithDate(
            Long vereinId,
            Long leiterId,
            String name,
            LocalDate beginnDatum
    ) throws Exception {

        VeranstaltungCreateDTO dto = new VeranstaltungCreateDTO();
        dto.setName(name);
        dto.setTyp(VeranstaltungTyp.JUGENDERHOLUNGSMASSNAHME);
        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);

        dto.setBeginnDatum(beginnDatum);
        dto.setBeginnZeit(LocalTime.of(10, 0));
        dto.setEndeDatum(beginnDatum.plusDays(5));
        dto.setEndeZeit(LocalTime.of(18, 0));

        String json = mockMvc.perform(
                        post("/api/veranstaltung")
                                .with(auth)   // ✅ korrekt
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(json)
                .get("id")
                .asLong();
    }
    /* =========================================================
       ADD NORMALER TEILNEHMER (Join-Entity)
       ========================================================= */

    public void addTeilnehmer(Long veranstaltungId, Long personId) throws Exception {

        // Veranstaltung aktiv setzen
        mockMvc.perform(
                put("/api/veranstaltung/{id}/aktiv", veranstaltungId)
                        .with(auth)
        ).andExpect(status().isOk());

        // Teilnehmer hinzufügen (POST /api/teilnehmer/{personId})
        mockMvc.perform(
                post("/api/teilnehmer/{personId}", personId)
                        .with(auth)
        ).andExpect(status().isCreated());
    }

    /* =========================================================
       CREATE INACTIVE
       ========================================================= */

    public Long createInactive(
            Long vereinId,
            Long leiterId,
            String name,
            Long aktivBleibenId
    ) throws Exception {

        Long newId = create(vereinId, leiterId, name);

        mockMvc.perform(
                put("/api/veranstaltung/{id}/aktiv", aktivBleibenId)
                        .with(auth)
        ).andExpect(status().isOk());

        return newId;
    }
}