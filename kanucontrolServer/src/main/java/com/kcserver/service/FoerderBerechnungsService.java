package com.kcserver.service;

import com.kcserver.entity.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoerderBerechnungsService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final FoerdersatzService foerdersatzService;
    private final KikZuschlagService kikZuschlagService;

    public BigDecimal berechneFoerderung(Long veranstaltungId) {

        Veranstaltung veranstaltung = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow();

        // Nur FM & JEM werden gefördert
        if (veranstaltung.getTyp() != VeranstaltungTyp.FM &&
                veranstaltung.getTyp() != VeranstaltungTyp.JEM) {
            return BigDecimal.ZERO;
        }

        LocalDate start = veranstaltung.getBeginnDatum();
        LocalDate end = veranstaltung.getEndeDatum();

        long tage = ChronoUnit.DAYS.between(start, end) + 1;

        Foerdersatz fs =
                foerdersatzService.findOptionalGueltigFuerTypAm(
                        veranstaltung.getTyp(),
                        start
                );

        if (fs == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal foerdersatz = fs.getFoerdersatz();
        BigDecimal deckel = fs.getFoerderdeckel();

        BigDecimal kik = BigDecimal.ZERO;

        if (veranstaltung.getVerein()
                .isKikZertifiziertAm(start)) {

            KikZuschlag kz =
                    kikZuschlagService.findOptionalGueltigAm(start);

            if (kz != null) {
                kik = kz.getKikZuschlag();
            }
        }

        BigDecimal proTagProTeilnehmer =
                foerdersatz.add(kik);

        if (deckel != null &&
                proTagProTeilnehmer.compareTo(deckel) > 0) {
            proTagProTeilnehmer = deckel;
        }

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository
                        .findAllWithPerson(veranstaltungId);

        BigDecimal gesamt = BigDecimal.ZERO;

        for (Teilnehmer t : teilnehmer) {

            if (!istFoerderfaehig(t, start)) {
                continue;
            }

            BigDecimal betrag =
                    proTagProTeilnehmer
                            .multiply(BigDecimal.valueOf(tage));

            gesamt = gesamt.add(betrag);
        }

        return gesamt;
    }

    private boolean istFoerderfaehig(
            Teilnehmer t,
            LocalDate veranstaltungsStart
    ) {

        if (t.getPerson().getGeburtsdatum() == null) {
            return false;
        }

        int alter = Period
                .between(
                        t.getPerson().getGeburtsdatum(),
                        veranstaltungsStart
                )
                .getYears();

        return alter >= 6 && alter <= 20;
    }
}