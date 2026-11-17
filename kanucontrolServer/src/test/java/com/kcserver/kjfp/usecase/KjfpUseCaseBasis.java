package com.kcserver.kjfp.usecase;

import com.kcserver.integration.AbstractFinanzIntegrationTest;
import com.kcserver.core.entity.*;
import com.kcserver.kjfp.entity.FinanzGruppe;
import com.kcserver.kjfp.dto.zahlungsnachweis.*;
import com.kcserver.kjfp.enumtype.Zahlungsweg;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

abstract class KjfpUseCaseBasis extends AbstractFinanzIntegrationTest {
    @Override protected String tenant(){return "kjfp_usecase_test";}
    static final LocalDate START=LocalDate.of(2027,7,1);
    @Autowired EntityManager em;
    @Autowired org.springframework.transaction.PlatformTransactionManager cleanupTxManager;
    Veranstaltung event() {
        veranstaltungRepository.findAll().forEach(v->v.setAktiv(false));em.flush();
        var v=veranstaltungRepository.findById(createTestVeranstaltung()).orElseThrow();
        v.setBeginnDatum(START);v.setEndeDatum(START.plusDays(4));v.setKjfpAktiv(true);
        var regel=v.getBeitragsstruktur().getRegeln().getFirst();regel.setRolle(null);regel.setAlterBis(null);regel.setBeitrag(new BigDecimal("100"));
        return v;
    }
    @Override protected void addSimulationVoraussetzungen(Veranstaltung v) {
        String suffix=UUID.randomUUID().toString();
        var unterkunft=new com.kcserver.kjfp.entity.Unterkunftsart();unterkunft.setBezeichnung("KJFP UseCase "+suffix);unterkunft.setPreisProPersonUndNacht(new BigDecimal("20"));unterkunft.setAktiv(true);v.setUnterkunftsart(unterkunftsartRepository.save(unterkunft));
        var verpflegung=new com.kcserver.kjfp.entity.Verpflegungsmodell();verpflegung.setBezeichnung("KJFP UseCase "+suffix);verpflegung.setPreisProPersonUndTag(new BigDecimal("12"));verpflegung.setAktiv(true);v.setVerpflegungsmodell(verpflegungsmodellRepository.save(verpflegung));
        var struktur=new com.kcserver.kjfp.entity.beitraege.Beitragsstruktur();struktur.setName("KJFP UseCase "+suffix);struktur.setAktiv(true);
        var regel=new com.kcserver.kjfp.entity.beitraege.Beitragsregel();regel.setSortierung(1);regel.setBeitrag(new BigDecimal("100"));regel.setStruktur(struktur);struktur.setRegeln(new ArrayList<>(List.of(regel)));v.setBeitragsstruktur(beitragsstrukturRepository.save(struktur));veranstaltungRepository.save(v);
    }
    Teilnehmer person(Veranstaltung v,FinanzGruppe konto) {
        var t=createTeilnehmer(v,BigDecimal.ZERO);t.getPerson().setGeburtsdatum(LocalDate.of(1990,1,1));
        if(konto!=null)konto.addTeilnehmer(t);return t;
    }
    ZahlungsnachweisUpdateDTO zahlung(String betrag,Zahlungsweg weg,FinanzGruppe konto,Teilnehmer... teilnehmer) {
        var dto=new ZahlungsnachweisUpdateDTO();dto.setBetrag(new BigDecimal(betrag));dto.setDatum(START);dto.setZahlungsweg(weg);dto.setFinanzGruppeId(konto==null?null:konto.getId());
        dto.setPositionen(Arrays.stream(teilnehmer).map(t->{var p=new ZahlungsPositionDTO();p.setTeilnehmerId(t.getId());p.setBetrag(BigDecimal.ZERO);return p;}).toList());return dto;
    }
    void neuLaden(){em.flush();em.clear();}
    @org.junit.jupiter.api.AfterEach void committedFixturesBereinigen() {
        if (org.springframework.test.context.transaction.TestTransaction.isActive()) return;
        com.kcserver.core.tenancy.TenantContext.setCurrentTenant(tenant());
        new org.springframework.transaction.support.TransactionTemplate(cleanupTxManager).executeWithoutResult(status -> {
            jdbcTemplate.execute("TRUNCATE TABLE "+tenant()+".veranstaltung, "+tenant()+".person, "+tenant()+".verein, "+tenant()+".planung, "+tenant()+".finanz_gruppe RESTART IDENTITY CASCADE");
            jdbcTemplate.update("DELETE FROM "+tenant()+".beitragsregel WHERE beitragsstruktur_id IN (SELECT id FROM "+tenant()+".beitragsstruktur WHERE name LIKE 'KJFP UseCase %')");
            jdbcTemplate.update("DELETE FROM "+tenant()+".beitragsstruktur WHERE name LIKE 'KJFP UseCase %'");
            jdbcTemplate.update("DELETE FROM "+tenant()+".unterkunftsart WHERE bezeichnung LIKE 'KJFP UseCase %'");
            jdbcTemplate.update("DELETE FROM "+tenant()+".verpflegungsmodell WHERE bezeichnung LIKE 'KJFP UseCase %'");
        });
    }

}
