package com.kcserver.unit;

import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.PlanungPositionService;
import com.kcserver.finanz.PlanungService;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class PlanungServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    PlanungService service;

    @Autowired
    PlanungPositionService positionService;

    @Autowired
    com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    Long veranstaltungId;

    // ✅ Factories
    VereinTestFactory vereinFactory;
    PersonTestFactory personFactory;
    VeranstaltungTestFactory veranstaltungFactory;

    @BeforeEach
    void setup() throws Exception {

        // Factories initialisieren
        vereinFactory = new VereinTestFactory(mockMvc, objectMapper);
        personFactory = new PersonTestFactory(mockMvc, objectMapper);
        veranstaltungFactory =
                new VeranstaltungTestFactory(
                        mockMvc,
                        objectMapper
                );

        // 1️⃣ Verein
        Long vereinId = vereinFactory.create("TV", "Testverein");

        // 2️⃣ Leiter (>=18 wichtig!)
        Long leiterId = personFactory.createWithVerein(vereinId, b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
        );

        // 3️⃣ Veranstaltung
        veranstaltungId = veranstaltungFactory.create(
                vereinId,
                leiterId,
                "Test Planung"
        );
    }

    @Test
    void shouldSubmitBalancedPlanung() {

        PlanungPositionCreateDTO kosten = new PlanungPositionCreateDTO();
        kosten.setKategorie(FinanzKategorie.UNTERKUNFT);
        kosten.setBetrag(new BigDecimal("100"));

        PlanungPositionCreateDTO einnahme = new PlanungPositionCreateDTO();
        einnahme.setKategorie(FinanzKategorie.KJFP_ZUSCHUSS);
        einnahme.setBetrag(new BigDecimal("100"));

        positionService.addPosition(veranstaltungId, kosten);
        positionService.addPosition(veranstaltungId, einnahme);

        service.einreichen(veranstaltungId);

        assertThat(service.getOrCreate(veranstaltungId).isEingereicht()).isTrue();
    }
}