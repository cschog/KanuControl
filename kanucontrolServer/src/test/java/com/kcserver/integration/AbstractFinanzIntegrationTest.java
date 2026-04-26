package com.kcserver.integration;

import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.finanz.AbrechnungBelegService;
import com.kcserver.finanz.AbrechnungService;
import com.kcserver.finanz.PlanungPositionService;
import com.kcserver.finanz.PlanungService;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.repository.*;
import com.kcserver.service.VeranstaltungService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public abstract class AbstractFinanzIntegrationTest
        extends AbstractTenantIntegrationTest {

    @Autowired protected VereinRepository vereinRepository;
    @Autowired protected PersonRepository personRepository;
    @Autowired protected VeranstaltungRepository veranstaltungRepository;
    @Autowired protected TeilnehmerRepository teilnehmerRepository;
    @Autowired protected AbrechnungRepository abrechnungRepository;

    @Autowired protected PlanungService planungService;
    @Autowired protected PlanungPositionService planungPositionService;

    @Autowired
    VeranstaltungService veranstaltungService;

    @Autowired protected AbrechnungService abrechnungService;
    @Autowired protected AbrechnungBelegService abrechnungBelegService;

    private static int counter = 1;

    protected Long createTestVeranstaltung() {

        String suffix = String.valueOf(counter++); // 1,2,3,...

        Verein verein = new Verein();
        verein.setName("Testverein_" + suffix);
        verein.setAbk("TV" + suffix); // <= 10 Zeichen!
        verein = vereinRepository.save(verein);

        Person leiter = new Person();
        leiter.setVorname("Max");
        leiter.setName("Mustermann");
        leiter.setSex(Sex.WEIBLICH);
        leiter.setAktiv(true);
        leiter = personRepository.save(leiter);

        Veranstaltung v = new Veranstaltung();
        v.setName("Finanztest");
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

    protected Long createTestVeranstaltung(
            VeranstaltungTyp typ,
            LocalDate start
    ) {

        String suffix = String.valueOf(counter++);  // 1,2,3,...

        Verein verein = new Verein();
        verein.setName("TV" + suffix);          // kurz & gültig
        verein.setAbk("TV" + suffix);           // max 10 Zeichen garantiert
        verein = vereinRepository.save(verein);

        Person leiter = new Person();
        leiter.setVorname("Max" + suffix);           // 🔥 eindeutig
        leiter.setName("Mustermann" + suffix);
        leiter.setSex(Sex.WEIBLICH);
        leiter.setGeburtsdatum(LocalDate.now().minusYears(30)); // wichtig!
        leiter.setAktiv(true);
        leiter = personRepository.save(leiter);

        VeranstaltungCreateDTO dto = new VeranstaltungCreateDTO();
        dto.setName("Finanztest");
        dto.setTyp(typ);
        dto.setBeginnDatum(start);
        dto.setEndeDatum(start.plusDays(1));
        dto.setBeginnZeit(LocalTime.NOON);
        dto.setEndeZeit(LocalTime.MIDNIGHT);
        dto.setVereinId(verein.getId());
        dto.setLeiterId(leiter.getId());

        return veranstaltungService.create(dto).getId();
    }

    protected Long createTestVeranstaltung(
            VeranstaltungTyp typ,
            LocalDate beginn,
            LocalDate ende
    ) {

        Veranstaltung v = new Veranstaltung();

        v.setTyp(typ);

        v.setBeginnDatum(beginn);
        v.setEndeDatum(ende);
        v.setBeginnZeit(LocalTime.of(8, 0));
        v.setEndeZeit(LocalTime.of(18, 0));

        Person leiter = new Person();
        leiter.setVorname("Test");
        leiter.setName("Leiter");
        leiter.setSex(Sex.MAENNLICH);
        leiter.setAktiv(true);

        leiter = personRepository.save(leiter);

        v.setLeiter(leiter);

        v.setName("Testveranstaltung");

        v.setVerein(vereinRepository.findAll().getFirst());

        v = veranstaltungRepository.save(v);

        return v.getId();
    }

    protected Abrechnung createOpenAbrechnung(Long veranstaltungId) {

        abrechnungService.getOrCreate(veranstaltungId);

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow();

        a.setStatus(AbrechnungsStatus.OFFEN);

        return abrechnungRepository.saveAndFlush(a);
    }
    protected Teilnehmer createTeilnehmer(
            Veranstaltung v,
            BigDecimal beitrag
    ) {

        Person p = new Person();
        p.setVorname("Teilnehmer");
        p.setName("Nr" + System.nanoTime());
        p.setSex(Sex.WEIBLICH);
        p.setAktiv(true);

        p = personRepository.save(p);

        Teilnehmer t = new Teilnehmer();
        t.setVeranstaltung(v);
        t.setPerson(p);
        t.setIndividuellerBeitrag(beitrag);

        return teilnehmerRepository.save(t);
    }
}