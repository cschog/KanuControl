package com.kcserver.support.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.support.web.AbstractApiTestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


public class VeranstaltungTestFactory extends AbstractApiTestFactory {

    public VeranstaltungTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper
    ) {
        super(mockMvc, objectMapper, null);
    }

    /* =========================================================
       CREATE
       ========================================================= */

    public Long create(Long vereinId, Long leiterId, String name) throws Exception {

        VeranstaltungCreateDTO dto = new VeranstaltungCreateDTO();
        dto.setName(name);
        dto.setTyp(VeranstaltungTyp.JEM);
        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);

        dto.setBeginnDatum(LocalDate.now().plusDays(10));
        dto.setBeginnZeit(LocalTime.of(10, 0));
        dto.setEndeDatum(LocalDate.now().plusDays(20));
        dto.setEndeZeit(LocalTime.of(18, 0));

        String json = mockMvc.perform(
                        post("/api/veranstaltungen")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(json).get("id").asLong();
    }

    /* =========================================================
       ADD TEILNEHMER
       ========================================================= */

    public void addTeilnehmer(Long veranstaltungId, Long personId) throws Exception {

        mockMvc.perform(
                post("/api/veranstaltungen/{vId}/teilnehmer/{personId}",
                        veranstaltungId, personId)
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
        dto.setTyp(VeranstaltungTyp.JEM);
        dto.setVereinId(vereinId);
        dto.setLeiterId(leiterId);

        dto.setBeginnDatum(beginnDatum);
        dto.setBeginnZeit(LocalTime.of(10, 0));
        dto.setEndeDatum(beginnDatum.plusDays(5));
        dto.setEndeZeit(LocalTime.of(18, 0));

        String json = mockMvc.perform(
                        post("/api/veranstaltungen")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(json).get("id").asLong();
    }

    /* =========================================================
       SET ACTIVE
       ========================================================= */

    public void setActive(Long id) throws Exception {
        mockMvc.perform(
                put("/api/veranstaltungen/{id}/aktiv", id)
        ).andExpect(status().isOk());
    }
    public Long createInactive(
            Long vereinId,
            Long leiterId,
            String name,
            Long aktivBleibenId
    ) throws Exception {

        Long newId = create(vereinId, leiterId, name);

        mockMvc.perform(
                        put("/api/veranstaltungen/{id}/aktiv", aktivBleibenId)
                )
                .andDo(print())   // 👈 WICHTIG
                .andExpect(status().isOk());

        return newId;
    }
}