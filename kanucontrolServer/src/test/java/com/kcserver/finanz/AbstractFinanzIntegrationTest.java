package com.kcserver.finanz;

import com.kcserver.entity.*;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.*;
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

    @Autowired protected AbrechnungService abrechnungService;
    @Autowired protected AbrechnungBuchungService buchungService;

    protected Long createTestVeranstaltung() {

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