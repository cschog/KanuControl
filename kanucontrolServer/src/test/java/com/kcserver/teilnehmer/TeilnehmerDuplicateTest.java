package com.kcserver.teilnehmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.support.data.PersonTestFactory;
import com.kcserver.support.data.VeranstaltungTestFactory;
import com.kcserver.support.data.VereinTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeilnehmerDuplicateTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired TeilnehmerRepository teilnehmerRepository;

    Long veranstaltungId;
    Long leiterId;
    Long personId;

    @BeforeEach
    void setup() throws Exception {

        VereinTestFactory vereine = new VereinTestFactory(mockMvc, objectMapper, tenantAuth());
        PersonTestFactory personen = new PersonTestFactory(mockMvc, objectMapper, tenantAuth());
        VeranstaltungTestFactory veranstaltungen = new VeranstaltungTestFactory(mockMvc, objectMapper, tenantAuth());

        Long vereinId = vereine.createIfNotExists("EKC", "Eschweiler Kanu Club");

        leiterId = personen.createOrReuse("Max","Mustermann", LocalDate.of(1990,1,1), null);
        personId = personen.createOrReuse("Erika","Musterfrau", LocalDate.of(2000,1,1), null);

        veranstaltungId = veranstaltungen.create(vereinId, leiterId, "Test");

        ensureTenantSchema();
    }

    @Test
    void shouldPreventDuplicateTeilnehmer() throws Exception {

        // 1. add ok
        mockMvc.perform(
                tenantRequest(post("/api/veranstaltungen/{v}/teilnehmer/{p}", veranstaltungId, personId))
        ).andExpect(status().isCreated());

        // 2. add again → CONFLICT
        mockMvc.perform(
                tenantRequest(post("/api/veranstaltungen/{v}/teilnehmer/{p}", veranstaltungId, personId))
        ).andExpect(status().isConflict());
    }
}