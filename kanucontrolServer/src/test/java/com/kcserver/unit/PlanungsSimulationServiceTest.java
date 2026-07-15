package com.kcserver.unit;

import com.kcserver.entity.Planung;
import com.kcserver.entity.PlanungPosition;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.service.PlanungBerechnungService;
import com.kcserver.service.PlanungAutomatikService;
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
    void aktualisiereAutomatischePositionen_legtAutomatischePositionenAn() {

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
                .hasSize(4);

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
