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

        // Kosten
        positionen.add(position(
                FinanzKategorie.UNTERKUNFT,
                berechnung.berechneUnterkunft(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.VERPFLEGUNG,
                berechnung.berechneVerpflegung(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.HONORARE,
                berechnung.berechneHonorare(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.FAHRTKOSTEN,
                berechnung.berechneFahrtkosten(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.VERBRAUCHSMATERIAL,
                berechnung.berechneVerbrauchsmaterial(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.KULTUR,
                berechnung.berechneKultur(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.MIETE,
                berechnung.berechneMiete(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.SONSTIGE_KOSTEN,
                berechnung.berechneSonstigeKosten(simulation)
        ));

        // Einnahmen
        positionen.add(position(
                FinanzKategorie.TEILNEHMERBEITRAG,
                berechnung.berechneTeilnehmerbeitraege(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.PFAND,
                berechnung.berechnePfand(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.SONSTIGE_EINNAHMEN,
                berechnung.berechneSonstigeEinnahmen(simulation)
        ));

        positionen.add(position(
                FinanzKategorie.KJFP_ZUSCHUSS,
                berechnung.berechneKjfpZuschuss(simulation)
        ));

        return positionen;
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


