package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerUpdateDTO;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.testdata.PersonTestFactory;
import com.kcserver.testdata.VeranstaltungTestFactory;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeilnehmerTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired TeilnehmerRepository teilnehmerRepository;

    Long veranstaltungId;
    Long leiterId;
    Long personId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine =
                new VereinTestFactory(mockMvc, objectMapper, tenantAuth());

        PersonTestFactory personen =
                new PersonTestFactory(mockMvc, objectMapper, tenantAuth());

        VeranstaltungTestFactory veranstaltungen =
                new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        Long vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.createOrReuse(
                "Max", "Mustermann",
                LocalDate.of(1990, 1, 1), null
        );

        personId = personen.createOrReuse(
                "Erika", "Musterfrau",
                LocalDate.of(2002, 5, 5), null
        );

        veranstaltungId = veranstaltungen.create(
                vereinId,
                leiterId,
                "Sommerfreizeit"
        );

        ensureTenantSchema();
        System.out.println("Teilnehmer:");
        teilnehmerRepository.findAll().forEach(t ->
                System.out.println(
                        "V=" + t.getVeranstaltung().getId() +
                                " P=" + t.getPerson().getId() +
                                " R=" + t.getRolle()
                )
        );
    }

    /* =========================================================
       ADD
       ========================================================= */

    @Test
    void shouldAddTeilnehmer() throws Exception {

        mockMvc.perform(
                tenantRequest(
                        post("/api/veranstaltung/{vId}/teilnehmer/{personId}",
                                veranstaltungId, personId)
                )
        ).andExpect(status().isCreated());
    }

    /* =========================================================
       REMOVE
       ========================================================= */

    @Test
    void shouldRemoveTeilnehmer() throws Exception {

        mockMvc.perform(
                tenantRequest(
                        post("/api/veranstaltung/{vId}/teilnehmer/{personId}",
                                veranstaltungId, personId)
                )
        ).andExpect(status().isCreated());

        ensureTenantSchema();
        Long teilnehmerId = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, personId)
                .orElseThrow()
                .getId();

        mockMvc.perform(
                tenantRequest(
                        delete("/api/veranstaltung/{vId}/teilnehmer/{id}",
                                veranstaltungId, teilnehmerId)
                )
        ).andExpect(status().isNoContent());
    }

    /* =========================================================
       PREVENT REMOVE LEITER
       ========================================================= */

    @Test
    void shouldPreventRemovingLeiter() throws Exception {

        ensureTenantSchema();
        Long leiterTeilnehmerId = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, leiterId)
                .orElseThrow()
                .getId();

        mockMvc.perform(
                tenantRequest(
                        delete("/api/veranstaltung/{vId}/teilnehmer/{id}",
                                veranstaltungId, leiterTeilnehmerId)
                )
        ).andExpect(status().isConflict());
    }

    /* =========================================================
       BULK DELETE
       ========================================================= */

    @Test
    void shouldBulkDeleteTeilnehmer() throws Exception {

        mockMvc.perform(
                tenantRequest(
                        post("/api/veranstaltung/{vId}/teilnehmer/{personId}",
                                veranstaltungId, personId)
                )
        ).andExpect(status().isCreated());

        TeilnehmerBulkDeleteDTO dto = new TeilnehmerBulkDeleteDTO();
        dto.setPersonIds(List.of(personId));

        mockMvc.perform(
                tenantRequest(
                        delete("/api/veranstaltung/{vId}/teilnehmer/bulk", veranstaltungId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
        ).andExpect(status().isNoContent());
    }

    /* =========================================================
       COUNT BY ROLLE
       ========================================================= */

    @Test
    void shouldCountByRolle() throws Exception {

        mockMvc.perform(
                tenantRequest(
                        post("/api/veranstaltung/{vId}/teilnehmer/{personId}",
                                veranstaltungId, personId)
                )
        ).andExpect(status().isCreated());

        ensureTenantSchema();
        Long teilnehmerId = teilnehmerRepository
                .findByVeranstaltungIdAndPersonId(veranstaltungId, personId)
                .orElseThrow()
                .getId();

        TeilnehmerUpdateDTO dto = new TeilnehmerUpdateDTO();
        dto.setRolle(TeilnehmerRolle.MITARBEITER);

        mockMvc.perform(
                tenantRequest(
                        put("/api/veranstaltung/{vId}/teilnehmer/{id}",
                                veranstaltungId, teilnehmerId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
        ).andExpect(status().isOk());

        ensureTenantSchema();
        long countLeiter =
                teilnehmerRepository.countByVeranstaltungIdAndRolle(
                        veranstaltungId, TeilnehmerRolle.LEITER);

        ensureTenantSchema();
        long countMitarbeiter =
                teilnehmerRepository.countByVeranstaltungIdAndRolle(
                        veranstaltungId, TeilnehmerRolle.MITARBEITER);
        ensureTenantSchema();
        long countNormale =
                teilnehmerRepository.countByVeranstaltungIdAndRolleIsNull(
                        veranstaltungId);

        assertEquals(1, countLeiter);
        assertEquals(1, countMitarbeiter);
        assertEquals(0, countNormale);
    }
}