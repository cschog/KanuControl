package com.kcserver.kjfp.service;

import com.kcserver.kjfp.entity.Teilnehmer;
import com.kcserver.kjfp.entity.Veranstaltung;
import com.kcserver.core.exception.BusinessRuleViolationException;
import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.repository.TeilnehmerRepository;
import com.kcserver.kjfp.repository.VeranstaltungRepository;
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
                    ErrorMessages.TEILNEHMER_NOT_IN_VERANSTALTUNG
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