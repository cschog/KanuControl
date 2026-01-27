package com.kcserver.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExactlyOneHauptvereinValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExactlyOneHauptverein {

    String message() default
            "Exactly one Mitglied must be marked as Hauptverein";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}