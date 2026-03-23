package com.kcserver.support.builder;

import com.kcserver.entity.Person;
import com.kcserver.enumtype.Sex;

import java.time.LocalDate;

public class PersonBuilder {

    private static long counter = 0;

    private String name;
    private String vorname;
    private Sex sex = Sex.MAENNLICH;
    private LocalDate geburtsdatum;

    public PersonBuilder() {
        counter++;

        this.name = "Mustermann" + counter;
        this.vorname = "Max" + counter;
        this.geburtsdatum = LocalDate.now().minusYears(30).minusDays(counter);
    }


    public static PersonBuilder aPerson() {
        return new PersonBuilder();
    }

    public PersonBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public PersonBuilder withVorname(String vorname) {
        this.vorname = vorname;
        return this;
    }

    public PersonBuilder withGeburtsdatum(LocalDate geburtsdatum) {
        this.geburtsdatum = geburtsdatum;
        return this;
    }

    public PersonBuilder female() {
        this.sex = Sex.WEIBLICH;
        return this;
    }

    public PersonBuilder male() {
        this.sex = Sex.MAENNLICH;
        return this;
    }

    public PersonBuilder under18() {
        this.geburtsdatum = LocalDate.now().minusYears(10);
        return this;
    }

    public Person build() {
        Person p = new Person();
        p.setName(name);
        p.setVorname(vorname);
        p.setSex(sex);
        p.setGeburtsdatum(geburtsdatum);
        return p;
    }
    public static void resetCounter() {
        counter = 0;
    }
}