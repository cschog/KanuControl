package com.kcserver;

import com.kcserver.dto.MitgliedSaveDTO;
import com.kcserver.dto.PersonSaveDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.enumtype.Sex;
import com.kcserver.validation.OnCreate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonCreateValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void createPerson_withExactlyOneHauptverein_isValid() {
        PersonSaveDTO dto = validPerson();

        Set<ConstraintViolation<PersonSaveDTO>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isEmpty();
    }

    @Test
    void createPerson_withTwoHauptvereine_isInvalid() {
        PersonSaveDTO dto = validPerson();

        MitgliedSaveDTO second = new MitgliedSaveDTO();
        second.setVereinId(2L);
        second.setHauptVerein(true);
        second.setFunktion(MitgliedFunktion.JUGENDWART);

        dto.getMitgliedschaften().add(second);

        Set<ConstraintViolation<PersonSaveDTO>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void createPerson_missingName_shouldFail() {
        PersonSaveDTO dto = new PersonSaveDTO();
        dto.setVorname("Chris");
        dto.setSex(Sex.MAENNLICH);

        Set<ConstraintViolation<PersonSaveDTO>> violations =
                validator.validate(dto, OnCreate.class);

        assertThat(violations).isNotEmpty();
    }

    /* =========================================================
       Testdaten
       ========================================================= */

    private PersonSaveDTO validPerson() {
        PersonSaveDTO dto = new PersonSaveDTO();
        dto.setName("Müller");
        dto.setVorname("Anna");
        dto.setSex(Sex.WEIBLICH);

        MitgliedSaveDTO mitglied = new MitgliedSaveDTO();
        mitglied.setVereinId(1L);
        mitglied.setHauptVerein(true);
        mitglied.setFunktion(MitgliedFunktion.JUGENDWART);

        dto.setMitgliedschaften(new ArrayList<>(List.of(mitglied)));
        return dto;
    }
}