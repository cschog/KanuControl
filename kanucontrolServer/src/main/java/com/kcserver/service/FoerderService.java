package com.kcserver.service;

import com.kcserver.config.FoerderConfig;
import com.kcserver.dto.foerder.FoerdersatzLookupResult;
import com.kcserver.entity.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FoerderService {

    private final FoerdersatzService foerdersatzService;
    private final KikZuschlagService kikZuschlagService;
    private final AltersService altersService;

    private static final int MAX_FOERDERTAGE_FM_JEM = 21;
    private boolean isFmJem(Veranstaltung veranstaltung) {
        return veranstaltung.getTyp() == VeranstaltungTyp.FM
                || veranstaltung.getTyp() == VeranstaltungTyp.JEM;
    }

    public boolean istFoerderfaehig(

            Veranstaltung veranstaltung,

            Teilnehmer teilnehmer

    ) {

        if (veranstaltung == null || teilnehmer == null) {
            return false;
        }

        VeranstaltungTyp typ = veranstaltung.getTyp();

        if (typ == null || !typ.isFoerderfaehig()) {

            return false;

        }

        // Mitarbeiter/Leiter nicht förderfähig

        if (teilnehmer.getRolle() != null) {

            return false;

        }

        LocalDate geburt =
                teilnehmer.getPerson() != null
                        ? teilnehmer.getPerson().getGeburtsdatum()
                        : null;

        Integer alter =
                altersService.berechneAlterBeiBeginn(
                        geburt,
                        veranstaltung.getBeginnDatum()
                );

        if (alter == null) {

            return false;

        }

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
        if (veranstaltung == null
                || veranstaltung.getBeginnDatum() == null
                || veranstaltung.getEndeDatum() == null) {
            return 0;
        }
        int tage = (int) ChronoUnit.DAYS.between(
                veranstaltung.getBeginnDatum(),
                veranstaltung.getEndeDatum()
        ) + 1;

        if (isFmJem(veranstaltung)) {
            return Math.min(tage, MAX_FOERDERTAGE_FM_JEM);
        }

        return tage;

    }

    /**
     * Förderung PRO TAG für einen Teilnehmer.
     */
    public BigDecimal berechneFoerderungProTagUndTeilnehmer(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        LocalDate datum = veranstaltung.getBeginnDatum();

        if (datum == null) {
            return BigDecimal.ZERO;
        }

        if (!istFoerderfaehig(veranstaltung, teilnehmer)) {
            return BigDecimal.ZERO;
        }

        FoerdersatzLookupResult result =
                foerdersatzService.getGueltigOderLetztenMitInfo(
                        veranstaltung.getTyp(),
                        datum
                );

        Foerdersatz fs = result.foerdersatz();

        if (fs.getFoerdersatz() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal grund = fs.getFoerdersatz();
        BigDecimal deckel = FoerderConfig.FOERDERDECKEL;

        BigDecimal zuschlag = BigDecimal.ZERO;

        if (
                veranstaltung.getVerein() != null
                        && veranstaltung.getVerein()
                        .isKikZertifiziertAm(datum)
        ) {

            KikZuschlag kik =
                    kikZuschlagService.findOptionalGueltigAm(datum);

            if (kik != null && kik.getKikZuschlag() != null) {
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

        if (veranstaltung == null) {
            return false;
        }

        VeranstaltungTyp typ = veranstaltung.getTyp();

        return typ == VeranstaltungTyp.FM
                || typ == VeranstaltungTyp.JEM;
    }

    public BigDecimal berechneGesamtfoerderung(

            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        if (veranstaltung == null || teilnehmer == null) {
            return BigDecimal.ZERO;
        }

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

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        LocalDate datum =
                veranstaltung.getBeginnDatum();

        if (datum == null) {
            return BigDecimal.ZERO;
        }

        FoerdersatzLookupResult result =
                foerdersatzService.getGueltigOderLetztenMitInfo(
                        veranstaltung.getTyp(),
                        datum
                );

        Foerdersatz fs = result.foerdersatz();

        if (fs.getFoerdersatz() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal grund =
                fs.getFoerdersatz();

        BigDecimal zuschlag =
                BigDecimal.ZERO;

        if (
                veranstaltung.getVerein() != null
                        && veranstaltung.getVerein()
                        .isKikZertifiziertAm(datum)
        ) {

            KikZuschlag kik =
                    kikZuschlagService.findOptionalGueltigAm(datum);

            if (kik != null && kik.getKikZuschlag() != null) {
                zuschlag = kik.getKikZuschlag();
            }
        }

        return grund
                .add(zuschlag)
                .min(FoerderConfig.FOERDERDECKEL);
    }

    public BigDecimal berechneGeplanteFoerderung(
            Veranstaltung veranstaltung
    ) {

        if (veranstaltung == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal tagessatz =
                berechneAngewandtenFoerdersatz(
                        veranstaltung
                );

        if (tagessatz == null) {
            return BigDecimal.ZERO;
        }

        int tage =
                berechneFoerdertage(
                        veranstaltung
                );

        if (tage <= 0) {
            return BigDecimal.ZERO;
        }

        int teilnehmer =
                n(veranstaltung.getGeplanteTeilnehmerMaennlich())
                        + n(veranstaltung.getGeplanteTeilnehmerWeiblich())
                        + n(veranstaltung.getGeplanteTeilnehmerDivers());

        return tagessatz
                .multiply(BigDecimal.valueOf(teilnehmer))
                .multiply(BigDecimal.valueOf(tage));
    }

    private int n(Integer value) {
        return value == null ? 0 : value;
    }
}