package com.kcserver.service.veranstaltung;

import com.kcserver.dto.validation.ValidationResultDTO;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.exception.BusinessRuleViolationException;
import com.kcserver.exception.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VeranstaltungValidator {


    /* =========================================================
       ABRECHNUNG
       ========================================================= */

    public void validateAbrechnungFaehigOrThrow(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        List<String> fehler =
                collectAbrechnungFehler(
                        veranstaltung,
                        teilnehmer
                );

        if (!fehler.isEmpty()) {

            throw new BusinessRuleViolationException(
                    String.join("\n", fehler)
            );
        }
    }

    public ValidationResultDTO getAbrechnungValidation(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        List<String> fehler =
                collectAbrechnungFehler(
                        veranstaltung,
                        teilnehmer
                );

        return new ValidationResultDTO(
                fehler.isEmpty(),
                fehler
        );
    }

    /* =========================================================
       FÖRDERUNG
       ========================================================= */

    public void validateFoerderFaehigOrThrow(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        List<String> fehler = new ArrayList<>();

        if (veranstaltung == null) {
            fehler.add(ErrorMessages.VERANSTALTUNG_REQUIRED);
        }

        if (veranstaltung != null) {

            if (veranstaltung.getTyp() == null) {
                fehler.add(ErrorMessages.VERANSTALTUNGSTYP_REQUIRED);
            }

            if (veranstaltung.getBeginnDatum() == null) {
                fehler.add(ErrorMessages.VERANSTALTUNG_BEGINN_REQUIRED);
            }

            if (veranstaltung.getEndeDatum() == null) {
                fehler.add(ErrorMessages.VERANSTALTUNG_ENDE_REQUIRED);
            }

            if (veranstaltung.getVerein() == null) {
                fehler.add(ErrorMessages.VERANSTALTUNG_VEREIN_REQUIRED);
            }
        }

        fehler.addAll(
                validateTeilnehmerdaten(teilnehmer)
        );

        if (!fehler.isEmpty()) {
            throw new BusinessRuleViolationException(
                    String.join("\n", fehler)
            );
        }
    }

    /* =========================================================
       TEILNEHMERDATEN
       ========================================================= */

    public List<String> validateTeilnehmerdaten(
            List<Teilnehmer> teilnehmer
    ) {

        List<String> fehler = new ArrayList<>();

        if (teilnehmer == null || teilnehmer.isEmpty()) {
            fehler.add(ErrorMessages.TEILNEHMER_REQUIRED);
            return fehler;
        }

        teilnehmer.forEach(t -> {

            if (t == null) {
                fehler.add(ErrorMessages.TEILNEHMER_EMPTY);
                return;
            }

            if (t.getPerson() == null) {
                fehler.add(ErrorMessages.TEILNEHMER_OHNE_PERSON);
                return;
            }

            String name =
                    (t.getPerson().getVorname() != null
                            ? t.getPerson().getVorname()
                            : "")
                            + " "
                            + (t.getPerson().getName() != null
                            ? t.getPerson().getName()
                            : "");

            if (t.getPerson().getGeburtsdatum() == null) {
                fehler.add(
                        ErrorMessages.TEILNEHMER_GEBURTSDATUM_REQUIRED
                                + name.trim()
                );
            }
        });

        return fehler;
    }

    /* =========================================================
       INTERN
       ========================================================= */

    private List<String> collectAbrechnungFehler(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        List<String> fehler = new ArrayList<>();

        if (veranstaltung == null) {
            fehler.add(ErrorMessages.VERANSTALTUNG_REQUIRED);
            return fehler;
        }

        if (veranstaltung.getLeiter() == null) {
            fehler.add(ErrorMessages.VERANSTALTUNGSLEITER_REQUIRED);
        }

        if (veranstaltung.getBeginnDatum() == null) {
            fehler.add(ErrorMessages.VERANSTALTUNG_BEGINN_REQUIRED);
        }

        if (veranstaltung.getEndeDatum() == null) {
            fehler.add(ErrorMessages.VERANSTALTUNG_ENDE_REQUIRED);
        }

        if (veranstaltung.getTyp() == null) {
            fehler.add(ErrorMessages.VERANSTALTUNGSTYP_REQUIRED);
        }

        fehler.addAll(
                validateTeilnehmerdaten(teilnehmer)
        );

        return fehler;
    }
}