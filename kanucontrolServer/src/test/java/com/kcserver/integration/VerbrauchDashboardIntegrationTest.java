package com.kcserver.integration;

import com.kcserver.core.entity.Person;
import com.kcserver.kjfp.enumtype.Sex;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.service.VerbrauchDashboardService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
class VerbrauchDashboardIntegrationTest extends AbstractTenantIntegrationTest {
    @Autowired EntityManager em;
    @Autowired VerbrauchDashboardService service;
    @Test void echteZuordnungenUndBuchungsstatusWerdenObjektuebergreifendAusgewertet() {
        int year=LocalDate.now(ZoneId.of("Europe/Berlin")).getYear()-1;
        var a=new Mietobjekt();a.setBezeichnung("Haus A");a.setAktiv(false);em.persist(a);
        var b=new Mietobjekt();b.setBezeichnung("Haus B");b.setAktiv(false);em.persist(b);
        var meter=new Zaehler();meter.setBezeichnung("Gemeinsames Gas");meter.setMengeneinheit("m³");meter.setVerbrauchsart(Verbrauchsart.GAS);em.persist(meter);
        var areaA=new Mietbereich();areaA.setMietobjekt(a);areaA.setBezeichnung("EG");areaA.setZaehler(new HashSet<>(Set.of(meter)));em.persist(areaA);
        var areaB=new Mietbereich();areaB.setMietobjekt(b);areaB.setBezeichnung("EG");areaB.setZaehler(new HashSet<>(Set.of(meter)));em.persist(areaB);
        var person=new Person();person.setName("Muster");person.setVorname("Test");person.setSex(Sex.DIVERS);em.persist(person);
        int number=1;
        for(var status:List.of(Buchungsstatus.BESTAETIGT,Buchungsstatus.ANFRAGE,Buchungsstatus.STORNIERT)) {
            var booking=new Buchung();booking.setBuchungsjahr(year);booking.setLaufendeNummer(number++);booking.setMietobjekt(status==Buchungsstatus.BESTAETIGT?b:a);
            booking.setMieter(person);booking.setBuchungsquelle(Buchungsquelle.DIREKT);booking.setStatus(status);
            booking.setAnreise(LocalDateTime.of(year,1,status==Buchungsstatus.BESTAETIGT?1:2,0,0));
            booking.setAbreise(LocalDateTime.of(year,1,status==Buchungsstatus.BESTAETIGT?2:3,0,0));em.persist(booking);
            var position=new BuchungMietbereich();position.setBuchung(booking);position.setMietbereich(status==Buchungsstatus.BESTAETIGT?areaB:areaA);
            position.setBerechnungsart(Berechnungsart.BUCHUNG);position.setVereinbarterPreis(BigDecimal.ONE);em.persist(position);
        }
        for(int i=0;i<2;i++) {var reading=new Zaehlerstand();reading.setZaehler(meter);reading.setAblesedatum(LocalDateTime.of(year,1,i==0?1:3,0,0));reading.setZaehlerstand(BigDecimal.valueOf(i*20));em.persist(reading);}
        em.flush();em.clear();
        var result=service.get(a.getId(),year);assertEquals(1,result.zaehler().size());
        var data=result.zaehler().getFirst();assertTrue(data.gemeinsam());
        assertEquals(20,data.kennzahlen().gesamt().doubleValue(),0.00001);
        assertEquals(10,data.kennzahlen().mitBuchung().doubleValue(),0.00001);
        assertEquals(10,data.kennzahlen().ohneBuchung().doubleValue(),0.00001);
        assertEquals(1,data.buchungen().size());assertEquals("BESTAETIGT",data.buchungen().getFirst().status());
    }
}
