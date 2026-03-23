package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
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
                b.withVorname("Peter")
                        .withName("Test")
                        .withGeburtsdatum(java.time.LocalDate.of(2000, 1, 1))
        );

        veranstaltungId = veranstaltungen.create(vereinId, leiterId, "Test");

        // Teilnehmer über Endpoint hinzufügen
        mockMvc.perform(

                        post("/api/veranstaltungen/{vId}/teilnehmer/{personId}",
                                veranstaltungId, p1)

        ).andExpect(status().isCreated());
    }

    @Test
    void shouldRemoveBulkWithoutRemovingLeiter() throws Exception {

        TeilnehmerBulkDeleteDTO dto = new TeilnehmerBulkDeleteDTO();
        dto.setPersonIds(List.of(leiterId, p1)); // Leiter enthalten

        mockMvc.perform(

                        delete("/api/veranstaltungen/{id}/teilnehmer/bulk", veranstaltungId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)
                )
        ).andExpect(status().isNoContent());

        ensureTenantSchema();

        // Nur Leiter darf übrig bleiben
        assertEquals(1, teilnehmerRepository.countByVeranstaltungId(veranstaltungId));
    }
}