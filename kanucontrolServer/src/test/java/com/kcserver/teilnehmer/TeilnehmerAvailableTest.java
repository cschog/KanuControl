package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeilnehmerAvailableTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;

    Long veranstaltungId;
    Long leiterId;
    Long freiePerson;

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

        freiePerson = personen.create(b ->
                b.withVorname("Peter")
                        .withName("Frei")
                        .withGeburtsdatum(java.time.LocalDate.of(2001, 1, 1))
        );


        veranstaltungId = veranstaltungen.create(vereinId, leiterId, "Test");
    }

    @Test
    void shouldReturnAvailablePersons() throws Exception {

        mockMvc.perform(

                        get("/api/veranstaltungen/{id}/teilnehmer/available", veranstaltungId)

        ).andExpect(status().isOk());
    }
}