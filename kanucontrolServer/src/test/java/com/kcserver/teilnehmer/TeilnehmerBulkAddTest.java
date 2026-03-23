package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.teilnehmer.TeilnehmerAddBulkDTO;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeilnehmerBulkAddTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired TeilnehmerRepository teilnehmerRepository;

    Long veranstaltungId;
    Long leiterId;
    Long p1;
    Long p2;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine = new VereinTestFactory(mockMvc, objectMapper);
        PersonTestFactory personen = new PersonTestFactory(mockMvc, objectMapper);
        VeranstaltungTestFactory veranstaltungen = new VeranstaltungTestFactory(mockMvc, objectMapper);

        Long vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.create(b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
        );

        p1 = personen.create(b ->
                b.withVorname("Erika")
                        .withName("Musterfrau")
                        .withGeburtsdatum(java.time.LocalDate.of(2000, 1, 1))
        );

        p2 = personen.create(b ->
                b.withVorname("Peter")
                        .withName("Muster")
                        .withGeburtsdatum(java.time.LocalDate.of(2000, 1, 1))
        );

        veranstaltungId = veranstaltungen.create(vereinId, leiterId, "Test");

        ensureTenantSchema();
    }

    @Test
    void shouldAddBulkTeilnehmer() throws Exception {

        TeilnehmerAddBulkDTO dto = new TeilnehmerAddBulkDTO();
        dto.setPersonIds(List.of(p1, p2));

        mockMvc.perform(

                        post("/api/veranstaltungen/{id}/teilnehmer/bulk", veranstaltungId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))

        ).andExpect(status().isCreated());

        ensureTenantSchema();
        assertEquals(3, teilnehmerRepository.countByVeranstaltungId(veranstaltungId));
        // 2 neue + Leiter
    }
    @Test
    void shouldNotDuplicateBulkAdd() throws Exception {
        TeilnehmerAddBulkDTO dto = new TeilnehmerAddBulkDTO();
        dto.setPersonIds(List.of(p1, p2));

        // first
        mockMvc.perform(
                post("/api/veranstaltungen/{id}/teilnehmer/bulk", veranstaltungId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)
        )).andExpect(status().isCreated());

        // second (same persons)
        mockMvc.perform(
                post("/api/veranstaltungen/{id}/teilnehmer/bulk", veranstaltungId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)
        )).andExpect(status().isCreated());

        ensureTenantSchema();
        assertEquals(3, teilnehmerRepository.countByVeranstaltungId(veranstaltungId));
    }
}