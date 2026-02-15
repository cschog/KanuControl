package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.testdata.PersonTestFactory;
import com.kcserver.testdata.VeranstaltungTestFactory;
import com.kcserver.testdata.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

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

        VereinTestFactory vereine = new VereinTestFactory(mockMvc, objectMapper, tenantAuth());
        PersonTestFactory personen = new PersonTestFactory(mockMvc, objectMapper, tenantAuth());
        VeranstaltungTestFactory veranstaltungen = new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        Long vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiter1 = personen.createOrReuse("Max","Altleiter", LocalDate.of(1990,1,1), null);
        leiter2 = personen.createOrReuse("Peter","Neuleiter", LocalDate.of(1990,1,1), null);

        veranstaltungId = veranstaltungen.create(vereinId, leiter1, "Test");
    }

    @Test
    void shouldChangeLeiter() throws Exception {

        mockMvc.perform(
                tenantRequest(
                        put("/api/veranstaltung/{vId}/teilnehmer/{personId}/leiter",
                                veranstaltungId, leiter2)
                )
        ).andExpect(status().isOk());

        ensureTenantSchema();
        long countLeiter =
                teilnehmerRepository.countByVeranstaltungIdAndRolle(
                        veranstaltungId,
                        com.kcserver.enumtype.TeilnehmerRolle.LEITER);

        assertEquals(1, countLeiter);
    }
}