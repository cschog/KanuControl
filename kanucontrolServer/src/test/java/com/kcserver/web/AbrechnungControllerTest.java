package com.kcserver.web;

import com.kcserver.finanz.AbrechnungService;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.data.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AbrechnungControllerTest extends AbstractTenantIntegrationTest {

    @Autowired TestDataFactory factory;
    @Autowired
    AbrechnungService abrechnungService;

    Long veranstaltungId;

    @BeforeEach
    void setup() {

        veranstaltungId = factory.createTestVeranstaltung();

        // erzeugt automatisch Abrechnung mit Status OFFEN
        abrechnungService.getOrCreate(veranstaltungId);
    }

    @Test
    void shouldGetAbrechnung() throws Exception {

        mockMvc.perform(
                tenantRequest(
                        get("/api/veranstaltungen/{id}/abrechnung", veranstaltungId)
                )
        ).andExpect(status().isOk());
    }
}