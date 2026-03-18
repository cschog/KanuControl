package com.kcserver.unit;

import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.PlanungPositionService;
import com.kcserver.finanz.PlanungService;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.support.data.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlanungServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    PlanungService service;
    @Autowired
    PlanungPositionService positionService;
    @Autowired TestDataFactory factory;

    Long veranstaltungId;

    @BeforeEach
    void setup() {
        veranstaltungId = factory.createTestVeranstaltung();
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