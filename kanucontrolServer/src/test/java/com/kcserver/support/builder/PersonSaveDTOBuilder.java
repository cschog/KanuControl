package com.kcserver.support.builder;

import com.kcserver.dto.person.PersonCreateDTO;
import com.kcserver.entity.Person;

import java.time.LocalDate;

public class PersonDTOBuilder {

    private final PersonBuilder base = PersonBuilder.aPerson();

    public static PersonDTOBuilder aPersonDTO() {
        return new PersonDTOBuilder();
    }

    public PersonDTOBuilder female() {
        base.female();
        return this;
    }

    public PersonCreateDTO build() {
        Person p = base.build();

        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setVorname(p.getVorname());
        dto.setName(p.getName());
        dto.setSex(p.getSex());
        dto.setGeburtsdatum(p.getGeburtsdatum());
        dto.setAktiv(true);

        return dto;
    }
    public PersonDTOBuilder withVorname(String vorname) {
        base.withVorname(vorname);
        return this;
    }

    public PersonDTOBuilder withName(String name) {
        base.withName(name);
        return this;
    }

    public PersonDTOBuilder withGeburtsdatum(LocalDate geburtsdatum) {
        base.withGeburtsdatum(geburtsdatum);
        return this;
    }
}
