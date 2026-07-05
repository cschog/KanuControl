package com.kcserver.service.simulation;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.dto.simulation.SimulationErgebnis;
import com.kcserver.dto.simulation.SimulationPosition;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.service.PlanungBerechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationEngine {

    private final PlanungBerechnungService berechnung;


    /* =========================================================
   SIMULATION
   ========================================================= */
    public SimulationErgebnis simuliere(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return SimulationErgebnis.builder()
                    .positionen(List.of())
                    .kosten(BigDecimal.ZERO)
                    .einnahmen(BigDecimal.ZERO)
                    .saldo(BigDecimal.ZERO)
                    .build();
        }

        List<SimulationPosition> positionen =
                berechnePositionen(simulation);

        BigDecimal kosten = BigDecimal.ZERO;
        BigDecimal einnahmen = BigDecimal.ZERO;

        for (SimulationPosition position : positionen) {

            if (istKosten(position.getKategorie())) {
                kosten = kosten.add(position.getBetrag());
            } else {
                einnahmen = einnahmen.add(position.getBetrag());
            }
        }

        return SimulationErgebnis.builder()
                .positionen(positionen)
                .kosten(kosten)
                .einnahmen(einnahmen)
                .saldo(einnahmen.subtract(kosten))
                .build();
    }

    /* =========================================================
   POSITIONEN
   ========================================================= */
    private List<SimulationPosition> berechnePositionen(
            PlanungsSimulation simulation
    ) {

        List<SimulationPosition> positionen = new ArrayList<>();

        positionen.add(berechneUnterkunft(simulation));
        positionen.add(berechneVerpflegung(simulation));
        positionen.add(berechneTeilnehmerbeitraege(simulation));
        positionen.add(berechneKjfp(simulation));

        return positionen;
    }

    /* =========================================================
   EINZELPOSITIONEN
   ========================================================= */
    private SimulationPosition berechneUnterkunft(
            PlanungsSimulation simulation
    ) {

        return position(
                FinanzKategorie.UNTERKUNFT,
                berechnung.berechneUnterkunft(simulation)
        );
    }

    private SimulationPosition berechneVerpflegung(
            PlanungsSimulation simulation
    ) {

        return position(
                FinanzKategorie.VERPFLEGUNG,
                berechnung.berechneVerpflegung(simulation)
        );
    }

    private SimulationPosition berechneTeilnehmerbeitraege(
            PlanungsSimulation simulation
    ) {

        return position(
                FinanzKategorie.TEILNEHMERBEITRAG,
                berechnung.berechneTeilnehmerbeitraege(simulation)
        );
    }

    private SimulationPosition berechneKjfp(
            PlanungsSimulation simulation
    ) {

        return position(
                FinanzKategorie.KJFP_ZUSCHUSS,
                berechnung.berechneKjfpZuschuss(simulation)
        );
    }

    /* =========================================================
   HILFSMETHODEN
   ========================================================= */
    private SimulationPosition position(
            FinanzKategorie kategorie,
            BigDecimal betrag
    ) {

        return SimulationPosition.builder()
                .kategorie(kategorie)
                .betrag(
                        betrag == null
                                ? BigDecimal.ZERO
                                : betrag
                )
                .automatisch(true)
                .build();
    }
    private boolean istKosten(
            FinanzKategorie kategorie
    ) {

        return switch (kategorie) {

            case UNTERKUNFT,
                 VERPFLEGUNG,
                 HONORARE,
                 FAHRTKOSTEN,
                 VERBRAUCHSMATERIAL,
                 KULTUR,
                 MIETE,
                 SONSTIGE_KOSTEN -> true;

            default -> false;
        };
    }
}


