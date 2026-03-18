package com.kcserver.testsupport;

import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.VereinRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class TestDataFactory {

    private final VereinRepository vereinRepository;
    private final PersonRepository personRepository;
    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;

    public TestDataFactory(
            VereinRepository vereinRepository,
            PersonRepository personRepository,
            VeranstaltungRepository veranstaltungRepository,
    TeilnehmerRepository teilnehmerRepository) {

        this.vereinRepository = vereinRepository;
        this.personRepository = personRepository;
        this.veranstaltungRepository = veranstaltungRepository;
        this.teilnehmerRepository = teilnehmerRepository;
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
    public Long createTeilnehmer(Long veranstaltungId) {

        Veranstaltung veranstaltung = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow();

        Person person = new Person();
        person.setVorname("Max");
        person.setName("Mustermann");
        person.setSex(Sex.MAENNLICH);
        person.setGeburtsdatum(LocalDate.of(2000, 1, 1));

        person = personRepository.save(person);

        Teilnehmer teilnehmer = new Teilnehmer();
        teilnehmer.setVeranstaltung(veranstaltung);
        teilnehmer.setPerson(person);

        teilnehmer = teilnehmerRepository.save(teilnehmer);

        return teilnehmer.getId();
    }
}