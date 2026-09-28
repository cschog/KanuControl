package com.kcserver.validation;

import com.kcserver.entity.Person;
import com.kcserver.exception.ErrorMessages;
import org.springframework.stereotype.Component;

@Component
public class PersonValidator {

    public ValidationResult validate(Person person) {

        ValidationResult result = new ValidationResult();

        if (person == null) {
            result.addError(
                    ErrorMessages.PERSON_REQUIRED,
                    null
            );
            return result;
        }

        if (isBlank(person.getName())) {
            result.addError(
                    ErrorMessages.PERSON_NAME_REQUIRED,
                    "name"
            );
        }

        if (isBlank(person.getVorname())) {
            result.addError(
                    ErrorMessages.PERSON_VORNAME_REQUIRED,
                    "vorname"
            );
        }

        if (person.getSex() == null) {
            result.addError(
                    ErrorMessages.PERSON_SEX_REQUIRED,
                    "sex"
            );
        }

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}