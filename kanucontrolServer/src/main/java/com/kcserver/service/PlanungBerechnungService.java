package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.beitrag.BeitragsregelService;
import com.kcserver.service.simulation.PlanungsSimulationFactory;
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
    private static final int FOERDER_HOECHSTALTER = 20;

    private final BeitragsregelService beitragsregelService;
    private final FoerderService foerderService;

    private final PlanungsSimulationFactory simulationFactory;

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

        if (simulation.getBeitragsstruktur() == null) {

            BigDecimal gebuehr = simulation.getStandardGebuehr();

            if (gebuehr == null) {
                return BigDecimal.ZERO;
            }

            return gebuehr.multiply(
                    BigDecimal.valueOf(
                            simulation.getTeilnehmer()
                                    + simulation.getMitarbeiter()
                    )
            );
        }

        BigDecimal teilnehmerBeitrag =
                beitragsregelService
                        .findPlanungsRegelTeilnehmer(
                                simulation.getBeitragsstruktur(),
                                FOERDER_HOECHSTALTER
                        )
                        .map(Beitragsregel::getBeitrag)
                        .orElse(BigDecimal.ZERO);

        BigDecimal mitarbeiterBeitrag =
                beitragsregelService
                        .findPlanungsRegelMitarbeiter(
                                simulation.getBeitragsstruktur()
                        )
                        .map(Beitragsregel::getBeitrag)
                        .orElse(BigDecimal.ZERO);

        return teilnehmerBeitrag.multiply(
                        BigDecimal.valueOf(simulation.getTeilnehmer()))
                .add(
                        mitarbeiterBeitrag.multiply(
                                BigDecimal.valueOf(simulation.getMitarbeiter())
                        )
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

}