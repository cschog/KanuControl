package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeilnehmerBeitragService {

    private final BeitragsregelService beitragsregelService;
    private final AltersService altersService;

    /**
     * Liefert den fachlich gültigen Beitrag
     * eines Teilnehmers für eine Veranstaltung.
     *
     * Regeln:
     *
     * 1. Individuelle Gebühren aktiv:
     *    -> individuellerBeitrag verwenden
     *    -> falls null => 0 €
     *
     * 2. Individuelle Gebühren deaktiviert:
     *    -> Standardgebühr der Veranstaltung
     *    -> falls null => 0 €
     */
    public BigDecimal getEffektiverBeitrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        if (veranstaltung == null || teilnehmer == null) {
            return BigDecimal.ZERO;
        }

    /* =========================================
       INDIVIDUELLE GEBÜHREN
       ========================================= */

        if (veranstaltung.isIndividuelleGebuehren()) {

            // 1️⃣ Manuell überschrieben
            if (teilnehmer.getIndividuellerBeitrag() != null) {
                return teilnehmer.getIndividuellerBeitrag();
            }

            // 2️⃣ Struktur
            Beitragsstruktur struktur = veranstaltung.getBeitragsstruktur();

            if (struktur != null) {

                Integer alter =
                        altersService.berechneAlterBeiBeginn(
                                teilnehmer.getPerson().getGeburtsdatum(),
                                veranstaltung.getBeginnDatum()
                        );

                if (alter != null) {

                    return beitragsregelService
                            .findPassendeRegel(
                                    struktur,
                                    alter,
                                    teilnehmer.getRolle()
                            )
                            .map(r -> {
                                log.debug(
                                        "Teilnehmer {} Alter {} → Regel {}",
                                        teilnehmer.getId(),
                                        alter,
                                        r.getBeitrag()
                                );
                                return r.getBeitrag();
                            })
                            .orElse(BigDecimal.ZERO);
                }
            }

            return BigDecimal.ZERO;
        }

    /* =========================================
       STANDARD
       ========================================= */

        return veranstaltung.getStandardGebuehr() != null
                ? veranstaltung.getStandardGebuehr()
                : BigDecimal.ZERO;
    }

    public BigDecimal berechneBeitrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        // =========================================
        // Individuell überschrieben
        // =========================================

        if (teilnehmer.getIndividuellerBeitrag() != null) {
            return teilnehmer.getIndividuellerBeitrag();
        }

        // =========================================
        // Normale Standardgebühr
        // =========================================

        if (!Boolean.TRUE.equals(
                veranstaltung.isIndividuelleGebuehren()
        )) {

            return veranstaltung.getStandardGebuehr();
        }

        // =========================================
        // Keine Struktur vorhanden
        // =========================================

        Beitragsstruktur struktur =
                veranstaltung.getBeitragsstruktur();

        if (struktur == null) {
            return BigDecimal.ZERO;
        }

        // =========================================
        // Alter berechnen
        // =========================================

        Integer alter = null;

        if (teilnehmer.getPerson()
                .getGeburtsdatum() != null) {

            alter = Period.between(
                    teilnehmer.getPerson().getGeburtsdatum(),
                    LocalDate.now()
            ).getYears();
        }

        TeilnehmerRolle rolle = teilnehmer.getRolle();

        // =========================================
        // Passende Regel suchen
        // =========================================

        for (Beitragsregel regel : struktur.getRegeln()) {

            // Rolle prüfen
            if (regel.getRolle() != null &&
                    regel.getRolle() != rolle) {

                continue;
            }

            // Alter von
            if (regel.getAlterVon() != null &&
                    alter != null &&
                    alter < regel.getAlterVon()) {

                continue;
            }

            // Alter bis
            if (regel.getAlterBis() != null &&
                    alter != null &&
                    alter > regel.getAlterBis()) {

                continue;
            }

            return regel.getBeitrag();
        }

        // =========================================
        // Fallback
        // =========================================

        return BigDecimal.ZERO;
    }

    public boolean isBezahlt(Teilnehmer teilnehmer) {

        if (teilnehmer == null) {
            return false;
        }

        return Boolean.TRUE.equals(teilnehmer.getBezahlt());
    }

    public BigDecimal getOffenerBetrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        if (isBezahlt(teilnehmer)) {
            return BigDecimal.ZERO;
        }

        return getEffektiverBeitrag(
                veranstaltung,
                teilnehmer
        );
    }
}