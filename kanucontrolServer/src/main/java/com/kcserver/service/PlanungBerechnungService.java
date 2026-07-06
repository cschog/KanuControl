package com.kcserver.service;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.simulation.SimulationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.kcserver.dto.simulation.PlanungsSimulation;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PlanungBerechnungService {

    /**
     * Aktuell fest im Code.
     * Später aus den Fördersätzen bzw. der Konfiguration ermitteln.
     */

    private final FoerderService foerderService;

    private final SimulationFactory simulationFactory;

    public BigDecimal berechneTeilnehmerbeitraege(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        return berechneTeilnehmerbeitraege(
                simulationFactory.fromVeranstaltung(veranstaltung)
        );
    }

    public BigDecimal berechneTeilnehmerbeitraege(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getTeilnehmerBeitragUnter21Jahre()
                .multiply(BigDecimal.valueOf(simulation.getTeilnehmer()))
                .add(
                        simulation.getMitarbeiterBeitrag()
                                .multiply(BigDecimal.valueOf(simulation.getMitarbeiter()))
                );
    }


    public BigDecimal berechneUnterkunft(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        return berechneUnterkunft(
                simulationFactory.fromVeranstaltung(
                        veranstaltung
                )
        );
    }

    public BigDecimal berechneUnterkunft(
            PlanungsSimulation simulation
    ) {

        if (simulation.getUnterkunftPreisProPersonUndNacht() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getUnterkunftPreisProPersonUndNacht()
                .multiply(
                        BigDecimal.valueOf(
                                simulation.getTeilnehmer()
                                        + simulation.getMitarbeiter()
                        )
                )

                .multiply(
                        BigDecimal.valueOf(
                                simulation.getNaechte()
                        )
                );

    }

    public BigDecimal berechneVerpflegung(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        return berechneVerpflegung(
                simulationFactory.fromVeranstaltung(
                        veranstaltung
                )
        );
    }

    public BigDecimal berechneVerpflegung(
            PlanungsSimulation simulation
    ) {
        if (simulation.getVerpflegungPreisProPersonUndTag() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getVerpflegungPreisProPersonUndTag()
                .multiply(
                        BigDecimal.valueOf(
                                simulation.getTeilnehmer()
                                        + simulation.getMitarbeiter()
                        )
                )
                .multiply(
                        BigDecimal.valueOf(
                                simulation.getTage()
                        )
                );
    }

    /* =========================================================
   HILFSMETHODEN
   ========================================================= */

    public BigDecimal berechneKjfpZuschuss(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        return berechneKjfpZuschuss(
                simulationFactory.fromVeranstaltung(veranstaltung)
        );
    }

    public BigDecimal berechneKjfpZuschuss(
            PlanungsSimulation simulation
    ) {

        if (simulation == null) {
            return BigDecimal.ZERO;
        }

        return foerderService.berechneGeplanteFoerderung(
                simulation
        );
    }

    public BigDecimal berechneHonorare(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getHonorare() == null
                ? BigDecimal.ZERO
                : simulation.getHonorare();
    }

    public BigDecimal berechneFahrtkosten(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getFahrtkosten() == null
                ? BigDecimal.ZERO
                : simulation.getFahrtkosten();
    }

    public BigDecimal berechneVerbrauchsmaterial(
            PlanungsSimulation simulation
    ) {
        if (simulation == null
                || simulation.getVerbrauchsmaterialProTag() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getVerbrauchsmaterialProTag()
                .multiply(BigDecimal.valueOf(simulation.getTage()));
    }

    public BigDecimal berechneKultur(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getKultur() == null
                ? BigDecimal.ZERO
                : simulation.getKultur();
    }

    public BigDecimal berechneMiete(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getMiete() == null
                ? BigDecimal.ZERO
                : simulation.getMiete();
    }

    public BigDecimal berechneSonstigeKosten(
            PlanungsSimulation simulation
    ) {
        if (simulation == null
                || simulation.getSonstigeKostenProTag() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getSonstigeKostenProTag()
                .multiply(BigDecimal.valueOf(simulation.getTage()));
    }

    public BigDecimal berechnePfand(
            PlanungsSimulation simulation
    ) {
        return simulation == null || simulation.getPfand() == null
                ? BigDecimal.ZERO
                : simulation.getPfand();
    }

    public BigDecimal berechneSonstigeEinnahmen(
            PlanungsSimulation simulation
    ) {
        if (simulation == null
                || simulation.getSonstigeEinnahmenProTag() == null) {
            return BigDecimal.ZERO;
        }

        return simulation.getSonstigeEinnahmenProTag()
                .multiply(BigDecimal.valueOf(simulation.getTage()));
    }

}