package com.kcserver.testdata;

import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;

import java.time.LocalDate;
import java.time.LocalTime;

public class DomainTestFactory {

    public static Veranstaltung validVeranstaltung(
            Verein verein,
            Person leiter
    ) {
        Veranstaltung v = new Veranstaltung();
        v.setName("Test Veranstaltung");
        v.setTyp(VeranstaltungTyp.JEM);

        v.setBeginnDatum(LocalDate.now());
        v.setBeginnZeit(LocalTime.of(10, 0));
        v.setEndeDatum(LocalDate.now().plusDays(5));
        v.setEndeZeit(LocalTime.of(18, 0));

        v.setAktiv(true);

        v.setVerein(verein);     // ⭐ WICHTIG
        v.setLeiter(leiter);     // ⭐ WICHTIG

        return v;
    }

    private static long counter = 0;

    public static Person validPerson() {
        Person p = new Person();

        long n = System.nanoTime();   // garantiert unique, auch parallel

        p.setVorname("Max" + n);
        p.setName("Mustermann" + n);

        // Geburtsdatum muss auch unique sein wegen Unique-Constraint
        int day = (int) (Math.abs(n) % 28) + 1;
        p.setGeburtsdatum(LocalDate.of(1990, 1, day));

        p.setSex(Sex.MAENNLICH);
        p.setAktiv(true);

        return p;
    }

    public static Teilnehmer normalTeilnehmer(Long veranstaltungId, Long personId) {
        Teilnehmer t = new Teilnehmer();
        Veranstaltung v = new Veranstaltung();
        v.setId(veranstaltungId);
        t.setVeranstaltung(v);

        Person p = new Person();
        p.setId(personId);
        t.setPerson(p);

        t.setRolle(null);
        return t;
    }

    public static Verein validVerein() {
        Verein v = new Verein();
        v.setName("Test Verein");
        v.setAbk("TV");
        return v;
    }
}