package com.kcserver.service;

import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TeilnehmerBeitragService {

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
           Individuelle Gebühren
           ========================================= */

        if (Boolean.TRUE.equals(
                veranstaltung.isIndividuelleGebuehren()
        )) {

            return teilnehmer.getIndividuellerBeitrag() != null
                    ? teilnehmer.getIndividuellerBeitrag()
                    : BigDecimal.ZERO;
        }

        /* =========================================
           Standardgebühr
           ========================================= */

        return veranstaltung.getStandardGebuehr() != null
                ? veranstaltung.getStandardGebuehr()
                : BigDecimal.ZERO;
    }

    /**
     * Prüft, ob ein Teilnehmer bezahlt hat.
     */
    public boolean isBezahlt(Teilnehmer teilnehmer) {

        if (teilnehmer == null) {
            return false;
        }

        return Boolean.TRUE.equals(
                teilnehmer.isBezahlt()
        );
    }

    /**
     * Liefert offenen Betrag.
     */
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