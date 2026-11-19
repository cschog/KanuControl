package kc.isolated;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerbrauchszeitraumTest {
    final Mietobjekt objekt = new Mietobjekt();
    final VerbrauchskostenRepository positionen = mock(VerbrauchskostenRepository.class);
    final ZaehlerstandRepository ablesungen = mock(ZaehlerstandRepository.class);
    final BuchungVerbrauchskostenRepository gespeichert = mock(BuchungVerbrauchskostenRepository.class);
    final VerbrauchsabrechnungService service = new VerbrauchsabrechnungService(positionen, mock(ZaehlerRepository.class), ablesungen, gespeichert);
    long id = 1;

    Verbrauchskosten tarif(Verbrauchsart art, String von, String bis, String preis) {
        var k = new Verbrauchskosten(); k.setId(id++); k.setMietobjekt(objekt); k.setBezeichnung(art + " " + von);
        k.setVerbrauchsart(art); k.setAbrechnungsart(art == Verbrauchsart.WASSER ? Verbrauchsabrechnungsart.TAGESPREIS : Verbrauchsabrechnungsart.ZAEHLERSTAND);
        k.setMengeneinheit(art == Verbrauchsart.STROM ? "kWh" : art == Verbrauchsart.GAS ? "m3" : null);
        k.getPreise().add(new Verbrauchspreis(LocalDate.parse(von), bis == null ? null : LocalDate.parse(bis), new BigDecimal(preis)));
        return k;
    }

    List<Verbrauchskosten> historie() {
        return new ArrayList<>(List.of(
            tarif(Verbrauchsart.STROM,"2026-07-01",null,"2.00"),
            tarif(Verbrauchsart.GAS,"2026-01-01",null,"3.00"),
            tarif(Verbrauchsart.WASSER,"2010-01-01",null,"10.00"),
            tarif(Verbrauchsart.STROM,"1999-01-01","2009-12-31","0.50"),
            tarif(Verbrauchsart.STROM,"2010-01-01","2021-12-31","0.75"),
            tarif(Verbrauchsart.STROM,"2022-01-01","2026-06-30","1.00"),
            tarif(Verbrauchsart.GAS,"1999-01-01","2009-12-31","0.50"),
            tarif(Verbrauchsart.GAS,"2010-01-01","2021-12-31","1.00"),
            tarif(Verbrauchsart.GAS,"2022-01-01","2025-12-31","2.50"),
            tarif(Verbrauchsart.WASSER,"1999-01-01","2001-12-31","4.00"),
            tarif(Verbrauchsart.WASSER,"2002-01-01","2009-12-31","5.00")));
    }

    Buchung buchung(String von, String bis) {
        objekt.setId(1L);
        var b = new Buchung(); b.setId(20L); b.setMietobjekt(objekt); b.setBuchungsquelle(Buchungsquelle.DIREKT);
        b.setStatus(Buchungsstatus.ABRECHENBAR); b.setAnreise(LocalDate.parse(von).atTime(16,0)); b.setAbreise(LocalDate.parse(bis).atTime(14,0));
        return b;
    }

    void zaehler(Buchung b, Verbrauchsart art, long meterId, String anfang, String ende) {
        var z = new Zaehler(); z.setId(meterId); z.setAktiv(true); z.setVerbrauchsart(art); z.setBezeichnung(art + " Bootshaus");
        var bereich = new Mietbereich(); bereich.setZaehler(Set.of(z));
        var position = new BuchungMietbereich(); position.setMietbereich(bereich);
        var bereiche = new ArrayList<>(b.getMietbereiche()); bereiche.add(position); b.setMietbereiche(bereiche);
        var a = new Zaehlerstand(); a.setId(meterId * 10); a.setZaehler(z); a.setAblesedatum(b.getAnreise()); a.setZaehlerstand(new BigDecimal(anfang));
        var e = new Zaehlerstand(); e.setId(meterId * 10 + 1); e.setZaehler(z); e.setAblesedatum(b.getAbreise()); e.setZaehlerstand(new BigDecimal(ende));
        when(ablesungen.findByZaehlerIdOrderByAblesedatumDesc(meterId)).thenReturn(List.of(e,a));
    }

    @Test void prodFall2022WaehltJeArtGenauEinePositionUndSpeichertPassendeKosten() {
        var b = buchung("2022-10-14","2022-10-16");
        when(positionen.findByMietobjektIdOrderByIdAsc(1L)).thenReturn(historie());
        zaehler(b,Verbrauchsart.STROM,7,"11","30"); zaehler(b,Verbrauchsart.GAS,8,"100","104");
        var vorschau = service.berechneVorschau(b);
        assertEquals(3,vorschau.size()); assertTrue(vorschau.stream().allMatch(v -> v.erfolgreich()));
        assertEquals(List.of(new BigDecimal("1.00"),new BigDecimal("2.50"),new BigDecimal("10.00")),vorschau.stream().map(v -> v.preis()).toList());
        assertEquals(List.of(new BigDecimal("19.00"),new BigDecimal("10.00"),new BigDecimal("20.00")),vorschau.stream().map(v -> v.kosten()).toList());
        var snapshots = service.berechneSnapshots(b,List.of()); assertEquals(3,snapshots.size());
        assertEquals(vorschau.stream().map(v -> v.kosten()).toList(),snapshots.stream().map(BuchungVerbrauchskosten::getKosten).toList());
        service.berechneUndSpeichere(b);
        verify(gespeichert).saveAll(argThat(rows -> {int count=0;for(var row:rows)count++;return count==3;}));
    }

    @Test void inaktivAltbestandWirdNachDatumGenausoVerwendetUndReihenfolgeIstEgal() {
        var daten = historie(); daten.forEach(p -> p.setAktiv(false)); Collections.reverse(daten);
        var auswahl = VerbrauchspreisAuswahl.zumDatum(daten,LocalDate.of(2022,10,14));
        assertEquals(3,auswahl.size()); assertTrue(auswahl.stream().allMatch(a -> a.hinweis()==null));
        assertEquals(new BigDecimal("1.00"),auswahl.stream().filter(a -> a.basis().getVerbrauchsart()==Verbrauchsart.STROM).findFirst().orElseThrow().konfiguration().getPreis());
    }

    @Test void inklusiveGrenzenUndPreissprungWaehlenEindeutig() {
        var daten=historie();
        for(var test:List.of(Map.entry("2009-12-31","0.50"),Map.entry("2010-01-01","0.75"),Map.entry("2026-06-30","1.00"),Map.entry("2026-07-01","2.00"))) {
            var strom=VerbrauchspreisAuswahl.zumDatum(daten,LocalDate.parse(test.getKey())).stream().filter(a -> a.basis().getVerbrauchsart()==Verbrauchsart.STROM).findFirst().orElseThrow();
            assertNull(strom.hinweis()); assertEquals(new BigDecimal(test.getValue()),strom.konfiguration().getPreis());
        }
    }

    @Test void fehlenderTarifErgibtEinenFehlerJeArtUndKeineSpeicherung() {
        var b=buchung("1998-10-14","1998-10-16");when(positionen.findByMietobjektIdOrderByIdAsc(1L)).thenReturn(historie());
        var v=service.berechneVorschau(b);assertEquals(3,v.size());assertTrue(v.stream().allMatch(p -> !p.erfolgreich() && p.hinweis().contains("Kein gültiger Preis")));
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(b));verifyNoInteractions(gespeichert);
    }

    @Test void lueckeInnerhalbDerHistorieBleibtFehlerAuchWennAndereArtenPassen() {
        var daten=historie();daten.removeIf(p -> p.getVerbrauchsart()==Verbrauchsart.STROM && p.getPreise().getFirst().getGueltigVon().equals(LocalDate.of(2022,1,1)));
        var v=VerbrauchspreisAuswahl.zumDatum(daten,LocalDate.of(2022,10,14));
        assertEquals(1,v.stream().filter(a -> a.hinweis()!=null).count());assertTrue(v.getFirst().hinweis().contains("Strom"));
    }

    @Test void ueberlappungAuchBeiIdentischemPreisVerhindertDoppelabrechnung() {
        var b=buchung("2022-10-14","2022-10-16");var daten=historie();daten.add(tarif(Verbrauchsart.STROM,"2022-10-14",null,"1.00"));
        when(positionen.findByMietobjektIdOrderByIdAsc(1L)).thenReturn(daten);
        var strom=service.berechneVorschau(b).getFirst();assertFalse(strom.erfolgreich());assertTrue(strom.hinweis().contains("Mehrere gültige Preise"));
        assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(b));verifyNoInteractions(gespeichert);
    }

    @Test void zweiPreiseInnerhalbEinerPositionSindEbenfallsMehrdeutig() {
        var k=tarif(Verbrauchsart.WASSER,"2022-01-01",null,"10");k.getPreise().add(new Verbrauchspreis(LocalDate.of(2022,10,14),null,BigDecimal.ONE));
        assertTrue(VerbrauchspreisAuswahl.zumDatum(List.of(k),LocalDate.of(2022,10,14)).getFirst().hinweis().contains("Mehrere gültige Preise"));
    }

    @Test void gemeinsamePreishistorieFunktioniertWeiterUndAufenthaltBehaeltAnreisepreis() {
        var b=buchung("2025-12-30","2026-01-03");
        var k=tarif(Verbrauchsart.WASSER,"2025-01-01","2025-12-31","10.00");k.getPreise().add(new Verbrauchspreis(LocalDate.of(2026,1,1),null,new BigDecimal("20.00")));
        when(positionen.findByMietobjektIdOrderByIdAsc(1L)).thenReturn(List.of(k));
        assertEquals(new BigDecimal("40.00"),service.berechneSnapshots(b,List.of()).getFirst().getKosten());
    }

    @Test void abgeschlosseneBuchungBleibtUnabhaengigVonHeutigerHistorie() {
        var b=buchung("2022-10-14","2022-10-16");b.setStatus(Buchungsstatus.ABGESCHLOSSEN);
        var original=new BuchungVerbrauchskosten();original.setPreis(new BigDecimal("0.99"));original.setKosten(new BigDecimal("18.81"));
        when(gespeichert.findByBuchungIdOrderByIdAsc(20L)).thenReturn(List.of(original));
        assertSame(original,service.berechneSnapshots(b,List.of()).getFirst());assertEquals(original.getKosten(),service.berechneVorschau(b).getFirst().kosten());
        service.berechneUndSpeichere(b);verifyNoInteractions(positionen,ablesungen);verify(gespeichert,never()).deleteByBuchungId(any());
    }
}
