package kc.isolated;
import com.kcserver.vermietung.service.VerbrauchAuswertung;
import com.kcserver.vermietung.dto.VerbrauchDashboardDTO.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class VerbrauchAuswertungTest {
    final LocalDateTime now=LocalDateTime.of(2026,12,31,23,59);
    Zaehler meter(){var m=new Zaehler();m.setId(1L);m.setBezeichnung("Gas");m.setVerbrauchsart(Verbrauchsart.GAS);m.setMengeneinheit("m³");return m;}
    Zaehlerstand read(String time,String value){var z=new Zaehlerstand();z.setAblesedatum(LocalDateTime.parse(time));z.setZaehlerstand(new BigDecimal(value));return z;}
    Belegung booking(long id,String from,String to){return new Belegung(id,"2026-00"+id,"BESTAETIGT",LocalDateTime.parse(from),LocalDateTime.parse(to));}
    ZaehlerAuswertung calc(List<Zaehlerstand> reads,List<Belegung> books){return VerbrauchAuswertung.calculate(meter(),2026,now,reads,books,false);}
    void near(double expected,BigDecimal n){assertNotNull(n);assertEquals(expected,n.doubleValue(),0.00001);}

    @Test void genaueAnUndAbreiseErgeben46StundenUndLeerzeitanteil(){
        var d=calc(List.of(read("2026-01-01T00:00","0"),read("2026-01-04T00:00","72")),
            List.of(booking(1,"2026-01-01T16:00","2026-01-03T14:00")));
        near(72,d.kennzahlen().gesamt());near(46,d.kennzahlen().mitBuchung());near(26,d.kennzahlen().ohneBuchung());
        near(24,d.kennzahlen().pro24hMit());near(24,d.kennzahlen().pro24hOhne());assertTrue(d.intervalle().getFirst().geschaetzt());
    }
    @Test void vollstaendigBelegteUndLeereMessintervalleSindEindeutig(){
        var d=calc(List.of(read("2026-01-01T00:00","0"),read("2026-01-02T00:00","10"),read("2026-01-03T00:00","12")),
            List.of(booking(1,"2026-01-01T00:00","2026-01-02T00:00")));
        near(10,d.kennzahlen().mitBuchung());near(2,d.kennzahlen().ohneBuchung());
        assertTrue(d.intervalle().stream().noneMatch(Intervall::geschaetzt));assertEquals(List.of("2026-001"),d.intervalle().getFirst().buchungen());
    }
    @Test void ueberlappendeBuchungenZaehlenNichtDoppelt(){
        var d=calc(List.of(read("2026-01-01T00:00","0"),read("2026-01-04T00:00","72")),
            List.of(booking(1,"2026-01-01T00:00","2026-01-03T00:00"),booking(2,"2026-01-02T00:00","2026-01-04T00:00")));
        near(72,d.kennzahlen().mitBuchung());near(0,d.kennzahlen().ohneBuchung());assertFalse(d.intervalle().getFirst().geschaetzt());
    }
    @Test void monatsgrenzenWerdenZeitanteiligVerteiltUndAlsGeschaetztMarkiert(){
        var d=calc(List.of(read("2026-01-31T00:00","0"),read("2026-02-02T00:00","48")),List.of());
        near(24,d.monate().get(0).verbrauch());near(24,d.monate().get(1).verbrauch());
        assertTrue(d.monate().get(0).geschaetzt());assertTrue(d.monate().get(1).geschaetzt());assertEquals(1,d.korrelation().intervalle());
    }
    @Test void jahresgrenzenWerdenBegrenztUndNichtAlsUnabhaengigeMesspunkteGezaehlt(){
        var d=calc(List.of(read("2025-12-31T00:00","0"),read("2026-01-02T00:00","48")),List.of());
        near(24,d.kennzahlen().gesamt());near(24,d.kennzahlen().abgedeckteStunden());assertTrue(d.intervalle().getFirst().geschaetzt());assertEquals(0,d.korrelation().intervalle());
    }
    @Test void zeitumstellungNutztTatsaechlicheStunden(){
        var d=calc(List.of(read("2026-03-28T00:00","0"),read("2026-03-30T00:00","47")),
            List.of(booking(1,"2026-03-28T00:00","2026-03-30T00:00")));
        near(47,d.kennzahlen().abgedeckteStunden());near(24,d.kennzahlen().pro24hMit());
    }
    @Test void fehlendeMessdatenWerdenNichtAlsNullverbrauchOderHochrechnungAusgegeben(){
        var d=calc(List.of(read("2026-01-01T00:00","0")),List.of());
        assertTrue(d.intervalle().isEmpty());near(0,d.kennzahlen().abgedeckteStunden());assertNull(d.kennzahlen().pro24hOhne());
        assertTrue(d.hinweise().stream().anyMatch(h->h.contains("mindestens zwei")));
    }
    @Test void fallendeStaendeLassenNurDasBetroffeneIntervallAus(){
        var d=calc(List.of(read("2026-01-01T00:00","100"),read("2026-01-02T00:00","10"),read("2026-01-03T00:00","20")),List.of());
        near(10,d.kennzahlen().gesamt());near(24,d.kennzahlen().abgedeckteStunden());assertTrue(d.hinweise().stream().anyMatch(h->h.contains("fallende")));
    }
    @Test void widerspruechlicheDoppelteStaendeWerdenNichtWillkuerlichAusgewaehlt(){
        var d=calc(List.of(read("2026-01-01T00:00","0"),read("2026-01-02T00:00","10"),read("2026-01-02T00:00","20"),read("2026-01-03T00:00","30")),List.of());
        assertTrue(d.intervalle().isEmpty());assertTrue(d.hinweise().stream().anyMatch(h->h.contains("widersprüchlich")));
        var identical=calc(List.of(read("2026-01-01T00:00","0"),read("2026-01-01T00:00","0.000"),read("2026-01-02T00:00","10")),List.of());near(10,identical.kennzahlen().gesamt());
    }
    @Test void zukunftsablesungenWerdenNichtZurBerechnungGenutzt(){
        var d=VerbrauchAuswertung.calculate(meter(),2026,LocalDateTime.parse("2026-01-02T00:00"),
            List.of(read("2026-01-01T00:00","0"),read("2026-01-03T00:00","100")),List.of(),false);
        assertTrue(d.intervalle().isEmpty());near(24,d.kennzahlen().zeitraumStunden());
    }
    @Test void korrelationBasiertAufVollstaendigenIntervallenStattInterpoliertenMonaten(){
        var d=calc(List.of(read("2026-01-01T00:00","0"),read("2026-01-02T00:00","1"),read("2026-01-03T00:00","3"),read("2026-01-04T00:00","6")),
            List.of(booking(1,"2026-01-02T12:00","2026-01-04T00:00")));
        assertEquals(3,d.korrelation().intervalle());assertEquals(1,d.korrelation().pearson(),0.00001);
        var flat=calc(List.of(read("2026-01-01T00:00","0"),read("2026-01-02T00:00","1"),read("2026-01-03T00:00","2"),read("2026-01-04T00:00","3")),List.of());assertNull(flat.korrelation().pearson());
    }
    @Test void gemeinsameUndInaktiveZaehlerBleibenTransparent(){
        var meter=meter();meter.setAktiv(false);
        var d=VerbrauchAuswertung.calculate(meter,2026,now,List.of(),List.of(),true);
        assertTrue(d.gemeinsam());assertTrue(d.hinweise().stream().anyMatch(h->h.contains("anderer")));assertTrue(d.hinweise().stream().anyMatch(h->h.contains("inaktiv")));
    }
}
