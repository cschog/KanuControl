package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.enumtype.FinanzKategorie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;



@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UltimateFinanzFlowTest extends AbstractFinanzIntegrationTest {

    Long veranstaltungId;

    @BeforeEach
    void setup() {

        veranstaltungId = createTestVeranstaltung();

        // garantiert offene Abrechnung
        createOpenAbrechnung(veranstaltungId);
    }

    @Test
    void shouldRunCompleteFinancialFlowSuccessfully() {

        /* =========================================================
           PLANUNG
           ========================================================= */

        PlanungPositionCreateDTO kosten = new PlanungPositionCreateDTO();
        kosten.setKategorie(FinanzKategorie.UNTERKUNFT);
        kosten.setBetrag(new BigDecimal("200"));

        PlanungPositionCreateDTO zuschuss = new PlanungPositionCreateDTO();
        zuschuss.setKategorie(FinanzKategorie.KJFP_ZUSCHUSS);
        zuschuss.setBetrag(new BigDecimal("200"));

        planungPositionService.addPosition(veranstaltungId, kosten);
        planungPositionService.addPosition(veranstaltungId, zuschuss);

        planungService.einreichen(veranstaltungId);

        /* =========================================================
           ABRECHNUNG
           ========================================================= */

        buchungService.addBuchung(
                veranstaltungId,
                createBuchung(FinanzKategorie.UNTERKUNFT, "200")
        );

        buchungService.addBuchung(
                veranstaltungId,
                createBuchung(FinanzKategorie.KJFP_ZUSCHUSS, "200")
        );

        abrechnungService.abschliessen(veranstaltungId);

        assertThat(
                abrechnungService.getOrCreate(veranstaltungId)
                        .getStatus()
                        .name()
        ).isEqualTo("ABGESCHLOSSEN");
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private AbrechnungBuchungCreateDTO createBuchung(
            FinanzKategorie kat,
            String betrag
    ) {

        AbrechnungBuchungCreateDTO dto = new AbrechnungBuchungCreateDTO();

        dto.setKategorie(kat);
        dto.setBetrag(new BigDecimal(betrag));
        dto.setDatum(LocalDate.now());

        return dto;
    }
}