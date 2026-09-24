package com.kcserver.service.person;

import com.kcserver.dto.person.PersonDataStatusDTO;
import com.kcserver.dto.validation.DataFieldStatusDTO;
import com.kcserver.dto.validation.DataStatus;
import com.kcserver.entity.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PersonDataStatusService {

    public PersonDataStatusDTO determineStatus(
            Person person,
            boolean isLeiter,
            boolean isFahrer
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
            addRequired(fields, "strasse", person.getStrasse(),
                    "Für den Veranstaltungsleiter erforderlich.");

            addRequired(fields, "plz", person.getPlz(),
                    "Für den Veranstaltungsleiter erforderlich.");

            addRequired(fields, "ort", person.getOrt(),
                    "Für den Veranstaltungsleiter erforderlich.");

            addRequired(fields, "email", person.getEmail(),
                    "Für den Veranstaltungsleiter erforderlich.");

            addRequired(fields, "telefon", person.getTelefon(),
                    "Für den Veranstaltungsleiter erforderlich.");
        }

        /*
         * Reisekostenfahrer:
         *
         * Bankdaten sind nicht zwingend.
         * Fehlende Daten erzeugen deshalb nur WARNING.
         *
         * Mitfahrer werden hier ausdrücklich nicht berücksichtigt.
         */
        if (isFahrer) {
            addRecommended(fields, "bankName", person.getBankName(),
                    "Bank wird für die Reisekostenabrechnung empfohlen.");

            addRecommended(fields, "iban", person.getIban(),
                    "IBAN wird für die Reisekostenabrechnung empfohlen.");

            addRecommended(fields, "bic", person.getBic(),
                    "BIC wird für die Reisekostenabrechnung empfohlen.");
        }

        DataStatus overallStatus = determineOverallStatus(fields);

        return new PersonDataStatusDTO(overallStatus, fields);
    }

    private void addRequired(
            Map<String, DataFieldStatusDTO> fields,
            String field,
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            fields.put(
                    field,
                    new DataFieldStatusDTO(DataStatus.ERROR, message)
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
                    new DataFieldStatusDTO(DataStatus.WARNING, message)
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