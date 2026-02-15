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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeilnehmerAssignedTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired TeilnehmerRepository teilnehmerRepository;

    Long veranstaltungId;
    Long leiterId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine = new VereinTestFactory(mockMvc, objectMapper, tenantAuth());
        PersonTestFactory personen = new PersonTestFactory(mockMvc, objectMapper, tenantAuth());
        VeranstaltungTestFactory veranstaltungen = new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        Long vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.createOrReuse("Max","Mustermann", LocalDate.of(1990,1,1), null);

        veranstaltungId = veranstaltungen.create(vereinId, leiterId, "Test");
    }

    @Test
    void shouldReturnAssignedPersons() throws Exception {

        mockMvc.perform(
                tenantRequest(
                        get("/api/veranstaltung/{id}/teilnehmer/assigned", veranstaltungId)
                )
        ).andExpect(status().isOk());
    }
}