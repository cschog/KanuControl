package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeilnehmerDuplicateTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;

    Long veranstaltungId;
    Long leiterId;
    Long personId;

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


        personId = personen.create(b ->
                b.withVorname("Erika")
                        .withName("Musterfrau")
                        .withGeburtsdatum(java.time.LocalDate.of(2000, 1, 1))
        );

        veranstaltungId = veranstaltungen.create(vereinId, leiterId, "Test");

        ensureTenantSchema();
    }

    @Test
    void shouldPreventDuplicateTeilnehmer() throws Exception {

        // 1. add ok
        mockMvc.perform(
                post("/api/veranstaltungen/{v}/teilnehmer/{p}", veranstaltungId, personId)
        ).andExpect(status().isCreated());

        // 2. add again → CONFLICT
        mockMvc.perform(
                post("/api/veranstaltungen/{v}/teilnehmer/{p}", veranstaltungId, personId)
        ).andExpect(status().isConflict());
    }
}