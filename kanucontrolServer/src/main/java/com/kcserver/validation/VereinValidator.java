package com.kcserver.validation;

import com.kcserver.entity.Verein;
import org.springframework.stereotype.Component;

@Component
public class VereinValidator {

    public ValidationResult validate(Verein verein) {

        ValidationResult result = new ValidationResult();

        if (verein == null) {
            result.addError("Verein ist nicht vorhanden.", null);
            return result;
        }

        if (isBlank(verein.getName())) {
            result.addError("Vereinsname ist erforderlich.", "name");
        }

        if (isBlank(verein.getAbk())) {
            result.addError("Vereinsabkürzung ist erforderlich.", "abk");
        }

        if (isBlank(verein.getStrasse())) {
            result.addError("Straße ist erforderlich.", "strasse");
        }

        if (isBlank(verein.getPlz())) {
            result.addError("PLZ ist erforderlich.", "plz");
        }

        if (isBlank(verein.getOrt())) {
            result.addError("Ort ist erforderlich.", "ort");
        }

        if (verein.getCountryCode() == null) {
            result.addError("Land ist erforderlich.", "countryCode");
        }

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}