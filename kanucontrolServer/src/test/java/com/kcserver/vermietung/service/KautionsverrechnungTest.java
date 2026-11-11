package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class KautionsverrechnungTest {
 final BuchungRepository br=mock(BuchungRepository.class);
 final BuchungFinanzbewegungRepository mr=mock(BuchungFinanzbewegungRepository.class);
 final VerbrauchsabrechnungService va=mock(VerbrauchsabrechnungService.class);
 final BuchungFinanzenService service=new BuchungFinanzenService(br,mr,mock(BuchungVerbrauchskostenRepository.class),va);
 final Buchung b=AbrechnungVorlagenTest.buchung();
 final List<BuchungFinanzbewegung> history=new ArrayList<>();
 final BuchungVerbrauchskosten cost=new BuchungVerbrauchskosten();
 @BeforeEach void setup(){
  b.setStatus(Buchungsstatus.ABRECHENBAR);b.setKautionBetrag(new BigDecimal("250.00"));
  when(br.findLockedById(1L)).thenReturn(Optional.of(b));when(mr.findByBuchungIdOrderByDatumAscIdAsc(1L)).thenReturn(history);
  when(mr.save(any())).thenAnswer(i->{BuchungFinanzbewegung m=i.getArgument(0);m.setId((long)history.size()+1);history.add(m);return m;});
  var deposit=new BuchungFinanzbewegung();deposit.setKategorie(Finanzkategorie.KAUTION);deposit.setArt(Finanzbewegungsart.EINGANG);deposit.setBetrag(new BigDecimal("250.00"));history.add(deposit);
  cost.setKosten(new BigDecimal("617.29"));when(va.berechneSnapshots(b,List.of())).thenReturn(List.of(cost));
 }
 KautionsverrechnungDTO request(String amount){return new KautionsverrechnungDTO(new BigDecimal(amount),LocalDate.of(2026,11,11),List.of());}
 @Test void verrechnungErzeugtZweiBewegungenUndReduziertRechnungssaldo(){
  var result=service.verrechneKaution(1L,request("250.00"));assertEquals(3,history.size());
  assertEquals(Finanzkategorie.KAUTION,history.get(1).getKategorie());assertEquals(Finanzkategorie.VERBRAUCHSKOSTEN,history.get(2).getKategorie());
  assertEquals(Finanzbewegungsart.VERRECHNUNG,history.get(1).getArt());assertEquals(new BigDecimal("367.29"),result.saldo());
  assertEquals(Buchungsstatus.ABRECHENBAR,b.getStatus());verify(mr).flush();
 }
 @Test void wiederholungKannKeineKautionDoppeltVerbrauchen(){
  service.verrechneKaution(1L,request("250.00"));assertThrows(IllegalArgumentException.class,()->service.verrechneKaution(1L,request("250.00")));assertEquals(3,history.size());
 }
 @Test void offenerVerbrauchBegrenztVerrechnungUndRestBleibtKaution(){
  cost.setKosten(new BigDecimal("100.00"));
  assertThrows(IllegalArgumentException.class,()->service.verrechneKaution(1L,request("250.00")));verify(mr,never()).save(any());
  var f=service.verrechneKaution(1L,request("100.00"));assertEquals(new BigDecimal("-150.00"),f.saldo());
 }
 @Test void bestaetigungWirdVorFinanzbuchungValidiert(){
  when(va.berechneSnapshots(b,List.of())).thenThrow(new IllegalArgumentException("Ablesungen bestätigen"));
  assertThrows(IllegalArgumentException.class,()->service.verrechneKaution(1L,request("250.00")));verify(mr,never()).save(any());
 }
 @Test void bereitsBezahlterVerbrauchWirdNichtErneutVerrechnet(){
  var paid=new BuchungFinanzbewegung();paid.setKategorie(Finanzkategorie.VERBRAUCHSKOSTEN);paid.setArt(Finanzbewegungsart.EINGANG);paid.setBetrag(new BigDecimal("600.00"));history.add(paid);
  assertThrows(IllegalArgumentException.class,()->service.verrechneKaution(1L,request("250.00")));verify(mr,never()).save(any());
  service.verrechneKaution(1L,request("17.29"));assertEquals(new BigDecimal("17.29"),history.getLast().getBetrag());
 }
 @Test void stornoKannNichtUeberAbschlussVerrechnetWerden(){
  b.setStatus(Buchungsstatus.STORNIERT);assertThrows(IllegalArgumentException.class,()->service.verrechneKaution(1L,request("250.00")));verifyNoInteractions(va);verify(mr,never()).save(any());
 }
}
