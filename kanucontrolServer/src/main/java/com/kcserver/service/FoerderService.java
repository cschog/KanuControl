package com.kcserver.service;

import com.kcserver.config.FoerderConfig;
import com.kcserver.entity.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoerderService {

    private final FoerdersatzService foerdersatzService;
    private final KikZuschlagService kikZuschlagService;

    public boolean istFoerderfaehig(

            Veranstaltung veranstaltung,

            Teilnehmer teilnehmer

    ) {

        VeranstaltungTyp typ = veranstaltung.getTyp();

        if (!typ.isFoerderfaehig()) {

            return false;

        }

        // Mitarbeiter/Leiter nicht förderfähig

        if (teilnehmer.getRolle() != null) {

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

        return alter >= typ.getMindestalter()
                && alter <= typ.getHoechstalter();

    }

    public long countFoerderfaehigeTeilnehmer(

            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {
        return teilnehmer.stream()
                .filter(t ->
                        istFoerderfaehig(
                                veranstaltung,
                                t
                        )
                )
                .count();
    }

    public int berechneFoerdertage(
            Veranstaltung veranstaltung
    ) {

        return (int)
                ChronoUnit.DAYS.between(
                        veranstaltung.getBeginnDatum(),
                        veranstaltung.getEndeDatum()
                ) + 1;

    }

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
        BigDecimal deckel = FoerderConfig.FOERDERDECKEL;

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


    private boolean istFmOderJem(Veranstaltung veranstaltung) {

        VeranstaltungTyp typ = veranstaltung.getTyp();

        return typ == VeranstaltungTyp.FM
                || typ == VeranstaltungTyp.JEM;
    }

    public BigDecimal berechneGesamtfoerderung(

            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {
        int tage =
                berechneFoerdertage(veranstaltung);

        return teilnehmer.stream()

                .filter(t ->
                        istFoerderfaehig(
                                veranstaltung,
                                t
                        )
                )

                .map(t ->
                        berechneFoerderungProTagUndTeilnehmer(
                                veranstaltung,
                                t
                        )
                )

                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(tage));
    }
    public BigDecimal berechneAngewandtenFoerdersatz(
            Veranstaltung veranstaltung
    ) {

        LocalDate datum =
                veranstaltung.getBeginnDatum();

        Foerdersatz fs =
                foerdersatzService.findEntityGueltigFuerTypAm(
                        veranstaltung.getTyp(),
                        datum
                );

        BigDecimal grund =
                fs.getFoerdersatz();

        BigDecimal zuschlag =
                BigDecimal.ZERO;

        if (veranstaltung.getVerein()
                .isKikZertifiziertAm(datum)) {

            KikZuschlag kik =
                    kikZuschlagService.findOptionalGueltigAm(datum);

            if (kik != null) {
                zuschlag = kik.getKikZuschlag();
            }
        }

        return grund
                .add(zuschlag)
                .min(FoerderConfig.FOERDERDECKEL);
    }
}