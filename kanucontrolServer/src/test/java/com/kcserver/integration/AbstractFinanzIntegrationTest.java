package com.kcserver.integration;



import com.kcserver.kjfp.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.kjfp.entity.*;
import com.kcserver.kjfp.entity.abrechnung.Abrechnung;
import com.kcserver.kjfp.entity.beitraege.Beitragsregel;
import com.kcserver.kjfp.entity.beitraege.Beitragsstruktur;
import com.kcserver.kjfp.enumtype.AbrechnungsStatus;
import com.kcserver.kjfp.enumtype.Sex;
import com.kcserver.kjfp.enumtype.TeilnehmerRolle;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import com.kcserver.kjfp.repository.*;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungRepository;
import com.kcserver.kjfp.repository.beitrag.BeitragsstrukturRepository;
import com.kcserver.kjfp.service.abrechnung.AbrechnungBelegService;
import com.kcserver.kjfp.service.abrechnung.AbrechnungService;
import com.kcserver.kjfp.service.finanz.FinanzGruppeService;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.kjfp.service.veranstaltung.VeranstaltungService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public abstract class AbstractFinanzIntegrationTest
        extends AbstractTenantIntegrationTest {

    @Autowired protected VereinRepository vereinRepository;
    @Autowired protected PersonRepository personRepository;
    @Autowired protected VeranstaltungRepository veranstaltungRepository;
    @Autowired protected TeilnehmerRepository teilnehmerRepository;
    @Autowired protected AbrechnungRepository abrechnungRepository;
    @Autowired protected FinanzGruppeService finanzGruppeService;


    @Autowired protected UnterkunftsartRepository unterkunftsartRepository;
    @Autowired protected VerpflegungsmodellRepository verpflegungsmodellRepository;
    @Autowired protected BeitragsstrukturRepository beitragsstrukturRepository;

    @Autowired
    VeranstaltungService veranstaltungService;

    @Autowired protected AbrechnungService abrechnungService;
    @Autowired protected AbrechnungBelegService abrechnungBelegService;

    private static int counter = 1;

    protected Long createTestVeranstaltung() {

        String suffix = String.valueOf(counter++);

        Verein verein = new Verein();
        verein.setName("Testverein_" + suffix);
        verein.setAbk("TV" + suffix);
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

        v = veranstaltungRepository.save(v);

        // HIER

        addSimulationVoraussetzungen(v);

        // System-Finanzgruppe für das Vereinskonto
        finanzGruppeService.getOrCreateVereinsFinanzGruppe(v);

        return v.getId();
    }

    protected Long createTestVeranstaltung(
            VeranstaltungTyp typ,
            LocalDate start
    ) {

        String suffix = String.valueOf(counter++);

        Verein verein = new Verein();
        verein.setName("TV" + suffix);
        verein.setAbk("TV" + suffix);
        verein = vereinRepository.save(verein);

        Person leiter = new Person();
        leiter.setVorname("Max" + suffix);
        leiter.setName("Mustermann" + suffix);
        leiter.setSex(Sex.WEIBLICH);
        leiter.setGeburtsdatum(LocalDate.now().minusYears(30));
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

        return veranstaltungService.create(dto).data().getId();
    }

    protected void addSimulationVoraussetzungen(Veranstaltung veranstaltung) {
        Unterkunftsart unterkunftsart =
                unterkunftsartRepository.save(
                        Unterkunftsart.builder()
                                .bezeichnung("Test Unterkunft " + veranstaltung.getId())
                                .preisProPersonUndNacht(new BigDecimal("20"))
                                .aktiv(true)
                                .build()
                );

        Verpflegungsmodell verpflegungsmodell =
                verpflegungsmodellRepository.save(
                        Verpflegungsmodell.builder()
                                .bezeichnung("Test Verpflegung " + veranstaltung.getId())
                                .preisProPersonUndTag(new BigDecimal("12"))
                                .aktiv(true)
                                .build()
                );

        Beitragsstruktur beitragsstruktur = new Beitragsstruktur();
        beitragsstruktur.setName("Test Beitragsstruktur " + veranstaltung.getId());
        beitragsstruktur.setAktiv(true);
        beitragsstruktur.setTemplate(false);
        beitragsstruktur.setSystem(false);

        Beitragsregel regel = new Beitragsregel();
        regel.setSortierung(1);
        regel.setAlterBis(18);
        regel.setRolle(TeilnehmerRolle.MITARBEITER);
        regel.setBeitrag(new BigDecimal("100"));
        regel.setStruktur(beitragsstruktur);

        beitragsstruktur.setRegeln(List.of(regel));
        beitragsstruktur = beitragsstrukturRepository.save(beitragsstruktur);

        veranstaltung.setUnterkunftsart(unterkunftsart);
        veranstaltung.setVerpflegungsmodell(verpflegungsmodell);
        veranstaltung.setBeitragsstruktur(beitragsstruktur);

        veranstaltungRepository.save(veranstaltung);
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