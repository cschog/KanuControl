package kc.isolated;

import com.kcserver.vermietung.dto.AblesungBestaetigungDTO;
import com.kcserver.vermietung.dto.VerbrauchskostenVorschauDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.VerbrauchsabrechnungService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerbrauchAblesungBestaetigungTest {
    final VerbrauchskostenRepository configs=mock(VerbrauchskostenRepository.class);
    final ZaehlerstandRepository reads=mock(ZaehlerstandRepository.class);
    final BuchungVerbrauchskostenRepository snapshots=mock(BuchungVerbrauchskostenRepository.class);
    final VerbrauchsabrechnungService service=new VerbrauchsabrechnungService(configs,mock(ZaehlerRepository.class),reads,snapshots);
    final Buchung booking=new Buchung();
    final LocalDateTime start=LocalDateTime.of(2026,10,10,16,0), end=start.plusDays(8);
    Zaehler meter;
    @BeforeEach void setup() {
        var object=new Mietobjekt();object.setId(1L);
        booking.setId(10L);booking.setMietobjekt(object);booking.setBuchungsquelle(Buchungsquelle.DIREKT);booking.setAnreise(start);booking.setAbreise(end);
        meter=new Zaehler();meter.setId(2L);meter.setBezeichnung("Strom EG");meter.setAktiv(true);meter.setVerbrauchsart(Verbrauchsart.STROM);
        var area=new Mietbereich();area.setZaehler(Set.of(meter));
        var pos=new BuchungMietbereich();pos.setMietbereich(area);booking.setMietbereiche(List.of(pos));
        var config=new Verbrauchskosten();config.setId(3L);config.setBezeichnung("Strom");config.setVerbrauchsart(Verbrauchsart.STROM);config.setAbrechnungsart(Verbrauchsabrechnungsart.ZAEHLERSTAND);config.setPreis(new BigDecimal("0.40"));config.setMengeneinheit("kWh");
        when(configs.findByMietobjektIdOrderByIdAsc(1L)).thenReturn(List.of(config));
    }
    Zaehlerstand reading(long id,LocalDateTime time,String value) {
        var r=new Zaehlerstand();r.setId(id);r.setZaehler(meter);r.setAblesedatum(time);r.setZaehlerstand(new BigDecimal(value));return r;
    }
    void readings(Zaehlerstand... values) {when(reads.findByZaehlerIdOrderByAblesedatumDesc(2L)).thenReturn(List.of(values));}
    VerbrauchskostenVorschauDTO preview() {return service.berechneVorschau(booking).getFirst();}
    AblesungBestaetigungDTO consent(VerbrauchskostenVorschauDTO p) {
        return new AblesungBestaetigungDTO(2L,start,end,p.anfangsablesung(),p.endablesung(),p.anfangsstand(),p.endstand());
    }
    @Test void genau24StundenBleibenOhneNachfrageZulaessig() {
        readings(reading(1,start.minusHours(24),"100"),reading(2,end.plusHours(24),"125"));
        var p=preview();assertTrue(p.erfolgreich());assertFalse(p.bestaetigungErforderlich());
        service.berechneUndSpeichere(booking);verify(snapshots).saveAll(any());
    }
    @Test void mehrAls24StundenErgibtNurVorschlagUndKeineAutomatischeAbrechnung() {
        readings(reading(1,start.minusHours(24).minusSeconds(1),"100"),reading(2,end,"125"));
        var p=preview();assertFalse(p.erfolgreich());assertTrue(p.bestaetigungErforderlich());assertEquals(new BigDecimal("10.00"),p.kosten());
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking));verifyNoInteractions(snapshots);
    }
    @Test void auchNurEntfernteEndablesungBenoetigtZustimmung() {
        readings(reading(1,start,"100"),reading(2,end.plusHours(25),"125"));
        assertTrue(preview().bestaetigungErforderlich());assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking));verifyNoInteractions(snapshots);
    }
    @Test void bestaetigteNaechstliegendeWerteWerdenAlsSnapshotGespeichert() {
        readings(reading(1,start.minusHours(30),"100"),reading(2,end.plusHours(28),"125"),reading(3,start.minusDays(10),"50"));
        var p=preview();service.berechneUndSpeichere(booking,List.of(consent(p)));
        @SuppressWarnings("unchecked") ArgumentCaptor<List<BuchungVerbrauchskosten>> captor=ArgumentCaptor.forClass(List.class);
        verify(snapshots).saveAll(captor.capture());var s=captor.getValue().getFirst();
        assertEquals(p.anfangsablesung(),s.getAnfangsablesung());assertEquals(p.endablesung(),s.getEndablesung());assertEquals(p.kosten(),s.getKosten());assertEquals(p.verbrauch(),s.getVerbrauch());
    }
    @Test void geaenderteMesswerteNachVorschauWerdenNichtUebernommen() {
        var a=reading(1,start.minusHours(30),"100");var z=reading(2,end.plusHours(30),"125");readings(a,z);
        var consent=consent(preview());z.setZaehlerstand(new BigDecimal("130"));
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking,List.of(consent)));verifyNoInteractions(snapshots);
    }
    @Test void neueNaehereAblesungMussErneutBestaetigtWerden() {
        var a=reading(1,start.minusHours(40),"100");var z=reading(2,end.plusHours(40),"125");readings(a,z);var consent=consent(preview());
        readings(a,z,reading(3,start.minusHours(30),"102"));
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking,List.of(consent)));verifyNoInteractions(snapshots);
    }
    @Test void geaenderterBuchungszeitraumMachtZustimmungUngueltig() {
        readings(reading(1,start.minusHours(30),"100"),reading(2,end.plusHours(30),"125"));var consent=consent(preview());booking.setAbreise(end.minusHours(1));
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking,List.of(consent)));verifyNoInteractions(snapshots);
    }
    @Test void fremdeZaehlerZustimmungGiltNicht() {
        readings(reading(1,start.minusHours(30),"100"),reading(2,end.plusHours(30),"125"));var p=preview();
        var wrong=new AblesungBestaetigungDTO(99L,start,end,p.anfangsablesung(),p.endablesung(),p.anfangsstand(),p.endstand());
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking,List.of(wrong)));verifyNoInteractions(snapshots);
    }
    @Test void identischeOderFehlendeStaendeBleibenGesperrt() {
        readings(reading(1,start,"100"));var p=preview();assertFalse(p.erfolgreich());assertFalse(p.bestaetigungErforderlich());
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking,List.of(consent(p))));
        readings();var missing=preview();assertFalse(missing.erfolgreich());assertFalse(missing.bestaetigungErforderlich());verifyNoInteractions(snapshots);
    }
    @Test void negativerVerbrauchKannNichtBestaetigtWerden() {
        readings(reading(1,start.minusHours(30),"200"),reading(2,end.plusHours(30),"125"));var p=preview();assertFalse(p.erfolgreich());assertFalse(p.bestaetigungErforderlich());
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking));verifyNoInteractions(snapshots);
    }
    @Test void gleicheAbstaendeWerdenStabilNachFrueheremZeitpunktAufgeloest() {
        readings(reading(2,start.plusHours(25),"110"),reading(1,start.minusHours(25),"100"),reading(3,end,"125"));
        assertEquals(start.minusHours(25),preview().anfangsablesung());
    }
    @Test void januarBuchungSchlaegtVorherigeNovemberAblesungVor() {
        booking.setAnreise(LocalDateTime.of(2026,1,24,8,0));
        booking.setAbreise(LocalDateTime.of(2026,1,25,14,0));
        var november=LocalDateTime.of(2025,11,2,0,0);
        var januar=LocalDateTime.of(2026,1,25,0,0);
        readings(reading(1,november,"4261"),reading(2,januar,"4748"));
        var p=preview();assertEquals(november,p.anfangsablesung());assertEquals(januar,p.endablesung());
        assertEquals(new BigDecimal("487"),p.verbrauch());assertTrue(p.bestaetigungErforderlich());
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking));
        var ok=new AblesungBestaetigungDTO(2L,booking.getAnreise(),booking.getAbreise(),
                p.anfangsablesung(),p.endablesung(),p.anfangsstand(),p.endstand());
        service.berechneUndSpeichere(booking,List.of(ok));
        @SuppressWarnings("unchecked") ArgumentCaptor<List<BuchungVerbrauchskosten>> captor=ArgumentCaptor.forClass(List.class);
        verify(snapshots).saveAll(captor.capture());
        assertEquals(november,captor.getValue().getFirst().getAnfangsablesung());
        assertEquals(januar,captor.getValue().getFirst().getEndablesung());
    }
    @Test void gemeinsameAnfangsablesungErgaenztNaechsteSpaetereAblesung() {
        readings(reading(1,start,"100"),reading(2,end.plusDays(20),"125"));
        var p=preview();assertEquals(start,p.anfangsablesung());assertEquals(end.plusDays(20),p.endablesung());
        assertTrue(p.bestaetigungErforderlich());
    }
    @Test void gleicheZeitpunkteSindKeinGueltigesMessintervall() {
        readings(reading(1,start,"100"),reading(2,start,"101"));
        assertFalse(preview().erfolgreich());assertFalse(preview().bestaetigungErforderlich());
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(booking));
    }
    @Test void gemeinsameEndablesungBehaeltEndeAuchBeiSpaeteremMesswert() {
        readings(reading(1,start.minusDays(90),"100"),reading(2,end,"125"),reading(3,end.plusDays(2),"130"));
        assertEquals(start.minusDays(90),preview().anfangsablesung());
        assertEquals(end,preview().endablesung());
    }
    @Test void airbnbBenoetigtWeiterhinKeineVerbrauchsabrechnung() {
        booking.setBuchungsquelle(Buchungsquelle.AIRBNB);assertTrue(service.berechneVorschau(booking).isEmpty());service.berechneUndSpeichere(booking);verifyNoInteractions(reads,snapshots);
    }
}
