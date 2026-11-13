package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.math.BigDecimal;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OhneAbrechnungTest {
 private Buchung beispiel() {
  var b=AbrechnungVorlagenTest.buchung(); b.setZahlungsvariante(Zahlungsvariante.OHNE_ABRECHNUNG);
  var p=new BuchungMietbereich();p.setBuchung(b);p.setAnzahl(2);p.setVereinbarterPreis(new BigDecimal("100"));p.setBerechnungsart(Berechnungsart.TAG);
  var m=new Mietbereich();m.setBezeichnung("Bootshaus");p.setMietbereich(m);b.getMietbereiche().add(p);
  var e=new BuchungEntschaedigung();e.setBuchung(b);e.setEinzelpreis(new BigDecimal("50"));e.setAnzahl(3);b.getEntschaedigungen().add(e);
  b.setKautionBetrag(new BigDecimal("200"));b.setAnzahlungBetrag(new BigDecimal("100"));return b;
 }
 @Test void mietUndZusatzkostenSindNullUndPreisSnapshotsBleibenErhalten() {
  var b=beispiel();assertEquals(0,BuchungPreiskalkulation.miete(b).signum());assertEquals(0,EntschaedigungPositionService.sum(b).signum());assertTrue(EntschaedigungPositionService.rows(b).isEmpty());
  assertEquals(0,Zahlungsplan.anzahlung(b).signum());assertEquals(0,Zahlungsplan.restmiete(b).signum());
  b.setZahlungsvariante(Zahlungsvariante.BEI_ABRECHNUNG);
  assertEquals(0,new BigDecimal("400").compareTo(BuchungPreiskalkulation.miete(b)));assertEquals(0,new BigDecimal("150").compareTo(EntschaedigungPositionService.sum(b)));
 }
 @Test void verbrauchOhneAblesungOderPreiszugriffIstLeer() {
  var costs=mock(VerbrauchskostenRepository.class);var meters=mock(ZaehlerRepository.class);var readings=mock(ZaehlerstandRepository.class);var snapshots=mock(BuchungVerbrauchskostenRepository.class);
  var service=new VerbrauchsabrechnungService(costs,meters,readings,snapshots);var b=beispiel();
  assertTrue(service.berechneSnapshots(b,List.of()).isEmpty());assertTrue(service.berechneVorschau(b).isEmpty());
  verifyNoInteractions(costs,meters,readings,snapshots);
 }
 @Test void finanzanzeigeIgnoriertAlteVerbrauchskostenUndKaution() {
  var snapshots=mock(BuchungVerbrauchskostenRepository.class);var b=beispiel();
  var old=new BuchungVerbrauchskosten();old.setBuchung(b);old.setKosten(new BigDecimal("99"));when(snapshots.findByBuchungIdOrderByIdAsc(b.getId())).thenReturn(List.of(old));
  var service=new BuchungFinanzenService(mock(BuchungRepository.class),mock(BuchungFinanzbewegungRepository.class),snapshots,mock(VerbrauchsabrechnungService.class));
  var f=service.toDto(b);assertEquals(0,f.mieteSoll().signum());assertEquals(0,f.verbrauchSoll().signum());assertEquals(0,f.kautionSoll().signum());assertEquals(0,f.abrechnungGesamt().signum());
  var dashboard=FinanzDashboardService.calculate(2025,LocalDate.of(2025,11,1),new BigDecimal("25000"),List.of(),List.of(b),List.of(old));
  assertEquals(0,dashboard.umsatz().signum());assertEquals(0,dashboard.geplantOffen().signum());
 }
 @Test void kostenloseAnfrageKannOhneVorauszahlungDirektBestaetigtWerden() {
  var b=beispiel();b.setStatus(Buchungsstatus.ANFRAGE);
  assertDoesNotThrow(()->BuchungsnachrichtDaten.zahlungsplanPruefen(b,Buchungsstatus.BESTAETIGT,List.of()));
 }
 @Test void rechnungsausstellungIstGesperrt() {
  var b=beispiel();var bookings=mock(BuchungRepository.class);when(bookings.findLockedById(b.getId())).thenReturn(Optional.of(b));
  var docs=mock(VermietungAbrechnungRepository.class);var renderer=mock(AbrechnungVorlagenService.class);
  var service=new VermietungAbrechnungService(bookings,docs,renderer);
  assertThrows(IllegalArgumentException.class,()->service.create(b.getId(),new VermietungAbrechnungService.Ausstellung(null,null)));
  verifyNoInteractions(docs,renderer);
 }
}
