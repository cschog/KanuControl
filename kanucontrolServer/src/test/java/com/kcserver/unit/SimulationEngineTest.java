package com.kcserver.unit;

import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.service.PlanungBerechnungService;
import com.kcserver.simulation.PlanungsSimulation;
import com.kcserver.simulation.SimulationEngine;
import com.kcserver.simulation.SimulationErgebnis;
import com.kcserver.simulation.SimulationPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationEngineTest {

    @Mock
    private PlanungBerechnungService berechnung;

    @InjectMocks
    private SimulationEngine engine;

    @Test
    void simuliere() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder().build();

        when(berechnung.berechneUnterkunft(simulation))
                .thenReturn(BigDecimal.valueOf(4200));

        when(berechnung.berechneVerpflegung(simulation))
                .thenReturn(BigDecimal.valueOf(2592));

        when(berechnung.berechneTeilnehmerbeitraege(simulation))
                .thenReturn(BigDecimal.valueOf(2200));

        when(berechnung.berechneKjfpZuschuss(simulation))
                .thenReturn(BigDecimal.valueOf(1800));

        SimulationErgebnis ergebnis =
                engine.simuliere(simulation);

        assertThat(ergebnis.getPositionen())
                .hasSize(4);

        assertThat(ergebnis.getKosten())
                .isEqualByComparingTo("6792");

        assertThat(ergebnis.getEinnahmen())
                .isEqualByComparingTo("4000");

        assertThat(ergebnis.getSaldo())
                .isEqualByComparingTo("-2792");
    }

    @Test
    void simuliereNull() {

        SimulationErgebnis ergebnis =
                engine.simuliere(null);

        assertThat(ergebnis.getKosten())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(ergebnis.getEinnahmen())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(ergebnis.getSaldo())
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(ergebnis.getPositionen())
                .isEmpty();
    }

    @Test
    void kategorienSindKorrekt() {

        PlanungsSimulation simulation =
                PlanungsSimulation.builder().build();

        when(berechnung.berechneUnterkunft(simulation))
                .thenReturn(BigDecimal.ONE);

        when(berechnung.berechneVerpflegung(simulation))
                .thenReturn(BigDecimal.ONE);

        when(berechnung.berechneTeilnehmerbeitraege(simulation))
                .thenReturn(BigDecimal.ONE);

        when(berechnung.berechneKjfpZuschuss(simulation))
                .thenReturn(BigDecimal.ONE);

        SimulationErgebnis ergebnis =
                engine.simuliere(simulation);

        assertThat(ergebnis.getPositionen())
                .extracting(SimulationPosition::getKategorie)
                .containsExactly(
                        FinanzKategorie.UNTERKUNFT,
                        FinanzKategorie.VERPFLEGUNG,
                        FinanzKategorie.TEILNEHMERBEITRAG,
                        FinanzKategorie.KJFP_ZUSCHUSS
                );
    }
}