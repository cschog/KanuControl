package com.kcserver.support.builder;
import com.kcserver.dto.person.PersonSaveDTO;
import com.kcserver.entity.Person;

import java.time.LocalDate;

public class PersonSaveDTOBuilder {

    private final PersonBuilder base = PersonBuilder.aPerson();

    public static PersonSaveDTOBuilder aDTO() {
        return new PersonSaveDTOBuilder();
    }

    public PersonSaveDTO build() {
        Person p = base.build();

        PersonSaveDTO dto = new PersonSaveDTO();
        dto.setVorname(p.getVorname());
        dto.setName(p.getName());
        dto.setSex(p.getSex());
        dto.setGeburtsdatum(p.getGeburtsdatum());
        dto.setAktiv(true);

        return dto;
    }
    public PersonSaveDTOBuilder withName(String name) {
        base.withName(name);
        return this;
    }

    public PersonSaveDTOBuilder withVorname(String vorname) {
        base.withVorname(vorname);
        return this;
    }

    public PersonSaveDTOBuilder withGeburtsdatum(LocalDate date) {
        base.withGeburtsdatum(date);
        return this;
    }

    public PersonSaveDTOBuilder male() {
        base.male();
        return this;
    }

    public PersonSaveDTOBuilder female() {
        base.female();
        return this;
    }

    public PersonSaveDTOBuilder under18() {
        base.under18();
        return this;
    }

    public PersonSaveDTOBuilder minimalValid() {
        return this;
    }
}