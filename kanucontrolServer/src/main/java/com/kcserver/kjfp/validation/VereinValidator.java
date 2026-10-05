package com.kcserver.kjfp.validation;

import com.kcserver.core.validation.ValidationResult;
import com.kcserver.kjfp.entity.Verein;
import com.kcserver.core.exception.ErrorMessages;
import org.springframework.stereotype.Component;

@Component
public class VereinValidator {

    public ValidationResult validate(Verein verein) {

        ValidationResult result = new ValidationResult();

        if (verein == null) {
            result.addError(
                    ErrorMessages.VEREIN_REQUIRED,
                    null
            );
            return result;
        }

        if (isBlank(verein.getName())) {
            result.addError(
                    ErrorMessages.VEREIN_NAME_REQUIRED,
                    "name"
            );
        }

        if (isBlank(verein.getAbk())) {
            result.addError(
                    ErrorMessages.VEREIN_ABK_REQUIRED,
                    "abk"
            );
        }

        if (isBlank(verein.getStrasse())) {
            result.addError(
                    ErrorMessages.VEREIN_STRASSE_REQUIRED,
                    "strasse"
            );
        }

        if (isBlank(verein.getPlz())) {
            result.addError(
                    ErrorMessages.VEREIN_PLZ_REQUIRED,
                    "plz"
            );
        }

        if (isBlank(verein.getOrt())) {
            result.addError(
                    ErrorMessages.VEREIN_ORT_REQUIRED,
                    "ort"
            );
        }

        if (verein.getCountryCode() == null) {
            result.addError(
                    ErrorMessages.VEREIN_LAND_REQUIRED,
                    "countryCode"
            );
        }

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}