package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeilnehmerLeiterChangeTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired TeilnehmerRepository teilnehmerRepository;

    Long veranstaltungId;
    Long leiter1;
    Long leiter2;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine = new VereinTestFactory(mockMvc, objectMapper);
        PersonTestFactory personen = new PersonTestFactory(mockMvc, objectMapper);
        VeranstaltungTestFactory veranstaltungen = new VeranstaltungTestFactory(mockMvc, objectMapper);

        Long vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiter1 = personen.create(b ->
                b.withVorname("Max")
                        .withName("Altleiter")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
        );

        leiter2 = personen.create(b ->
                b.withVorname("Peter")
                        .withName("Neuleiter")
                        .withGeburtsdatum(java.time.LocalDate.of(2000, 1, 1))
        );

        veranstaltungId = veranstaltungen.create(vereinId, leiter1, "Test");
    }

    @Test
    void shouldChangeLeiter() throws Exception {

        mockMvc.perform(

                        put("/api/veranstaltungen/{vId}/teilnehmer/{personId}/leiter",
                                veranstaltungId, leiter2)

        ).andExpect(status().isOk());

        ensureTenantSchema();
        long countLeiter =
                teilnehmerRepository.countByVeranstaltungIdAndRolle(
                        veranstaltungId,
                        com.kcserver.enumtype.TeilnehmerRolle.LEITER);

        assertEquals(1, countLeiter);
    }
}