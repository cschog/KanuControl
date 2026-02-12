package com.kcserver.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VeranstaltungTestFactory extends AbstractApiTestFactory {

    public VeranstaltungTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            RequestPostProcessor auth
    ) {
        super(mockMvc, objectMapper, auth);
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
                        req(post("/api/veranstaltung"))
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(json).get("id").asLong();
    }

    /* =========================================================
       ADD NORMALER TEILNEHMER
       ========================================================= */

    public void addTeilnehmer(Long veranstaltungId, Long personId) throws Exception {

        mockMvc.perform(
                post("/api/veranstaltung/{vId}/teilnehmer/{personId}",
                        veranstaltungId, personId)
                        .with(auth)
        ).andExpect(status().isCreated());
    }

    /* =========================================================
       CREATE MIT DATUM
       ========================================================= */

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
                        req(post("/api/veranstaltung"))
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(json).get("id").asLong();
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
                req(put("/api/veranstaltung/{id}/aktiv", aktivBleibenId))
        ).andExpect(status().isOk());

        return newId;
    }
}