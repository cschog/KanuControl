package com.kcserver.web;

import com.kcserver.finanz.AbrechnungService;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AbrechnungControllerTest extends AbstractTenantIntegrationTest {

    @Autowired
    AbrechnungService abrechnungService;

    @Autowired
    com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    Long veranstaltungId;

    // ✅ Factories
    VereinTestFactory vereinFactory;
    PersonTestFactory personFactory;
    VeranstaltungTestFactory veranstaltungFactory;

    @BeforeEach
    void setup() throws Exception {

        vereinFactory = new VereinTestFactory(mockMvc, objectMapper);
        personFactory = new PersonTestFactory(mockMvc, objectMapper);
        veranstaltungFactory = new VeranstaltungTestFactory(mockMvc, objectMapper);

        // 1️⃣ Verein
        Long vereinId = vereinFactory.create("TV", "Testverein");

        // 2️⃣ Leiter

        Long leiterId = personFactory.createWithVerein(vereinId, b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
        );

        // 3️⃣ Veranstaltung
        veranstaltungId = veranstaltungFactory.create(
                vereinId,
                leiterId,
                "Test Abrechnung"
        );

        // 4️⃣ Abrechnung erzeugen
        abrechnungService.getOrCreate(veranstaltungId);
    }

    @Test
    void shouldGetAbrechnung() throws Exception {

        mockMvc.perform(

                        get("/api/veranstaltungen/{id}/abrechnung", veranstaltungId)

        ).andExpect(status().isOk());
    }
}