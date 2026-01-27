package com.kcserver.mapper;

import com.kcserver.dto.PersonDTO;
import com.kcserver.entity.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PersonMapperTest {

    @Autowired
    PersonMapper mapper;

    @Test
    void personDto_containsCorrectAge() {
        // given
        LocalDate geb = LocalDate.now().minusYears(30);
        Person p = new Person();
        p.setGeburtsdatum(geb);

        // when
        PersonDTO dto = mapper.toDTO(p);

        // then
        assertThat(dto.getAlter()).isEqualTo(30);
    }
    @Test
    void personDto_withNullBirthdate_hasNullAge() {
        Person p = new Person();
        p.setGeburtsdatum(null);

        PersonDTO dto = mapper.toDTO(p);

        assertThat(dto.getAlter()).isNull();
    }

    @Test
    void personDto_birthdayToday_ageIsZero() {
        Person p = new Person();
        p.setGeburtsdatum(LocalDate.now());

        PersonDTO dto = mapper.toDTO(p);

        assertThat(dto.getAlter()).isZero();
    }
}