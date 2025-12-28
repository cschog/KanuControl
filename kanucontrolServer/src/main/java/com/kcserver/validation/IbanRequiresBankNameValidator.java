package com.kcserver.validation;

import com.kcserver.dto.PersonDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IbanRequiresBankNameValidator
        implements ConstraintValidator<IbanRequiresBankName, PersonDTO> {

    @Override
    public boolean isValid(PersonDTO person, ConstraintValidatorContext context) {

        if (person == null) return true;

        boolean ibanSet =person.getIban() != null && !person.getIban(). isBlank();
        boolean bankSet = person.getBankName() != null && !person.getBankName().isBlank();

        if (!ibanSet || bankSet) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "Bank name must be provided when IBAN is set"
                ).addPropertyNode("bankName")
                .addConstraintViolation();

        return false;
    }
}
