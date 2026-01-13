package com.kcserver.validation;

import com.kcserver.dto.PersonDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExactlyOneHauptvereinValidator
        implements ConstraintValidator<ExactlyOneHauptverein, PersonDTO> {

    @Override
    public boolean isValid(
            PersonDTO person,
            ConstraintValidatorContext context
    ) {

        // Person selbst ist null → andere Validatoren greifen
        if (person == null) {
            return true;
        }

        // ✅ KEINE Mitgliedschaften → erlaubt (reine Person-CRUDs!)
        if (person.getMitgliedschaften() == null
                || person.getMitgliedschaften().isEmpty()) {
            return true;
        }

        long count = person.getMitgliedschaften().stream()
                .filter(m -> Boolean.TRUE.equals(m.getHauptVerein()))
                .count();

        if (count == 1) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "Exactly one Mitglied must be marked as Hauptverein"
                )
                .addPropertyNode("mitgliedschaften")
                .addConstraintViolation();

        return false;
    }
}