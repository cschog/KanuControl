package com.kcserver.validation;

import com.kcserver.dto.validation.ValidationResultDTO;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.exception.BusinessRuleViolationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VeranstaltungValidator {

    public ValidationResult validate(Veranstaltung veranstaltung) {

        ValidationResult result = new ValidationResult();

        if (veranstaltung == null) {
            result.addError("Veranstaltung ist nicht vorhanden.", null);
            return result;
        }

        if (isBlank(veranstaltung.getName())) {
            result.addError(
                    "Name der Veranstaltung ist erforderlich.",
                    "name"
            );
        }

        if (veranstaltung.getPlz() == null ||
                veranstaltung.getPlz().isBlank()) {
            result.addError(
                    "PLZ des Veranstaltungsortes ist erforderlich.",
                    "plz"
            );
        }

        if (veranstaltung.getOrt() == null ||
                veranstaltung.getOrt().isBlank()) {
            result.addError(
                    "Ort des Veranstaltungsortes ist erforderlich.",
                    "ort"
            );
        }

        if (veranstaltung.getCountryCode() == null) {
            result.addError(
                    "Land des Veranstaltungsortes ist erforderlich.",
                    "countryCode"
            );
        }

        if (veranstaltung.getBeginnDatum() == null) {
            result.addError(
                    "Beginndatum ist erforderlich.",
                    "beginnDatum"
            );
        }

        if (veranstaltung.getBeginnZeit() == null) {
            result.addError(
                    "Beginnzeit ist erforderlich.",
                    "beginnZeit"
            );
        }

        if (veranstaltung.getEndeDatum() == null) {
            result.addError(
                    "Enddatum ist erforderlich.",
                    "endeDatum"
            );
        }

        if (veranstaltung.getEndeZeit() == null) {
            result.addError(
                    "Endzeit ist erforderlich.",
                    "endeZeit"
            );
        }

        if (veranstaltung.getVerein() == null) {
            result.addError(
                    "Ausrichter/Verein ist erforderlich.",
                    "verein"
            );
        }

        if (veranstaltung.getLeiter() == null) {
            result.addError(
                    "Leiter ist erforderlich.",
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
                fehler
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
            fehler.add("Keine Veranstaltung vorhanden.");
            return fehler;
        }

        if (veranstaltung.getLeiter() == null) {
            fehler.add("Leiter fehlt.");
        }

        if (veranstaltung.getBeginnDatum() == null) {
            fehler.add("Beginn-Datum fehlt.");
        }

        if (veranstaltung.getEndeDatum() == null) {
            fehler.add("Ende-Datum fehlt.");
        }

        if (veranstaltung.getTyp() == null) {
            fehler.add("Veranstaltungstyp fehlt.");
        }

        fehler.addAll(validateTeilnehmerdaten(teilnehmer));

        return fehler;
    }

    private List<String> validateTeilnehmerdaten(
            List<Teilnehmer> teilnehmer) {

        List<String> fehler = new ArrayList<>();

        if (teilnehmer == null || teilnehmer.isEmpty()) {
            fehler.add("Keine Teilnehmer vorhanden.");
            return fehler;
        }

        teilnehmer.forEach(t -> {

            if (t == null) {
                fehler.add("Teilnehmer ist leer.");
                return;
            }

            if (t.getPerson() == null) {
                fehler.add("Teilnehmer ohne Person.");
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
                        "Geburtsdatum fehlt bei: "
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