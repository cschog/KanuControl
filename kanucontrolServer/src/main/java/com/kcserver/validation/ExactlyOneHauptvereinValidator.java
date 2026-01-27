package com.kcserver.validation;

import com.kcserver.dto.HasMitgliedschaften;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExactlyOneHauptvereinValidator
        implements ConstraintValidator<ExactlyOneHauptverein, HasMitgliedschaften> {

    @Override
    public boolean isValid(
            HasMitgliedschaften person,
            ConstraintValidatorContext context
    ) {

        if (person == null) return true;

        var mitgliedschaften = person.getMitgliedschaften();
        if (mitgliedschaften == null || mitgliedschaften.isEmpty()) {
            return true; // Person ohne Verein ist erlaubt
        }

        long count = mitgliedschaften.stream()
                .filter(m -> Boolean.TRUE.equals(m.getHauptVerein()))
                .count();

        if (count == 1) return true;

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "Exactly one Mitglied must be marked as Hauptverein"
                )
                .addPropertyNode("mitgliedschaften")
                .addConstraintViolation();

        return false;
    }
}