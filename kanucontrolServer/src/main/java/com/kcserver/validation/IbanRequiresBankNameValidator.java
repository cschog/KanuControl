package com.kcserver.validation;

import com.kcserver.dto.person.PersonSaveDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IbanRequiresBankNameValidator
        implements ConstraintValidator<IbanRequiresBankName, PersonSaveDTO> {

    @Override
    public boolean isValid(PersonSaveDTO person, ConstraintValidatorContext context) {

        if (person == null) return true;

        boolean ibanSet =
                person.getIban() != null && !person.getIban().isBlank();
        boolean bankSet =
                person.getBankName() != null && !person.getBankName().isBlank();

        // ✔ entweder keine IBAN ODER Bankname vorhanden → ok
        if (!ibanSet || bankSet) {
            return true;
        }

        // ❌ IBAN gesetzt, aber kein Bankname
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "Bankname muss angegeben werden, wenn eine IBAN gesetzt ist"
                )
                .addPropertyNode("bankName")
                .addConstraintViolation();

        return false;
    }
}