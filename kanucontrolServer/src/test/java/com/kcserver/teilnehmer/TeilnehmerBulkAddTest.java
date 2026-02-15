package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.teilnehmer.TeilnehmerAddBulkDTO;
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

        VereinTestFactory vereine = new VereinTestFactory(mockMvc, objectMapper, tenantAuth());
        PersonTestFactory personen = new PersonTestFactory(mockMvc, objectMapper, tenantAuth());
        VeranstaltungTestFactory veranstaltungen = new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        Long vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.createOrReuse("Max","Mustermann", LocalDate.of(1990,1,1), null);
        p1 = personen.createOrReuse("Erika","Musterfrau", LocalDate.of(2000,1,1), null);
        p2 = personen.createOrReuse("Peter","Muster", LocalDate.of(2001,1,1), null);

        veranstaltungId = veranstaltungen.create(vereinId, leiterId, "Test");

        ensureTenantSchema();
    }

    @Test
    void shouldAddBulkTeilnehmer() throws Exception {

        TeilnehmerAddBulkDTO dto = new TeilnehmerAddBulkDTO();
        dto.setPersonIds(List.of(p1, p2));

        mockMvc.perform(
                tenantRequest(
                        post("/api/veranstaltung/{id}/teilnehmer/bulk", veranstaltungId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
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
        mockMvc.perform(tenantRequest(
                post("/api/veranstaltung/{id}/teilnehmer/bulk", veranstaltungId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )).andExpect(status().isCreated());

        // second (same persons)
        mockMvc.perform(tenantRequest(
                post("/api/veranstaltung/{id}/teilnehmer/bulk", veranstaltungId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )).andExpect(status().isCreated());

        ensureTenantSchema();
        assertEquals(3, teilnehmerRepository.countByVeranstaltungId(veranstaltungId));
    }
}