package com.kcserver.service;

import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.exception.BusinessRuleViolationException;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeranstaltungFoerderService {

    private static final int MIN_FOERDERFAEHIGE =
            7;

    private static final long MAX_TAGE =
            21;

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final FoerderService foerderService;

    public BigDecimal berechneFoerderung(
            Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow();

        long tage =
                ChronoUnit.DAYS.between(
                        veranstaltung.getBeginnDatum(),
                        veranstaltung.getEndeDatum()
                ) + 1;

        // max. 21 Tage
        tage = Math.min(tage, MAX_TAGE);

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository
                        .findAllWithPerson(veranstaltungId);

        // Pflichtfeldprüfung

        boolean missingBirthdate =
                teilnehmer.stream()
                        .anyMatch(t ->
                                t.getPerson()
                                        .getGeburtsdatum() == null
                        );
        if (missingBirthdate) {
            throw new BusinessRuleViolationException(
                    "Alle Teilnehmer benötigen ein Geburtsdatum für die Förderberechnung"
            );
        }

        // nur förderfähige Teilnehmer zählen
        List<Teilnehmer> foerderfaehige =
                teilnehmer.stream()
                        .filter(t ->
                                foerderService
                                        .istFoerderfaehig(
                                                veranstaltung,
                                                t
                                        )
                        )
                        .toList();

        // mindestens 7 förderfähige Teilnehmer nötig
        if (foerderfaehige.size()
                < MIN_FOERDERFAEHIGE) {

            return BigDecimal.ZERO;
        }

        BigDecimal gesamt = BigDecimal.ZERO;

        for (Teilnehmer t : foerderfaehige) {

            BigDecimal proTag =
                    foerderService
                            .berechneFoerderungProTagUndTeilnehmer(
                                    veranstaltung,
                                    t
                            );

            BigDecimal betrag =
                    proTag.multiply(
                            BigDecimal.valueOf(tage)
                    );

            gesamt = gesamt.add(betrag);
        }

        return gesamt;
    }
}