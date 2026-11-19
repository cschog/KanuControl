package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.dto.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class WorkflowErgaenzungTest {
 BuchungRepository br;BuchungFinanzbewegungRepository mr;BuchungVerbrauchskostenRepository vr;Buchung b;BuchungFinanzenService service;List<BuchungFinanzbewegung> moves;
 @BeforeEach void setup(){br=mock(BuchungRepository.class);mr=mock(BuchungFinanzbewegungRepository.class);vr=mock(BuchungVerbrauchskostenRepository.class);b=AbrechnungVorlagenTest.buchung();b.setAbrechnungRevision(3);b.setAbschlussRevision(3);moves=new ArrayList<>();when(br.findLockedById(1L)).thenReturn(Optional.of(b));when(mr.findByBuchungIdOrderByDatumAscIdAsc(1L)).thenAnswer(i->List.copyOf(moves));doAnswer(i->{moves.removeAll(i.getArgument(0));return null;}).when(mr).deleteAll(anyList());service=new BuchungFinanzenService(br,mr,vr,mock(VerbrauchsabrechnungService.class));}
 BuchungFinanzbewegung move(long id,Finanzkategorie k,Finanzbewegungsart art){var m=new BuchungFinanzbewegung();m.setId(id);m.setBuchung(b);m.setKategorie(k);m.setArt(art);m.setBetrag(new BigDecimal("25.00"));m.setDatum(LocalDate.of(2026,11,19));moves.add(m);return m;}
 @Test void zahlungenAuchNachAbschlussOhneGrundLoeschenUndSaldoNeuBerechnen(){move(1,Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG);move(2,Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG);var before=service.toDto(b);var result=service.entfernen(1L,List.of(1L,2L));assertTrue(result.bewegungen().isEmpty());assertEquals(new BigDecimal("50.00"),result.saldo().subtract(before.saldo()));assertEquals(Buchungsstatus.ABGESCHLOSSEN,b.getStatus());assertEquals(4,b.getAbrechnungRevision());assertEquals(3,b.getAbschlussRevision());}
 @Test void fremdeOderDoppelteIdsLoeschenNichts(){move(1,Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG);assertThrows(Exception.class,()->service.entfernen(1L,List.of(1L,999L)));assertThrows(Exception.class,()->service.entfernen(1L,List.of(1L,1L)));verify(mr,never()).deleteAll(anyList());assertEquals(1,moves.size());}
 @Test void kautionsverrechnungMussAufBeidenSeitenEntferntWerden(){move(1,Finanzkategorie.KAUTION,Finanzbewegungsart.VERRECHNUNG);move(2,Finanzkategorie.VERBRAUCHSKOSTEN,Finanzbewegungsart.VERRECHNUNG);assertThrows(Exception.class,()->service.entfernen(1L,List.of(1L)));assertEquals(2,moves.size());assertTrue(service.entfernen(1L,List.of(1L,2L)).bewegungen().isEmpty());}
 @Test void neueZahlungNachAbschlussMachtAktuelleAbrechnungErneuerbar(){when(mr.save(any())).thenAnswer(i->{var m=(BuchungFinanzbewegung)i.getArgument(0);m.setId(1L);moves.add(m);return m;});service.add(1L,new FinanzbewegungSaveDTO(Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,new BigDecimal("12"),LocalDate.of(2026,11,19),null));assertEquals(1,moves.size());assertEquals(Buchungsstatus.ABGESCHLOSSEN,b.getStatus());assertEquals(4,b.getAbrechnungRevision());}
 @Test void aktuelleRechnungNachZahlungskorrekturOhneWiederoeffnen(){var docs=mock(VermietungAbrechnungRepository.class);var renderer=mock(AbrechnungVorlagenService.class);b.setAbrechnungRevision(4);when(renderer.erstellen(eq(b),anyInt(),isNull())).thenReturn(new byte[]{1,2});when(docs.saveAndFlush(any())).thenAnswer(i->{var d=(VermietungAbrechnung)i.getArgument(0);d.setId(99L);return d;});new VermietungAbrechnungService(br,docs,renderer).create(1L,new VermietungAbrechnungService.Ausstellung(null,""));assertEquals(Buchungsstatus.ABGESCHLOSSEN,b.getStatus());assertEquals(4,b.getAbschlussRevision());verify(renderer).erstellen(b,1,null);}
 @Test void bestaetigungDarfZahlungenErstAnfordern(){b.setStatus(Buchungsstatus.RESERVIERT);b.setAnzahlungBetrag(BigDecimal.ZERO);assertDoesNotThrow(()->BuchungsnachrichtDaten.zahlungsplanPruefen(b,Buchungsstatus.BESTAETIGT,List.of()));assertFalse(BuchungsnachrichtDaten.ohneMail(Buchungsstatus.RESERVIERT,Buchungsstatus.BESTAETIGT));assertTrue(BuchungsnachrichtDaten.ohneMail(Buchungsstatus.STORNIERT,Buchungsstatus.BESTAETIGT));}
 @Test void auchAbgelehnteBuchungenBleibenFuerZahlungskorrekturenErreichbar(){b.setStatus(Buchungsstatus.ABGELEHNT);move(1,Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG);when(br.findAll()).thenReturn(List.of(b));assertEquals(1,service.getAll().size());assertTrue(service.entfernen(1L,List.of(1L)).bewegungen().isEmpty());assertEquals(Buchungsstatus.ABGELEHNT,b.getStatus());}

}
