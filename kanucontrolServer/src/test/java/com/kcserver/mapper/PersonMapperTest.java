package com.kcserver.mapper;

import com.kcserver.dto.PersonListDTO;
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
    void personListDto_containsCorrectAge() {
        // given
        LocalDate geb = LocalDate.now().minusYears(30);
        Person p = new Person();
        p.setGeburtsdatum(geb);

        // when
        PersonListDTO dto = mapper.toListDTO(p);

        // then
        assertThat(dto.getAlter()).isEqualTo(30);
    }

    @Test
    void personListDto_withNullBirthdate_hasNullAge() {
        Person p = new Person();
        p.setGeburtsdatum(null);

        PersonListDTO dto = mapper.toListDTO(p);

        assertThat(dto.getAlter()).isNull();
    }

    @Test
    void personListDto_birthdayToday_ageIsZero() {
        Person p = new Person();
        p.setGeburtsdatum(LocalDate.now());

        PersonListDTO dto = mapper.toListDTO(p);

        assertThat(dto.getAlter()).isZero();
    }
}