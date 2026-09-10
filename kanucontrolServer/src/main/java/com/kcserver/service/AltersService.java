package com.kcserver.service;

import com.kcserver.enumtype.VeranstaltungTyp;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class AltersService {

    /**
     * Berechnet das Alter zu einem beliebigen Stichtag.
     */
    public Integer berechneAlter(
            LocalDate geburtsdatum,
            LocalDate stichtag
    ) {

        if (geburtsdatum == null || stichtag == null) {
            return null;
        }

        if (stichtag.isBefore(geburtsdatum)) {
            return 0;
        }

        return Period.between(
                geburtsdatum,
                stichtag
        ).getYears();
    }

    /**
     * Berechnet das Alter zum Beginn einer Veranstaltung.
     */
    public Integer berechneAlterBeiBeginn(
            LocalDate geburtsdatum,
            LocalDate veranstaltungsbeginn
    ) {

        return berechneAlter(
                geburtsdatum,
                veranstaltungsbeginn
        );
    }

    /**
     * Ermittelt das für die Veranstaltung maßgebliche Alter.
     * .
     * Mindestalter darf während der Veranstaltung erreicht werden.
     * Höchstalter wird zum Beginn der Veranstaltung geprüft.
     */
    public Integer berechneMassgeblichesAlter(
            LocalDate geburtsdatum,
            LocalDate beginnDatum,
            LocalDate endeDatum,
            VeranstaltungTyp typ
    ) {

        if (geburtsdatum == null
                || beginnDatum == null
                || typ == null) {
            return null;
        }

        Integer alterBeiBeginn =
                berechneAlter(
                        geburtsdatum,
                        beginnDatum
                );

        if (alterBeiBeginn == null) {
            return null;
        }

        /*
         * Ohne Enddatum bleibt das Alter zu Beginn maßgeblich.
         */
        if (endeDatum == null) {
            return alterBeiBeginn;
        }

        Integer alterBeiEnde =
                berechneAlter(
                        geburtsdatum,
                        endeDatum
                );

        /*
         * Wird das Mindestalter während der Veranstaltung erreicht,
         * ist das Alter am Ende maßgeblich.
         */
        if (alterBeiBeginn < typ.getMindestalter()
                && alterBeiEnde >= typ.getMindestalter()) {

            return alterBeiEnde;
        }

        /*
         * In allen anderen Fällen ist das Alter zu Beginn maßgeblich.
         */
        return alterBeiBeginn;
    }
}