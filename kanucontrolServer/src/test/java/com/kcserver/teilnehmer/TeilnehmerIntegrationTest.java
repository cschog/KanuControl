package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.teilnehmer.TeilnehmerUpdateDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.VereinRepository;
import com.kcserver.testdata.DomainTestFactory;
import com.kcserver.testdata.TeilnehmerTestFactory;
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
    @Autowired VeranstaltungRepository veranstaltungRepository;
    @Autowired PersonRepository personRepository;
    @Autowired VereinRepository vereinRepository;

    Long veranstaltungId;
    Long personId;

    /* =========================================================
       SETUP
       ========================================================= */

    TeilnehmerTestFactory factory;

    @BeforeEach
    void setup() throws Exception {

        // ⭐ garantiert Tenant aktiv
        ensureTenantSchema();

        // ⭐ Factory JETZT erzeugen (nicht vorher!)
        factory = new TeilnehmerTestFactory(mockMvc, objectMapper, tenantAuth());

        ensureTenantSchema();
        Person leiter = personRepository.save(DomainTestFactory.validPerson());

        ensureTenantSchema();
        Verein verein = vereinRepository.save(DomainTestFactory.validVerein());

        veranstaltungId = factory.createActiveVeranstaltung(verein.getId(), leiter.getId());

        ensureTenantSchema();
        Person p = personRepository.save(DomainTestFactory.validPerson());
        personId = p.getId();
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
                tenantRequest(post("/api/veranstaltung/{vId}/teilnehmer/{personId}", veranstaltungId, personId))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                        tenantRequest(get("/api/veranstaltung/{vId}/teilnehmer?page=0&size=10", veranstaltungId))
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
                        tenantRequest(put("/api/veranstaltung/{vId}/teilnehmer/{id}", veranstaltungId, id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @Test
    void shouldDeleteTeilnehmer() throws Exception {

        Long id = factory.addTeilnehmer(veranstaltungId, personId);
        mockMvc.perform(
                        tenantRequest(delete("/api/veranstaltung/{vId}/teilnehmer/{id}", veranstaltungId, id)))
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
                        tenantRequest(post("/api/veranstaltung/{vId}/teilnehmer/{personId}", veranstaltungId, personId)))
                .andExpect(status().isConflict());
    }
}