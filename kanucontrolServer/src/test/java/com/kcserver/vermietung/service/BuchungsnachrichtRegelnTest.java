package com.kcserver.vermietung.service;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.enumtype.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BuchungsnachrichtRegelnTest {
 static BigDecimal n(String s){return new BigDecimal(s);}
 static FinanzbewegungDTO m(Finanzkategorie k,Finanzbewegungsart a,String v){return new FinanzbewegungDTO(1L,k,a,n(v),LocalDate.of(2026,1,1),"");}
 @ParameterizedTest @ValueSource(ints={55,56,57}) void grenzeIstExaktUndInterneVerrechnungIstKeinGeldeingang(int tage){
  var heute=LocalDate.of(2026,11,11);var result=Stornoberechnung.berechnen(heute,heute.plusDays(tage),List.of(m(Finanzkategorie.ANZAHLUNG,Finanzbewegungsart.EINGANG,"50"),m(Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"950"),m(Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"250"),m(Finanzkategorie.MIETE,Finanzbewegungsart.RUECKZAHLUNG,"100"),m(Finanzkategorie.KAUTION,Finanzbewegungsart.VERRECHNUNG,"20"),m(Finanzkategorie.VERBRAUCHSKOSTEN,Finanzbewegungsart.VERRECHNUNG,"20")));
  assertEquals(tage>=56,result.volleErstattung());assertEquals(n("1250.00"),result.gezahlt());assertEquals(n("100.00"),result.bereitsErstattet());assertEquals(tage>=56?n("1150.00"):n("0.00"),result.erstattung());assertEquals(tage<56?n("1150.00"):n("0.00"),result.einbehalt());
 }
 @Test void keineZahlungBedeutetKeineErstattung(){var d=LocalDate.of(2026,11,11);assertEquals(0,Stornoberechnung.berechnen(d,d.plusDays(90),List.of()).erstattung().signum());}
 @Test void anzahlungGenuegtOhneSpaeterFaelligeKautionOderRestmiete(){var b=AbrechnungVorlagenTest.buchung();b.setStatus(Buchungsstatus.RESERVIERT);b.setZahlungsvariante(Zahlungsvariante.ANZAHLUNG);b.setAnzahlungBetrag(n("50"));b.setKautionBetrag(n("250"));assertDoesNotThrow(()->BuchungsnachrichtDaten.zahlungsplanPruefen(b,Buchungsstatus.BESTAETIGT,List.of(m(Finanzkategorie.ANZAHLUNG,Finanzbewegungsart.EINGANG,"50"))));assertThrows(Exception.class,()->BuchungsnachrichtDaten.zahlungsplanPruefen(b,Buchungsstatus.BESTAETIGT,List.of()));assertTrue(BuchungsnachrichtDaten.ohneMail(b.getStatus(),Buchungsstatus.BESTAETIGT));}
 @Test void direkteBestaetigungVerlangtZahlungBeiAbrechnung(){var b=AbrechnungVorlagenTest.buchung();b.setStatus(Buchungsstatus.ANFRAGE);b.setZahlungsvariante(Zahlungsvariante.ANZAHLUNG);assertThrows(Exception.class,()->BuchungsnachrichtDaten.zahlungsplanPruefen(b,Buchungsstatus.BESTAETIGT,List.of()));b.setZahlungsvariante(Zahlungsvariante.BEI_ABRECHNUNG);assertDoesNotThrow(()->BuchungsnachrichtDaten.zahlungsplanPruefen(b,Buchungsstatus.BESTAETIGT,List.of()));assertFalse(BuchungsnachrichtDaten.ohneMail(b.getStatus(),Buchungsstatus.BESTAETIGT));}
 @Test void einbehaltUndTatsaechlicheRueckzahlungBleibenImStornosaldoGetrennt(){var f=AbrechnungVorlagenTest.finanzen(n("1000"));var cancelled=new BuchungFinanzenDTO(f.buchungId(),f.buchungsnummer(),f.mieter(),f.mietobjekt(),f.buchungsquelle(),"STORNIERT",f.anreise(),f.abreise(),false,f.anzahlungSoll(),f.kautionSoll(),f.mietkosten(),f.mietrabatt(),f.mieteSoll(),f.verbrauchSoll(),true,f.bewegungen(),f.entschaedigungen(),f.entschaedigungSoll(),n("1000"));assertEquals(n("0.00"),cancelled.saldo());}
}
