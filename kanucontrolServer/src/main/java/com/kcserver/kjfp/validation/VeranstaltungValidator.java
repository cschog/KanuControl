package com.kcserver.kjfp.validation;

import com.kcserver.core.validation.ValidationMessage;
import com.kcserver.core.validation.ValidationResult;
import com.kcserver.kjfp.dto.validation.ValidationResultDTO;
import com.kcserver.kjfp.entity.Teilnehmer;
import com.kcserver.kjfp.entity.Veranstaltung;
import com.kcserver.core.exception.BusinessRuleViolationException;
import com.kcserver.core.exception.ErrorMessages;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VeranstaltungValidator {

    public ValidationResult validate(Veranstaltung veranstaltung) {

        ValidationResult result = new ValidationResult();

        if (veranstaltung == null) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_REQUIRED,
                    null
            );
            return result;
        }

        if (isBlank(veranstaltung.getName())) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_NAME_REQUIRED,
                    "name"
            );
        }

        if (isBlank(veranstaltung.getPlz())) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_PLZ_REQUIRED,
                    "plz"
            );
        }

        if (isBlank(veranstaltung.getOrt())) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_ORT_REQUIRED,
                    "ort"
            );
        }

        if (veranstaltung.getCountryCode() == null) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_LAND_REQUIRED,
                    "countryCode"
            );
        }

        if (veranstaltung.getBeginnDatum() == null) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_BEGINN_REQUIRED,
                    "beginnDatum"
            );
        }

        if (veranstaltung.getBeginnZeit() == null) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_BEGINN_ZEIT_REQUIRED,
                    "beginnZeit"
            );
        }

        if (veranstaltung.getEndeDatum() == null) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_ENDE_REQUIRED,
                    "endeDatum"
            );
        }

        if (veranstaltung.getEndeZeit() == null) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_ENDE_ZEIT_REQUIRED,
                    "endeZeit"
            );
        }

        if (veranstaltung.getVerein() == null) {
            result.addError(
                    ErrorMessages.VERANSTALTUNG_VEREIN_REQUIRED,
                    "verein"
            );
        }

        if (veranstaltung.getLeiter() == null) {
            result.addError(
                    ErrorMessages.VERANSTALTUNGSLEITER_REQUIRED,
                    "leiter"
            );
        }

        return result;
    }

    public ValidationResultDTO getAbrechnungValidation(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer) {

        List<String> fehler =
                collectAbrechnungFehler(veranstaltung, teilnehmer);

        return new ValidationResultDTO(
                fehler.isEmpty(),
                fehler.stream()
                        .map(message ->
                                ValidationMessage.error(message, null)
                        )
                        .toList(),
                List.of()
        );
    }

    public void validateAbrechnungFaehigOrThrow(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer) {

        List<String> fehler =
                collectAbrechnungFehler(veranstaltung, teilnehmer);

        if (!fehler.isEmpty()) {
            throw new BusinessRuleViolationException(
                    String.join("\n", fehler)
            );
        }
    }

    private List<String> collectAbrechnungFehler(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer) {

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

        fehler.addAll(validateTeilnehmerdaten(teilnehmer));

        return fehler;
    }

    private List<String> validateTeilnehmerdaten(
            List<Teilnehmer> teilnehmer) {

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
                                + " "
                                + name.trim()
                );
            }
        });

        return fehler;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}