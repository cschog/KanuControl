package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
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

class TeilnehmerBulkRemoveNoLeiterTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired TeilnehmerRepository teilnehmerRepository;

    Long veranstaltungId;
    Long leiterId;
    Long p1;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine = new VereinTestFactory(mockMvc, objectMapper, tenantAuth());
        PersonTestFactory personen = new PersonTestFactory(mockMvc, objectMapper, tenantAuth());
        VeranstaltungTestFactory veranstaltungen = new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        Long vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.createOrReuse("Max","Mustermann", LocalDate.of(1990,1,1), null);
        p1 = personen.createOrReuse("Peter","Test", LocalDate.of(2000,1,1), null);

        veranstaltungId = veranstaltungen.create(vereinId, leiterId, "Test");

        // Teilnehmer über Endpoint hinzufügen
        mockMvc.perform(
                tenantRequest(
                        post("/api/veranstaltung/{vId}/teilnehmer/{personId}",
                                veranstaltungId, p1)
                )
        ).andExpect(status().isCreated());
    }

    @Test
    void shouldRemoveBulkWithoutRemovingLeiter() throws Exception {

        TeilnehmerBulkDeleteDTO dto = new TeilnehmerBulkDeleteDTO();
        dto.setPersonIds(List.of(leiterId, p1)); // Leiter enthalten

        mockMvc.perform(
                tenantRequest(
                        delete("/api/veranstaltung/{id}/teilnehmer/bulk", veranstaltungId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
        ).andExpect(status().isNoContent());

        ensureTenantSchema();

        // Nur Leiter darf übrig bleiben
        assertEquals(1, teilnehmerRepository.countByVeranstaltungId(veranstaltungId));
    }
}