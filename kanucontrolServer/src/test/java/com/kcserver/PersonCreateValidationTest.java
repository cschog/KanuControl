package com.kcserver;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.PersonDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.validation.OnCreate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonCreateValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createPerson_withExactlyOneHauptverein_isValid() {
        PersonDTO dto = validPerson();

        Set<ConstraintViolation<PersonDTO>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isEmpty();
    }

    @Test
    void createPerson_withoutHauptverein_isInvalid() {
        PersonDTO dto = validPerson();
        dto.getMitgliedschaften().forEach(m -> m.setHauptVerein(false));

        Set<ConstraintViolation<PersonDTO>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Exactly one Mitglied must be marked as Hauptverein");
    }

    @Test
    void createPerson_withTwoHauptvereine_isInvalid() {
        PersonDTO dto = validPerson();

        MitgliedDTO secondMitglied = new MitgliedDTO(
                null,
                2L,
                null,
                null,
                MitgliedFunktion.JUGENDWART,
                true
        );

        dto.getMitgliedschaften().add(secondMitglied);

        Set<ConstraintViolation<PersonDTO>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void createPerson_missingName_shouldFail() {
        PersonDTO dto = new PersonDTO();
        dto.setVorname("Chris");

        Set<ConstraintViolation<PersonDTO>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isNotEmpty();
    }

    /* =========================================================
       Testdaten
       ========================================================= */

    private PersonDTO validPerson() {
        PersonDTO dto = new PersonDTO();
        dto.setName("Müller");
        dto.setVorname("Anna");

        MitgliedDTO mitglied = new MitgliedDTO(
                null,
                1L,
                null,
                null,
                MitgliedFunktion.JUGENDWART,
                true
        );

        List<MitgliedDTO> list = new ArrayList<>();
        list.add(mitglied);
        dto.setMitgliedschaften(list);
        return dto;
    }
}