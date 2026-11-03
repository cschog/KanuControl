package com.kcserver.unit;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.service.*;
import com.kcserver.vermietung.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungPreiskalkulationTest {
    private Buchung gruppe() {
        Buchung b = new Buchung(); b.setId(7L); b.setBuchungsquelle(Buchungsquelle.DIREKT);
        b.setAnreise(LocalDateTime.of(2027,6,10,16,0)); b.setAbreise(LocalDateTime.of(2027,6,12,14,0));
        b.setRabattart(Rabattart.VEREIN);
        BuchungMietbereich p = new BuchungMietbereich(); p.setBuchung(b); p.setAnzahl(1);
        p.setBerechnungsart(Berechnungsart.TAG); p.setVereinbarterPreis(new BigDecimal("300.00"));
        p.setRabattwertTyp(RabattwertTyp.PROZENTUAL); p.setRabattwert(new BigDecimal("50"));
        Mietbereich bereich = new Mietbereich(); bereich.setBezeichnung("Bootshaus"); p.setMietbereich(bereich);
        b.getMietbereiche().add(p); return b;
    }
    private void geld(String expected, BigDecimal actual) { assertEquals(0, new BigDecimal(expected).compareTo(actual)); }
    @Test void zweiTageMitVereinsrabattSindNichtHundertProzentRabatt() {
        Buchung b = gruppe(); var p = b.getMietbereiche().getFirst();
        assertEquals(2, BuchungPreiskalkulation.einheiten(p)); geld("600", BuchungPreiskalkulation.preis(p));
        geld("300", BuchungPreiskalkulation.rabatt(b));
        geld("300", BuchungPreiskalkulation.preis(p).subtract(BuchungPreiskalkulation.rabatt(b)));
    }
    @Test void tageUndMengenWerdenNurEinmalMultipliziert() {
        Buchung b = gruppe(); var p=b.getMietbereiche().getFirst(); p.setAnzahl(2);
        geld("1200", BuchungPreiskalkulation.preis(p)); geld("600", BuchungPreiskalkulation.rabatt(b));
    }
    @Test void pauschaleBleibtBeiMehrerenTagenEinmalig() {
        Buchung b = gruppe(); var p=b.getMietbereiche().getFirst(); p.setBerechnungsart(Berechnungsart.BUCHUNG);
        geld("300", BuchungPreiskalkulation.preis(p)); geld("150", BuchungPreiskalkulation.rabatt(b));
    }
    @Test void festerPositionsrabattGiltProEinheitUndTag() {
        Buchung b = gruppe(); var p=b.getMietbereiche().getFirst(); p.setRabattwertTyp(RabattwertTyp.ABSOLUT); p.setRabattwert(new BigDecimal("25"));
        geld("50", BuchungPreiskalkulation.rabatt(b));
    }
    @Test void individuellerGesamtrabattGiltEinmalig() {
        Buchung b = gruppe(); b.setRabattart(Rabattart.INDIVIDUELL); b.setRabattwertTyp(RabattwertTyp.ABSOLUT); b.setRabattwert(new BigDecimal("25"));
        geld("25", BuchungPreiskalkulation.rabatt(b));
    }
    @Test void sommerzeitUndJahreswechselVerwendenKalendertage() {
        Buchung b=gruppe(); b.setAnreise(LocalDateTime.of(2027,3,27,16,0));b.setAbreise(LocalDateTime.of(2027,3,29,14,0));
        assertEquals(2,BuchungPreiskalkulation.einheiten(b.getMietbereiche().getFirst()));
        b.setAnreise(LocalDateTime.of(2027,12,31,16,0));b.setAbreise(LocalDateTime.of(2028,1,2,14,0));
        assertEquals(2,BuchungPreiskalkulation.einheiten(b.getMietbereiche().getFirst()));
    }
    @Test void airbnbUndKeinRabattErhaltenKeinenAbzug() {
        Buchung b=gruppe(); b.setBuchungsquelle(Buchungsquelle.AIRBNB);geld("0",BuchungPreiskalkulation.rabatt(b));
        b.setBuchungsquelle(Buchungsquelle.DIREKT);b.setRabattart(Rabattart.KEIN_RABATT);geld("0",BuchungPreiskalkulation.rabatt(b));
    }
    @Test void alteGesamtsummeWirdAusVorhandenemProzentsnapshotKorrektAngezeigt() {
        Buchung b=gruppe(); b.setRabattbetrag(new BigDecimal("150"));geld("300",BuchungPreiskalkulation.rabatt(b));
        geld("150",b.getRabattbetrag()); // Lesen schreibt die alte Buchung nicht um.
    }
    @Test void finanzuebersichtVerwendetDieselbenTageUndRabatte() {
        Buchung b=gruppe(); com.kcserver.core.entity.Person person=new com.kcserver.core.entity.Person();person.setVorname("Test");person.setName("Gast");b.setMieter(person);
        Mietobjekt objekt=new Mietobjekt();objekt.setBezeichnung("Bootshaus");b.setMietobjekt(objekt);
        BuchungRepository buchungen=mock(BuchungRepository.class);when(buchungen.findAll()).thenReturn(List.of(b));
        var service=new BuchungFinanzenService(buchungen,mock(BuchungFinanzbewegungRepository.class),mock(BuchungVerbrauchskostenRepository.class),mock(VerbrauchsabrechnungService.class));
        var dto=service.getAll().getFirst();geld("600",dto.mietkosten().getFirst().gesamtbetrag());
        assertEquals(2,dto.mietkosten().getFirst().abrechnungseinheiten());geld("300",dto.mietrabatt());geld("300",dto.mieteSoll());
    }
}
