package com.kcserver.validation;

import com.kcserver.dto.mitglied.HasMitgliedschaften;
import com.kcserver.dto.verein.HasHauptverein;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExactlyOneHauptvereinValidator
        implements ConstraintValidator<
        ExactlyOneHauptverein,
        HasMitgliedschaften<? extends HasHauptverein>> {

    @Override
    public boolean isValid(
            HasMitgliedschaften<? extends HasHauptverein> person,
            ConstraintValidatorContext context
    ) {

        if (person == null) {
            return true;
        }

        var mitgliedschaften = person.getMitgliedschaften();

        if (mitgliedschaften == null || mitgliedschaften.isEmpty()) {
            return true;
        }

        long count = mitgliedschaften.stream()
                .filter(m -> Boolean.TRUE.equals(m.getHauptVerein()))
                .count();

        if (count == 1) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("mitgliedschaften")
                .addConstraintViolation();

        return false;
    }
}