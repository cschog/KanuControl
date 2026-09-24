package com.kcserver.validation;

import com.kcserver.entity.Person;
import org.springframework.stereotype.Component;

@Component
public class PersonValidator {

    public ValidationResult validate(Person person) {

        ValidationResult result = new ValidationResult();

        if (person == null) {
            result.addError("Person ist nicht vorhanden.", null);
            return result;
        }

        if (isBlank(person.getName())) {
            result.addError("Nachname ist erforderlich.", "name");
        }

        if (isBlank(person.getVorname())) {
            result.addError("Vorname ist erforderlich.", "vorname");
        }

        if (person.getSex() == null) {
            result.addError("Geschlecht ist erforderlich.", "sex");
        }

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}