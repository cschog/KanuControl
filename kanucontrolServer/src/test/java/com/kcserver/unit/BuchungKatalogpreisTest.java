package com.kcserver.unit;
import com.kcserver.core.entity.Person;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BuchungKatalogpreisTest {
 final BuchungRepository bookings=mock(BuchungRepository.class);
 final MietpreisRepository prices=mock(MietpreisRepository.class);
 final MietbereichRepository areas=mock(MietbereichRepository.class);
 final Buchung b=new Buchung();final Mietobjekt obj=new Mietobjekt();final Mietbereich area=new Mietbereich();final BuchungMietbereich position=new BuchungMietbereich();
 BuchungService service;BuchungDTO dto;
 @BeforeEach void setup(){
  var persons=mock(com.kcserver.kjfp.repository.PersonRepository.class);var person=new Person();person.setId(1L);person.setKjfpSichtbar(true);when(persons.findLockedById(1L)).thenReturn(Optional.of(person));
  obj.setId(7L);obj.setAnreisezeit(LocalTime.of(16,0));obj.setAbreisezeit(LocalTime.of(14,0));obj.setDirektbuchungAktiv(true);
  var objects=mock(MietobjektRepository.class);when(objects.findById(7L)).thenReturn(Optional.of(obj));
  area.setId(3L);area.setMietobjekt(obj);area.setBezeichnung("EG mit Wiese");area.setBerechnungsart(Berechnungsart.TAG);when(areas.findAllById(List.of(3L))).thenReturn(List.of(area));
  b.setId(24L);b.setBuchungsnummer("2026-024");b.setMieter(person);b.setMietobjekt(obj);b.setStatus(Buchungsstatus.ABRECHENBAR);b.setBuchungsquelle(Buchungsquelle.DIREKT);b.setAnreise(LocalDateTime.of(2026,7,10,16,0));b.setAbreise(LocalDateTime.of(2026,7,12,14,0));b.setRabattart(Rabattart.VEREIN);
  position.setId(8L);position.setBuchung(b);position.setMietbereich(area);position.setAnzahl(1);position.setVereinbarterPreis(new BigDecimal("900.00"));position.setBerechnungsart(Berechnungsart.BUCHUNG);position.setRabattwertTyp(RabattwertTyp.PROZENTUAL);position.setRabattwert(new BigDecimal("50"));b.getMietbereiche().add(position);
  when(bookings.findLockedById(24L)).thenReturn(Optional.of(b));
  service=new BuchungService(bookings,mock(MieterRepository.class),null,persons,mock(com.kcserver.core.repository.VereinRepository.class),objects,areas,prices,mock(MietbereichRabattRepository.class),null,null,mock(VerbrauchsabrechnungService.class),mock(BuchungVerbrauchskostenRepository.class),mock(BuchungFinanzbewegungRepository.class),null);
  dto=new BuchungDTO();dto.setMieterId(1L);dto.setMietobjektId(7L);dto.setAnreise(b.getAnreise());dto.setAbreise(b.getAbreise());dto.setRabattart(Rabattart.VEREIN);var row=new BuchungMietbereichDTO();row.setMietbereichId(3L);row.setAnzahl(1);dto.setPositionen(List.of(row));
 }
 void katalog(String value){var p=new Mietpreis();p.setPreis(new BigDecimal(value));when(prices.findFirstByMietbereichIdAndBuchungsquelleAndGueltigAbLessThanEqualOrderByGueltigAbDesc(3L,Buchungsquelle.DIREKT,LocalDate.of(2026,7,10))).thenReturn(Optional.of(p));}
 void aktivieren(){dto.setMietpreiseNeuBerechnen(true);dto.setMietpreisStichtag(LocalDate.of(2026,7,10));}
 @Test void normalSpeichernBewahrtExcelPauschale(){
  service.update(24L,dto);assertEquals(new BigDecimal("900.00"),position.getVereinbarterPreis());assertEquals(Berechnungsart.BUCHUNG,position.getBerechnungsart());assertEquals(new BigDecimal("450.00"),BuchungPreiskalkulation.miete(b));verifyNoInteractions(prices);
 }
 @Test void optInUebernimmtTagespreisUndErhaeltRabattregel(){
  aktivieren();katalog("300.00");var result=service.update(24L,dto);
  assertEquals(new BigDecimal("300.00"),position.getVereinbarterPreis());assertEquals(Berechnungsart.TAG,position.getBerechnungsart());assertEquals(new BigDecimal("600.00"),BuchungPreiskalkulation.preis(position));assertEquals(new BigDecimal("300.00"),BuchungPreiskalkulation.miete(b));assertEquals(new BigDecimal("50"),position.getRabattwert());assertEquals(Buchungsstatus.ABRECHENBAR,b.getStatus());assertFalse(result.isMietpreiseNeuBerechnen());verify(bookings).flush();
 }
 @Test void berechnungsartStammtAusKatalogNichtFestAusTag(){
  aktivieren();katalog("150.00");area.setBerechnungsart(Berechnungsart.BUCHUNG);service.update(24L,dto);assertEquals(Berechnungsart.BUCHUNG,position.getBerechnungsart());assertEquals(new BigDecimal("150.00"),BuchungPreiskalkulation.preis(position));
 }
 @Test void fehlenderKatalogpreisKannNichtGespeichertWerden(){
  aktivieren();assertThrows(IllegalArgumentException.class,()->service.update(24L,dto));assertEquals(new BigDecimal("900.00"),position.getVereinbarterPreis());verify(bookings,never()).flush();
 }
 @Test void neuberechnungErfordertAusdruecklichenStichtag(){
  dto.setMietpreiseNeuBerechnen(true);assertThrows(IllegalArgumentException.class,()->service.update(24L,dto));verifyNoInteractions(prices);verify(bookings,never()).flush();
 }
 @Test void stornoErlaubtKeinePreisneuberechnung(){
  aktivieren();b.setStatus(Buchungsstatus.STORNIERT);assertThrows(IllegalArgumentException.class,()->service.update(24L,dto));verifyNoInteractions(prices);verify(bookings,never()).flush();
 }
}
