package com.kcserver.service.person;

import com.kcserver.dto.person.PersonDataStatusDTO;
import com.kcserver.dto.validation.DataFieldStatusDTO;
import com.kcserver.dto.validation.DataStatus;
import com.kcserver.entity.Person;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.service.AltersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PersonDataStatusService {

    private final AltersService altersService;

    /**
     * Allgemeine Statusprüfung einer Person.
     *
     * Diese Methode bleibt für bestehende Aufrufer erhalten.
     */
    public PersonDataStatusDTO determineStatus(
            Person person,
            boolean isLeiter,
            boolean isFahrer,
            boolean isTeilnehmer
    ) {
        return determineStatus(
                person,
                isLeiter,
                isFahrer,
                isTeilnehmer,
                null,
                null
        );
    }

    /**
     * Statusprüfung einer Person im konkreten Veranstaltungskontext.
     *
     * Zusätzlich zur allgemeinen Datenprüfung werden bei einem
     * Teilnehmer die veranstaltungsbezogenen Regeln für Alter und eFZ
     * geprüft.
     */
    public PersonDataStatusDTO determineStatus(
            Person person,
            boolean isLeiter,
            boolean isFahrer,
            boolean isTeilnehmer,
            TeilnehmerRolle rolle,
            LocalDate veranstaltungsDatum
    ) {
        Map<String, DataFieldStatusDTO> fields = new HashMap<>();

        /*
         * Veranstaltungsleiter:
         *
         * Die Person kann nur über die Leiterauswahl zugeordnet werden,
         * wenn sie >= 18 ist und ein Geburtsdatum besitzt.
         *
         * Deshalb ist das hier kein nachträglicher ERROR-Status.
         *
         * Die übrigen Daten werden für die Verwendung als Leiter geprüft.
         */
        if (isLeiter) {
            addRequired(
                    fields,
                    "strasse",
                    person.getStrasse(),
                    "Für den Veranstaltungsleiter erforderlich."
            );

            addRequired(
                    fields,
                    "plz",
                    person.getPlz(),
                    "Für den Veranstaltungsleiter erforderlich."
            );

            addRequired(
                    fields,
                    "ort",
                    person.getOrt(),
                    "Für den Veranstaltungsleiter erforderlich."
            );

            addRequired(
                    fields,
                    "email",
                    person.getEmail(),
                    "Für den Veranstaltungsleiter erforderlich."
            );

            addRequired(
                    fields,
                    "telefon",
                    person.getTelefon(),
                    "Für den Veranstaltungsleiter erforderlich."
            );
        }

        /*
         * Teilnehmer:
         *
         * Sobald eine Person Teilnehmer einer Veranstaltung ist,
         * sind Geburtsdatum, PLZ und Ort Pflichtangaben.
         */
        if (isTeilnehmer) {
            addRequired(
                    fields,
                    "geburtsdatum",
                    person.getGeburtsdatum(),
                    "Für einen Teilnehmer ist das Geburtsdatum erforderlich."
            );

            addRequired(
                    fields,
                    "plz",
                    person.getPlz(),
                    "Für einen Teilnehmer ist die PLZ erforderlich."
            );

            addRequired(
                    fields,
                    "ort",
                    person.getOrt(),
                    "Für einen Teilnehmer ist der Ort erforderlich."
            );

            /*
             * Veranstaltungsbezogene Teilnehmerprüfung.
             *
             * Nur durchführen, wenn Rolle und Veranstaltungsdatum
             * bekannt sind.
             */
            if (rolle != null || veranstaltungsDatum != null) {
                addTeilnehmerValidation(
                        fields,
                        person,
                        rolle,
                        veranstaltungsDatum
                );
            }
        }

        /*
         * Reisekostenfahrer:
         *
         * Bankdaten sind nicht zwingend.
         * Fehlende Daten erzeugen deshalb nur WARNING.
         */
        if (isFahrer) {
            addRecommended(
                    fields,
                    "bankName",
                    person.getBankName(),
                    "Bank wird für die Reisekostenabrechnung empfohlen."
            );

            addRecommended(
                    fields,
                    "iban",
                    person.getIban(),
                    "IBAN wird für die Reisekostenabrechnung empfohlen."
            );

            addRecommended(
                    fields,
                    "bic",
                    person.getBic(),
                    "BIC wird für die Reisekostenabrechnung empfohlen."
            );
        }

        DataStatus overallStatus = determineOverallStatus(fields);

        return new PersonDataStatusDTO(
                overallStatus,
                fields
        );
    }

    /**
     * Veranstaltungsbezogene Prüfung eines Teilnehmers.
     *
     * Regeln:
     *
     * LEITER:
     *   - mindestens 18 Jahre
     *   - eFZ erforderlich
     *
     * MITARBEITER:
     *   - mindestens 14 Jahre
     *   - eFZ erforderlich
     *
     * normaler Teilnehmer:
     *   - bis einschließlich 20 Jahre kein eFZ
     *   - ab 21 Jahren eFZ erforderlich
     *
     * Ein fehlendes oder zu altes eFZ erzeugt nur eine WARNING.
     */
    private void addTeilnehmerValidation(
            Map<String, DataFieldStatusDTO> fields,
            Person person,
            TeilnehmerRolle rolle,
            LocalDate veranstaltungsDatum
    ) {

        /*
         * eFZ für Leiter und Mitarbeiter:
         *
         * Diese Pflicht hängt nicht vom Alter ab.
         * Deshalb muss sie auch geprüft werden, wenn
         * das Geburtsdatum noch fehlt.
         */
        boolean efzRequiredByRole =
                rolle == TeilnehmerRolle.LEITER
                        || rolle == TeilnehmerRolle.MITARBEITER;

        if (efzRequiredByRole && veranstaltungsDatum != null) {
            validateEfz(
                    fields,
                    person.getEfz(),
                    veranstaltungsDatum
            );
        }

        /*
         * Ohne Geburtsdatum bzw. Veranstaltungsdatum
         * ist keine Altersprüfung möglich.
         */
        if (person.getGeburtsdatum() == null
                || veranstaltungsDatum == null) {
            return;
        }

        Integer alter = altersService.berechneAlterBeiBeginn(
                person.getGeburtsdatum(),
                veranstaltungsDatum
        );

        if (alter == null) {
            return;
        }

        /*
         * Mitarbeiter müssen mindestens 14 Jahre alt sein.
         */
        if (rolle == TeilnehmerRolle.MITARBEITER
                && alter < 14) {

            fields.put(
                    "geburtsdatum",
                    new DataFieldStatusDTO(
                            DataStatus.ERROR,
                            "Mitarbeiter müssen bei Veranstaltungsbeginn mindestens 14 Jahre alt sein."
                    )
            );
        }

        /*
         * Leiter müssen mindestens 18 Jahre alt sein.
         */
        if (rolle == TeilnehmerRolle.LEITER
                && alter < 18) {

            fields.put(
                    "geburtsdatum",
                    new DataFieldStatusDTO(
                            DataStatus.ERROR,
                            "Veranstaltungsleiter müssen bei Veranstaltungsbeginn mindestens 18 Jahre alt sein."
                    )
            );
        }

        /*
         * Normale Teilnehmer:
         *
         * Bis einschließlich 20 Jahre kein eFZ.
         * Ab 21 Jahren eFZ erforderlich.
         */
        if (rolle == null && alter > 20) {
            validateEfz(
                    fields,
                    person.getEfz(),
                    veranstaltungsDatum
            );
        }
    }

    /**
     * Prüft das eFZ zum Beginn der Veranstaltung.
     *
     * Das eFZ darf maximal 5 Jahre alt sein.
     *
     * Fehlendes oder zu altes eFZ ist bewusst nur eine WARNING.
     */
    private void validateEfz(
            Map<String, DataFieldStatusDTO> fields,
            LocalDate efz,
            LocalDate veranstaltungsDatum
    ) {
        if (efz == null) {
            fields.put(
                    "efz",
                    new DataFieldStatusDTO(
                            DataStatus.WARNING,
                            "Für diese Person ist ein gültiges erweitertes Führungszeugnis erforderlich."
                    )
            );
            return;
        }

        if (efz.plusYears(5).isBefore(veranstaltungsDatum)) {
            fields.put(
                    "efz",
                    new DataFieldStatusDTO(
                            DataStatus.WARNING,
                            "Das erweiterte Führungszeugnis ist bei Veranstaltungsbeginn älter als 5 Jahre."
                    )
            );
        }
    }

    private void addRequired(
            Map<String, DataFieldStatusDTO> fields,
            String field,
            Object value,
            String message
    ) {
        if (value == null
                || (value instanceof String s && s.isBlank())) {

            fields.put(
                    field,
                    new DataFieldStatusDTO(
                            DataStatus.ERROR,
                            message
                    )
            );
        }
    }

    private void addRecommended(
            Map<String, DataFieldStatusDTO> fields,
            String field,
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            fields.put(
                    field,
                    new DataFieldStatusDTO(
                            DataStatus.WARNING,
                            message
                    )
            );
        }
    }

    private DataStatus determineOverallStatus(
            Map<String, DataFieldStatusDTO> fields
    ) {
        if (fields.values().stream()
                .anyMatch(f -> f.getStatus() == DataStatus.ERROR)) {

            return DataStatus.ERROR;
        }

        if (fields.values().stream()
                .anyMatch(f -> f.getStatus() == DataStatus.WARNING)) {

            return DataStatus.WARNING;
        }

        return DataStatus.OK;
    }
}