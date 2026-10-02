package com.kcserver.service;

import com.kcserver.dto.validation.DataFieldStatusDTO;
import com.kcserver.dto.validation.DataStatus;
import com.kcserver.dto.validation.VereinDataStatusDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.exception.ErrorMessages;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class VereinDataStatusService {

    public VereinDataStatusDTO determineStatus(
            Verein verein,
            boolean isVeranstalter
    ) {

        Map<String, DataFieldStatusDTO> fields = new HashMap<>();

        if (isVeranstalter) {

            // =====================================================
            // Adresse Verein
            // =====================================================

            addRequired(
                    fields,
                    "strasse",
                    verein.getStrasse(),
                    ErrorMessages.VEREIN_STRASSE_REQUIRED
            );

            addRequired(
                    fields,
                    "plz",
                    verein.getPlz(),
                    ErrorMessages.VEREIN_PLZ_REQUIRED
            );

            addRequired(
                    fields,
                    "ort",
                    verein.getOrt(),
                    ErrorMessages.VEREIN_ORT_REQUIRED
            );

            // =====================================================
            // Bankdaten Verein – weiterhin nur empfohlen
            // =====================================================

            addRecommended(
                    fields,
                    "bankName",
                    verein.getBankName(),
                    ErrorMessages.VERANSTALTER_BANKNAME_RECOMMENDED
            );

            addRecommended(
                    fields,
                    "iban",
                    verein.getIban(),
                    ErrorMessages.VERANSTALTER_IBAN_RECOMMENDED
            );

            addRecommended(
                    fields,
                    "bic",
                    verein.getBic(),
                    ErrorMessages.VERANSTALTER_BIC_RECOMMENDED
            );

            // =====================================================
            // Schutzkonzept
            // =====================================================

            addRecommended(
                    fields,
                    "schutzkonzept",
                    verein.getSchutzkonzept(),
                    ErrorMessages.VERANSTALTER_SCHUTZKONZEPT_RECOMMENDED
            );

            // =====================================================
            // Kontoinhaber
            // =====================================================

            validateKontoinhaber(
                    fields,
                    verein.getKontoinhaber()
            );
        }

        DataStatus overallStatus = determineOverallStatus(fields);

        return new VereinDataStatusDTO(
                overallStatus,
                fields
        );
    }

    private void validateKontoinhaber(
            Map<String, DataFieldStatusDTO> fields,
            Person kontoinhaber
    ) {
        if (kontoinhaber == null) {
            addRequired(
                    fields,
                    "kontoinhaber",
                    null,
                    ErrorMessages.KONTOINHABER_REQUIRED
            );
            return;
        }

        if (kontoinhaber.getGeburtsdatum() == null) {
            addRequired(
                    fields,
                    "kontoinhaberGeburtsdatum",
                    null,
                    ErrorMessages.KONTOINHABER_GEBURTSDATUM_REQUIRED
            );
        } else if (kontoinhaber.getGeburtsdatum()
                .plusYears(18)
                .isAfter(LocalDate.now())) {

            addRequired(
                    fields,
                    "kontoinhaberGeburtsdatum",
                    kontoinhaber.getGeburtsdatum(),
                    ErrorMessages.KONTOINHABER_MIND_ALTER
            );
        }

        addRequired(
                fields,
                "kontoinhaberStrasse",
                kontoinhaber.getStrasse(),
                ErrorMessages.KONTOINHABER_STRASSE_REQUIRED
        );

        addRequired(
                fields,
                "kontoinhaberPlz",
                kontoinhaber.getPlz(),
                ErrorMessages.KONTOINHABER_PLZ_REQUIRED
        );

        addRequired(
                fields,
                "kontoinhaberOrt",
                kontoinhaber.getOrt(),
                ErrorMessages.KONTOINHABER_ORT_REQUIRED
        );

        // =====================================================
// Bankdaten Kontoinhaber – empfohlen
// =====================================================

        addRecommended(
                fields,
                "kontoinhaberBankName",
                kontoinhaber.getBankName(),
                ErrorMessages.KONTOINHABER_BANKNAME_RECOMMENDED
        );

        addRecommended(
                fields,
                "kontoinhaberIban",
                kontoinhaber.getIban(),
                ErrorMessages.KONTOINHABER_IBAN_RECOMMENDED
        );

        addRecommended(
                fields,
                "kontoinhaberBic",
                kontoinhaber.getBic(),
                ErrorMessages.KONTOINHABER_BIC_RECOMMENDED
        );
    }

    private void addRequired(
            Map<String, DataFieldStatusDTO> fields,
            String field,
            Object value,
            String message
    ) {
        if (value == null ||
                (value instanceof String string && string.isBlank())) {

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
            Object value,
            String message
    ) {
        if (value == null ||
                (value instanceof String string && string.isBlank())) {

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