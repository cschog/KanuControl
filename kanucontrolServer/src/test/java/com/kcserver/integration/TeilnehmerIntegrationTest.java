package com.kcserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.core.dto.teilnehmer.TeilnehmerUpdateDTO;

import com.kcserver.core.enumtype.TeilnehmerRolle;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;

import com.kcserver.core.repository.TeilnehmerRepository;
import com.kcserver.support.api.TeilnehmerTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeilnehmerIntegrationTest extends AbstractTenantIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired TeilnehmerRepository teilnehmerRepository;

    Long veranstaltungId;
    Long personId;

    /* =========================================================
       SETUP
       ========================================================= */

    TeilnehmerTestFactory factory;
    PersonTestFactory personFactory;
    VereinTestFactory vereinFactory;

    @BeforeEach
    void setup() throws Exception {

        factory = new TeilnehmerTestFactory(mockMvc, objectMapper);
        personFactory = new PersonTestFactory(mockMvc, objectMapper);
        vereinFactory = new VereinTestFactory(mockMvc, objectMapper);

        // 1️⃣ Verein
        Long vereinId = vereinFactory.create("TV", "Testverein");

        // 2️⃣ Leiter
        Long leiterId = personFactory.createWithVerein(vereinId, b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
        );

        // 3️⃣ Veranstaltung
        veranstaltungId = factory.createActiveVeranstaltung(vereinId, leiterId);

        // 4️⃣ Teilnehmer
        personId = personFactory.createWithVerein(vereinId, b ->
                b.withVorname("Anna")
                        .withName("Test")
                        .withGeburtsdatum(java.time.LocalDate.of(2000, 1, 1))
        );
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @Test
    void shouldCreateTeilnehmer() throws Exception {

        Long teilnehmerId = factory.addTeilnehmer(veranstaltungId, personId);

        ensureTenantSchema();
        assertTrue(teilnehmerRepository.findById(teilnehmerId).isPresent());
    }

    /* =========================================================
       PAGED READ
       ========================================================= */

    @Test
    void shouldReturnPagedTeilnehmer() throws Exception {

        mockMvc.perform(
                post("/api/veranstaltungen/{vId}/teilnehmer/{personId}", veranstaltungId, personId)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/veranstaltungen/{vId}/teilnehmer?page=0&size=10", veranstaltungId)
                )
                .andExpect(status().isOk());
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @Test
    void shouldUpdateTeilnehmer() throws Exception {

        Long id = factory.addTeilnehmer(veranstaltungId, personId);

        TeilnehmerUpdateDTO dto = new TeilnehmerUpdateDTO();
        dto.setRolle(TeilnehmerRolle.MITARBEITER);

        mockMvc.perform(

                        put("/api/veranstaltungen/{vId}/teilnehmer/{id}",
                                veranstaltungId,
                                id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)
                )
        ).andExpect(status().isOk());
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Test
    void shouldDeleteTeilnehmer() throws Exception {

        Long id = factory.addTeilnehmer(veranstaltungId, personId);
        mockMvc.perform(
                        delete("/api/veranstaltungen/{vId}/teilnehmer/{id}", veranstaltungId, id))
                .andExpect(status().isNoContent());

        ensureTenantSchema();
        assertFalse(teilnehmerRepository.findById(id).isPresent());
    }

    /* =========================================================
       DUPLICATE PROTECTION
       ========================================================= */

    @Test
    void shouldRejectDuplicateTeilnehmer() throws Exception {

        factory.addTeilnehmer(veranstaltungId, personId);
        mockMvc.perform(
                        post("/api/veranstaltungen/{vId}/teilnehmer/{personId}", veranstaltungId, personId))
                .andExpect(status().isConflict());
    }
    @Test
    void allgemeineTeilnahmeFunktioniertOhneKjfpUndBleibtVomFachmodulGetrennt() throws Exception {
        ensureTenantSchema();
        jdbcTemplate.update("UPDATE veranstaltung SET kjfp_aktiv=false, fahrten_aktiv=true WHERE id=?", veranstaltungId);
        String response = mockMvc.perform(post(
                        "/api/veranstaltungen/{v}/teilnehmer/allgemein/{p}", veranstaltungId, personId))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        var data = objectMapper.readTree(response).path("data");
        assertFalse(data.path("kjfpZugeordnet").asBoolean());
        long teilnehmerId = data.path("id").asLong();
        assertTrue(teilnehmerId > 0);
        mockMvc.perform(put("/api/veranstaltungen/{v}/teilnehmer/{t}/kjfp-zuordnung", veranstaltungId, teilnehmerId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"zugeordnet\":true}"))
                .andExpect(status().isConflict());
    }

    @Test
    void kjfpZuordnungFunktioniertNachControllerTrennung() throws Exception {
        String response = mockMvc.perform(post(
                        "/api/veranstaltungen/{v}/teilnehmer/allgemein/{p}", veranstaltungId, personId))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long teilnehmerId = objectMapper.readTree(response).path("data").path("id").asLong();
        for (boolean zugeordnet : new boolean[]{true, false}) {
            String changed = mockMvc.perform(put(
                            "/api/veranstaltungen/{v}/teilnehmer/{t}/kjfp-zuordnung", veranstaltungId, teilnehmerId)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"zugeordnet\":" + zugeordnet + "}"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            org.junit.jupiter.api.Assertions.assertEquals(zugeordnet,
                    objectMapper.readTree(changed).path("data").path("kjfpZugeordnet").asBoolean());
        }
    }
}