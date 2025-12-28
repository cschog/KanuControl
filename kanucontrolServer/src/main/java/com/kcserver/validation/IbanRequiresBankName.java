package com.kcserver.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IbanRequiresBankNameValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IbanRequiresBankName {

    String message() default "IBAN requires bank name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
