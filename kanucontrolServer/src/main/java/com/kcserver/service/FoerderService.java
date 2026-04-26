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
     * Förderung PRO TAG für einen Teilnehmer.
     */
    public BigDecimal berechneFoerderungProTagUndTeilnehmer(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        LocalDate datum = veranstaltung.getBeginnDatum();

        if (!istFoerderfaehig(veranstaltung, teilnehmer)) {
            return BigDecimal.ZERO;
        }

        Foerdersatz fs =
                foerdersatzService.findEntityGueltigFuerTypAm(
                        veranstaltung.getTyp(),
                        datum
                );

        BigDecimal grund = fs.getFoerdersatz();
        BigDecimal deckel = fs.getFoerderdeckel();

        BigDecimal zuschlag = BigDecimal.ZERO;

        if (veranstaltung.getVerein()
                .isKikZertifiziertAm(datum)) {

            KikZuschlag kik =
                    kikZuschlagService.findOptionalGueltigAm(datum);

            if (kik != null) {
                zuschlag = kik.getKikZuschlag();
            }
        }

        BigDecimal gesamt = grund.add(zuschlag);

        if (deckel != null) {
            gesamt = gesamt.min(deckel);
        }

        return gesamt;
    }

    /**
     * Zentrale Förderfähigkeitsprüfung.
     */
    public boolean istFoerderfaehig(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        if (!istFmOderJem(veranstaltung)) {
            return false;
        }

        LocalDate geburt =
                teilnehmer.getPerson().getGeburtsdatum();

        if (geburt == null) {
            return false;
        }

        int alter =
                Period.between(
                        geburt,
                        veranstaltung.getBeginnDatum()
                ).getYears();

        return alter >= 6 && alter <= 20;
    }

    private boolean istFmOderJem(Veranstaltung veranstaltung) {

        VeranstaltungTyp typ = veranstaltung.getTyp();

        return typ == VeranstaltungTyp.FM
                || typ == VeranstaltungTyp.JEM;
    }
}