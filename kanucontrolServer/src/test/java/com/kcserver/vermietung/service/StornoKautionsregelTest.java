package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;
class StornoKautionsregelTest {
 static BigDecimal n(String s){return new BigDecimal(s);}
 static FinanzbewegungDTO m(Finanzkategorie k,Finanzbewegungsart a,String betrag){return new FinanzbewegungDTO(1L,k,a,n(betrag),LocalDate.of(2026,11,19),null);}
 @ParameterizedTest @ValueSource(ints={-1,0,1,55}) void kurzfristigWirdKautionErstattetUndNurMieteEinbehalten(int tage){var d=LocalDate.of(2026,11,19);var result=Stornoberechnung.berechnen(d,d.plusDays(tage),List.of(m(Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"1000"),m(Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"250")));assertEquals(n("250.00"),result.erstattung());assertEquals(n("1000.00"),result.einbehalt());assertTrue(result.bedingungen().containsAll(Set.of(Bedingung.STORNO_ERSTATTUNG_POSITIV,Bedingung.STORNO_EINBEHALT_POSITIV)));}
 @Test void bereitsErstatteteKautionWirdNichtDoppeltZurueckgezahlt(){var d=LocalDate.of(2026,11,19);var result=Stornoberechnung.berechnen(d,d.plusDays(1),List.of(m(Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"1000"),m(Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"250"),m(Finanzkategorie.KAUTION,Finanzbewegungsart.RUECKZAHLUNG,"100")));assertEquals(n("150.00"),result.erstattung());assertEquals(n("1000.00"),result.einbehalt());}
 @Test void vollstaendigeErstattungAb56TagenBleibtErhalten(){var d=LocalDate.of(2026,11,19);var result=Stornoberechnung.berechnen(d,d.plusDays(56),List.of(m(Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"1000"),m(Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"250")));assertTrue(result.volleErstattung());assertEquals(n("1250.00"),result.erstattung());assertEquals(n("0.00"),result.einbehalt());}
 @Test void ohneGezahlteKautionKeineErfundeneErstattung(){var d=LocalDate.of(2026,11,19);var result=Stornoberechnung.berechnen(d,d.plusDays(1),List.of(m(Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"1000")));assertEquals(n("0.00"),result.erstattung());assertEquals(n("1000.00"),result.einbehalt());}
 @Test void nurKautionUndSchonRueckgezahlteMietueberzahlungSindBegrenzt(){var d=LocalDate.of(2026,11,19);assertEquals(n("250.00"),Stornoberechnung.berechnen(d,d.plusDays(1),List.of(m(Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"250"))).erstattung());assertEquals(n("150.00"),Stornoberechnung.verbleibendeKaution(List.of(m(Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"1000"),m(Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"250"),m(Finanzkategorie.MIETE,Finanzbewegungsart.RUECKZAHLUNG,"1100"))));}
 @Test void gespeicherterAlterEinbehaltSchliesstKautionInAktuellerAnsichtAusUndRueckzahlungAendertNurEchtenGeldfluss(){
  var b=AbrechnungVorlagenTest.buchung();b.setStatus(Buchungsstatus.STORNIERT);b.setStornoDatum(b.getAnreise().toLocalDate().minusDays(1));b.setStornoEinbehalt(n("1250"));
  var br=mock(BuchungRepository.class);var mr=mock(BuchungFinanzbewegungRepository.class);var vr=mock(BuchungVerbrauchskostenRepository.class);var moves=new ArrayList<BuchungFinanzbewegung>();
  for(var value:List.of(m(Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"1000"),m(Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"250"))){var move=new BuchungFinanzbewegung();move.setBuchung(b);move.setId((long)moves.size()+1);move.setKategorie(value.kategorie());move.setArt(value.art());move.setBetrag(value.betrag());move.setDatum(value.datum());moves.add(move);}
  when(br.findLockedById(1L)).thenReturn(Optional.of(b));when(mr.findByBuchungIdOrderByDatumAscIdAsc(1L)).thenAnswer(i->List.copyOf(moves));when(mr.save(any())).thenAnswer(i->{BuchungFinanzbewegung move=i.getArgument(0);move.setId(3L);moves.add(move);return move;});
  var service=new BuchungFinanzenService(br,mr,vr,mock(VerbrauchsabrechnungService.class));assertEquals(n("1000.00"),service.toDto(b).stornoEinbehalt());assertEquals(n("-250.00"),service.toDto(b).saldo());assertEquals(n("1250"),b.getStornoEinbehalt());verify(mr,never()).save(any());
  assertThrows(IllegalArgumentException.class,()->service.add(1L,new FinanzbewegungSaveDTO(Finanzkategorie.KAUTION,Finanzbewegungsart.RUECKZAHLUNG,n("251"),LocalDate.now(),null)));assertEquals(2,moves.size());
  var result=service.add(1L,new FinanzbewegungSaveDTO(Finanzkategorie.KAUTION,Finanzbewegungsart.RUECKZAHLUNG,n("250"),LocalDate.now(),null));assertEquals(n("0.00"),result.saldo());assertEquals(n("1000.00"),b.getStornoEinbehalt());assertEquals(Buchungsstatus.STORNIERT,b.getStatus());
  assertThrows(IllegalArgumentException.class,()->service.add(1L,new FinanzbewegungSaveDTO(Finanzkategorie.KAUTION,Finanzbewegungsart.RUECKZAHLUNG,n("1"),LocalDate.now(),null)));assertThrows(IllegalArgumentException.class,()->service.add(1L,new FinanzbewegungSaveDTO(Finanzkategorie.MIETE,Finanzbewegungsart.RUECKZAHLUNG,n("1"),LocalDate.now(),null)));assertEquals(3,moves.size());
 }
 @Test void bekannteAlteStandardtexteWerdenOhneFalscheFristaussageGelesen(){var v=new Vorlage(null,"Stornierung","Mail",List.of(new Block("Erstattung","Da die Stornierung mindestens 56 Tage vor Mietbeginn erfolgt, werden alle geleisteten Zahlungen zurückerstattet.",Bedingung.STORNO_ERSTATTUNG_POSITIV,List.of())));var text=VorlagenModell.normalisieren(v).bloecke().getFirst().text();assertTrue(text.contains("Kaution"));assertFalse(text.contains("mindestens 56 Tage"));}
}
