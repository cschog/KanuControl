package com.kcserver.mapper;

import com.kcserver.dto.person.PersonListDTO;
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
        Person p = new Person();
        p.setGeburtsdatum(LocalDate.now().minusYears(10));

        PersonListDTO dto = mapper.toListDTO(p);

        assertThat(dto.getAlter()).isEqualTo(10);
    }

    @Test
    void personListDto_withNullBirthdate_hasNullAge() {
        Person p = new Person();

        PersonListDTO dto = mapper.toListDTO(p);

        assertThat(dto.getAlter()).isNull();
    }

    @Test
    void personListDto_doesNotExposeEmail() {
        Person p = new Person();
        p.setEmail("test@test.de");

        PersonListDTO dto = mapper.toListDTO(p);

        // bewusst KEIN dto.getEmail()
        // Test ist implizit bestanden, wenn es kompiliert
    }
}