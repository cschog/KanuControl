package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Veranstaltung;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                getGeplanteTeilnehmer(veranstaltung)
                        + getGeplanteMitarbeiter(veranstaltung);

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
                        BigDecimal.valueOf(getGeplanteTeilnehmer(veranstaltung)))
                .add(
                        mitarbeiterBeitrag.multiply(
                                BigDecimal.valueOf(getGeplanteMitarbeiter(veranstaltung))
                        )
                );
    }

    /* =========================================================
       HILFSMETHODEN
       ========================================================= */

    private int getGeplanteTeilnehmer(
            Veranstaltung veranstaltung
    ) {

        return n(veranstaltung.getGeplanteTeilnehmerMaennlich())
                + n(veranstaltung.getGeplanteTeilnehmerWeiblich())
                + n(veranstaltung.getGeplanteTeilnehmerDivers());
    }

    private int getGeplanteMitarbeiter(
            Veranstaltung veranstaltung
    ) {

        return n(veranstaltung.getGeplanteMitarbeiterMaennlich())
                + n(veranstaltung.getGeplanteMitarbeiterWeiblich())
                + n(veranstaltung.getGeplanteMitarbeiterDivers());
    }

    private int n(Integer value) {
        return value == null ? 0 : value;
    }

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