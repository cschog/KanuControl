package com.kcserver.mapper;

import com.kcserver.dto.person.PersonListDTO;
import com.kcserver.entity.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(PersonMapperImpl.class)
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

        assertThat(dto).isNotNull();

        assertThat(PersonListDTO.class.getDeclaredFields())
                .extracting("name")
                .doesNotContain("email");
    }

    @Test
    void personListDto_mapsName() {

        Person p = new Person();
        p.setName("Mustermann");

        PersonListDTO dto = mapper.toListDTO(p);

        assertThat(dto.getName()).isEqualTo("Mustermann");
    }
}