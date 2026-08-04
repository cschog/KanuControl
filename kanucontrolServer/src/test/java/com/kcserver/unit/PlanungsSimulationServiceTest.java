package com.kcserver.unit;

import com.kcserver.entity.Planung;
import com.kcserver.entity.PlanungPosition;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.service.planung.PlanungBerechnungService;
import com.kcserver.service.planung.PlanungAutomatikService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanungsSimulationServiceTest {

    @Mock
    private PlanungBerechnungService planungBerechnungService;

    @InjectMocks
    private PlanungAutomatikService service;

    @Test
    void aktualisiereAutomatischePositionen_legtAllePlanungspositionenAn(){

        Planung planung = new Planung();
        planung.setVeranstaltung(new Veranstaltung());

        when(planungBerechnungService.berechneUnterkunft(planung))
                .thenReturn(BigDecimal.valueOf(1200));

        when(planungBerechnungService.berechneVerpflegung(planung))
                .thenReturn(BigDecimal.valueOf(900));

        when(planungBerechnungService.berechneTeilnehmerbeitraege(planung))
                .thenReturn(BigDecimal.valueOf(2500));

        when(planungBerechnungService.berechneKjfpZuschuss(planung))
                .thenReturn(BigDecimal.valueOf(1800));

        service.aktualisiereAutomatischePositionen(planung);

        assertThat(planung.getPositionen())
                .extracting(PlanungPosition::getKategorie)
                .containsExactlyInAnyOrder(
                        FinanzKategorie.UNTERKUNFT,
                        FinanzKategorie.VERPFLEGUNG,
                        FinanzKategorie.HONORARE,
                        FinanzKategorie.FAHRTKOSTEN,
                        FinanzKategorie.VERBRAUCHSMATERIAL,
                        FinanzKategorie.KULTUR,
                        FinanzKategorie.MIETE,
                        FinanzKategorie.SONSTIGE_KOSTEN,
                        FinanzKategorie.TEILNEHMERBEITRAG,
                        FinanzKategorie.KJFP_ZUSCHUSS
                );

        assertPosition(planung,
                FinanzKategorie.UNTERKUNFT,
                "1200.00");

        assertPosition(planung,
                FinanzKategorie.VERPFLEGUNG,
                "900.00");

        assertPosition(planung,
                FinanzKategorie.TEILNEHMERBEITRAG,
                "2500.00");

        assertPosition(planung,
                FinanzKategorie.KJFP_ZUSCHUSS,
                "1800.00");

        assertPosition(planung,
                FinanzKategorie.HONORARE,
                "0.00");

        assertPosition(planung,
                FinanzKategorie.FAHRTKOSTEN,
                "0.00");

        assertPosition(planung,
                FinanzKategorie.VERBRAUCHSMATERIAL,
                "0.00");

        assertPosition(planung,
                FinanzKategorie.KULTUR,
                "0.00");

        assertPosition(planung,
                FinanzKategorie.MIETE,
                "0.00");

        assertPosition(planung,
                FinanzKategorie.SONSTIGE_KOSTEN,
                "0.00");
    }

    private void assertPosition(
            Planung planung,
            FinanzKategorie kategorie,
            String betrag) {

        PlanungPosition position =
                planung.getPositionen()
                        .stream()
                        .filter(p -> p.getKategorie() == kategorie)
                        .findFirst()
                        .orElseThrow();

        assertThat(position.getBetrag())
                .isEqualByComparingTo(betrag);

        assertThat(position.isAutomatischBerechnet())
                .isTrue();

        assertThat(position.isEditierbar())
                .isFalse();
    }
}
