package com.kcserver.service;

import com.kcserver.entity.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class FoerderService {

    private final FoerdersatzService foerdersatzService;
    private final KikZuschlagService kikZuschlagService;

    /**
     * Berechnet die Förderung pro Tag und Teilnehmer.
     */
    public BigDecimal berechneFoerderungProTagUndTeilnehmer(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        LocalDate datum = veranstaltung.getBeginnDatum();

        // 1️⃣ passenden Foerdersatz laden (typ + datum)
        Foerdersatz fs =
                foerdersatzService.findEntityGueltigFuerTypAm(
                        veranstaltung.getTyp(),
                        datum
                );

        BigDecimal grund = fs.getFoerdersatz();
        BigDecimal deckel = fs.getFoerderdeckel();

        BigDecimal zuschlag = BigDecimal.ZERO;

        // 2️⃣ KiK nur bei FM/JEM prüfen
        if (istFmOderJem(veranstaltung)
                && veranstaltung.getVerein().isKikZertifiziertAm(datum)
                && istZwischen6Und20(teilnehmer, datum)
        ) {

            KikZuschlag kik =
                    kikZuschlagService.findOptionalGueltigAm(datum);

            zuschlag = kik.getKikZuschlag();
        }

        // 3️⃣ Gesamt berechnen
        BigDecimal gesamt = grund.add(zuschlag);

        // 4️⃣ Deckel anwenden (immer!)
        return gesamt.min(deckel);
    }

    private boolean istFmOderJem(Veranstaltung v) {
        VeranstaltungTyp typ = v.getTyp();
        return typ == VeranstaltungTyp.FM
                || typ == VeranstaltungTyp.JEM;
    }

    private boolean istZwischen6Und20(
            Teilnehmer teilnehmer,
            LocalDate stichtag
    ) {

        LocalDate geburt =
                teilnehmer.getPerson().getGeburtsdatum();

        if (geburt == null) return false;

        int alter =
                Period.between(geburt, stichtag).getYears();

        return alter >= 6 && alter <= 20;
    }
}