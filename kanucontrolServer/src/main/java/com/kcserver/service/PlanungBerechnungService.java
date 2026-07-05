package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.simulation.PlanungsSimulationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.kcserver.simulation.PlanungsSimulation;

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

    private final VeranstaltungBerechnungsService veranstaltungBerechnungsService;

    private final PlanungsSimulationFactory simulationFactory;

    public BigDecimal berechneTeilnehmerbeitraege(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        if (!veranstaltung.isIndividuelleGebuehren()) {
            return berechneStandardGebuehr(veranstaltung);
        }

        return berechneBeitragsstruktur(veranstaltung);
    }

    /* =========================================================
       STANDARDGEBÜHR
       ========================================================= */

    private BigDecimal berechneStandardGebuehr(
            Veranstaltung veranstaltung
    ) {

        BigDecimal gebuehr = veranstaltung.getStandardGebuehr();

        if (gebuehr == null) {
            return BigDecimal.ZERO;
        }

        int personen =
                veranstaltungBerechnungsService
                        .ermittleGeplanteGesamtPersonen(veranstaltung);

        return gebuehr.multiply(BigDecimal.valueOf(personen));
    }

    /* =========================================================
       BEITRAGSSTRUKTUR
       ========================================================= */

    private BigDecimal berechneBeitragsstruktur(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung.getBeitragsstruktur() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal teilnehmerBeitrag =
                beitragsregelService
                        .findPlanungsRegelTeilnehmer(
                                veranstaltung.getBeitragsstruktur(),
                                FOERDER_HOECHSTALTER
                        )
                        .map(Beitragsregel::getBeitrag)
                        .orElse(BigDecimal.ZERO);

        BigDecimal mitarbeiterBeitrag =
                beitragsregelService
                        .findPlanungsRegelMitarbeiter(
                                veranstaltung.getBeitragsstruktur()
                        )
                        .map(Beitragsregel::getBeitrag)
                        .orElse(BigDecimal.ZERO);

        return teilnehmerBeitrag.multiply(
                        BigDecimal.valueOf(veranstaltungBerechnungsService
                                .ermittleGeplanteTeilnehmer(veranstaltung)))
                .add(
                        mitarbeiterBeitrag.multiply(
                                BigDecimal.valueOf(veranstaltungBerechnungsService
                                        .ermittleGeplanteMitarbeiter(veranstaltung))
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

        return foerderService.berechneGeplanteFoerderung(
                veranstaltung
        );
    }




}