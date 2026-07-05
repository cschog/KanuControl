package com.kcserver.service.veranstaltung;

import com.kcserver.entity.Veranstaltung;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class VeranstaltungBerechnungsService {

    /* =========================================================
       TEILNEHMER
       ========================================================= */

    public int ermittleGeplanteTeilnehmer(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return 0;
        }

        return n(veranstaltung.getGeplanteTeilnehmerMaennlich())
                + n(veranstaltung.getGeplanteTeilnehmerWeiblich())
                + n(veranstaltung.getGeplanteTeilnehmerDivers());
    }

    /* =========================================================
       MITARBEITER
       ========================================================= */

    public int ermittleGeplanteMitarbeiter(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return 0;
        }

        return n(veranstaltung.getGeplanteMitarbeiterMaennlich())
                + n(veranstaltung.getGeplanteMitarbeiterWeiblich())
                + n(veranstaltung.getGeplanteMitarbeiterDivers());
    }

    /* =========================================================
       GESAMTPERSONEN
       ========================================================= */

    public int ermittleGeplanteGesamtPersonen(
            Veranstaltung veranstaltung
    ) {

        return ermittleGeplanteTeilnehmer(veranstaltung)
                + ermittleGeplanteMitarbeiter(veranstaltung);
    }

    /* =========================================================
       DAUER
       ========================================================= */

    /**
     * Ermittelt die Anzahl der Übernachtungen.
     */
    public long ermittleNaechte(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null
                || veranstaltung.getBeginnDatum() == null
                || veranstaltung.getEndeDatum() == null) {

            return 0;
        }

        long naechte = ChronoUnit.DAYS.between(
                veranstaltung.getBeginnDatum(),
                veranstaltung.getEndeDatum()
        );

        return Math.max(0, naechte);
    }

    /**
     * Ermittelt die Anzahl der Veranstaltungstage.
     */
    public long ermittleTage(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null
                || veranstaltung.getBeginnDatum() == null
                || veranstaltung.getEndeDatum() == null) {

            return 0;
        }

        return ermittleNaechte(veranstaltung) + 1;
    }

    /* =========================================================
       STATUS
       ========================================================= */

    public boolean hatUnterkunft(
            Veranstaltung veranstaltung
    ) {

        return veranstaltung != null
                && veranstaltung.getUnterkunftsart() != null;
    }

    public boolean hatVerpflegung(
            Veranstaltung veranstaltung
    ) {

        return veranstaltung != null
                && veranstaltung.getVerpflegungsmodell() != null;
    }

    /* =========================================================
       HELFER
       ========================================================= */

    private int n(Integer value) {
        return value == null ? 0 : value;
    }
}