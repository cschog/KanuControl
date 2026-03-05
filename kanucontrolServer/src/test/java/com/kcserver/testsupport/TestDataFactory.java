package com.kcserver.testsupport;

import com.kcserver.entity.Person;
import com.kcserver.entity.Verein;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VereinRepository;
import com.kcserver.repository.VeranstaltungRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class TestDataFactory {

    private final VereinRepository vereinRepository;
    private final PersonRepository personRepository;
    private final VeranstaltungRepository veranstaltungRepository;

    public TestDataFactory(
            VereinRepository vereinRepository,
            PersonRepository personRepository,
            VeranstaltungRepository veranstaltungRepository) {

        this.vereinRepository = vereinRepository;
        this.personRepository = personRepository;
        this.veranstaltungRepository = veranstaltungRepository;
    }

    public Long createTestVeranstaltung() {

        Verein verein = new Verein();
        verein.setName("Testverein");
        verein.setAbk("TV");
        verein = vereinRepository.save(verein);

        Person leiter = new Person();
        leiter.setVorname("Max");
        leiter.setName("Mustermann");
        leiter.setSex(Sex.WEIBLICH);
        leiter.setAktiv(true);
        leiter = personRepository.save(leiter);

        Veranstaltung v = new Veranstaltung();
        v.setName("Test " + System.nanoTime());
        v.setTyp(VeranstaltungTyp.JEM);
        v.setVerein(verein);
        v.setLeiter(leiter);
        v.setBeginnDatum(LocalDate.now());
        v.setEndeDatum(LocalDate.now().plusDays(1));
        v.setBeginnZeit(LocalTime.NOON);
        v.setEndeZeit(LocalTime.MIDNIGHT);
        v.setAktiv(true);

        return veranstaltungRepository.save(v).getId();
    }
}