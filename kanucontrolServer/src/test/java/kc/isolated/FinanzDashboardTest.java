package kc.isolated;
import com.kcserver.core.entity.Person;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.FinanzDashboardService;
import org.junit.jupiter.api.Test;
import java.math.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanzDashboardTest {
    final LocalDate today=LocalDate.of(2026,7,1);
    final BigDecimal limit=new BigDecimal("25000");
    Buchung booking(long id, int year, Buchungsstatus status, String rent) {
        var b=new Buchung();b.setId(id);b.setBuchungsjahr(year);b.setLaufendeNummer((int)id);
        b.setAnreise(LocalDate.of(year,8,1).atTime(16,0));b.setAbreise(LocalDate.of(year,8,3).atTime(14,0));
        b.setStatus(status);b.setBuchungsquelle(Buchungsquelle.DIREKT);
        var person=new Person();person.setVorname("Test");person.setName("Mieter");b.setMieter(person);
        var object=new Mietobjekt();object.setBezeichnung("Bootshaus");b.setMietobjekt(object);
        var position=new BuchungMietbereich();position.setBuchung(b);position.setAnzahl(1);
        position.setBerechnungsart(Berechnungsart.BUCHUNG);position.setVereinbarterPreis(new BigDecimal(rent));
        b.setMietbereiche(List.of(position));return b;
    }
    BuchungFinanzbewegung move(Buchung b, String date, Finanzkategorie k, Finanzbewegungsart a, String value) {
        var m=new BuchungFinanzbewegung();m.setBuchung(b);m.setDatum(LocalDate.parse(date));m.setKategorie(k);m.setArt(a);m.setBetrag(new BigDecimal(value));return m;
    }
    FinanzDashboardDTO calc(List<BuchungFinanzbewegung> moves,List<Buchung> bookings) {
        return FinanzDashboardService.calculate(2026,today,limit,moves,bookings,List.of());
    }
    void amount(String expected,BigDecimal value){assertEquals(0,new BigDecimal(expected).compareTo(value));}

    @Test void umsatzFolgtZahlungsdatumAuchBeiStorniertenUndJahresfremdenBuchungen() {
        var b=booking(1,2027,Buchungsstatus.STORNIERT,"1000");
        var d=calc(List.of(move(b,"2025-12-30",Finanzkategorie.ANZAHLUNG,Finanzbewegungsart.EINGANG,"300"),
            move(b,"2026-01-02",Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"500"),
            move(b,"2026-02-01",Finanzkategorie.MIETE,Finanzbewegungsart.RUECKZAHLUNG,"100"),
            move(b,"2026-12-01",Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"900")),List.of());
        amount("400",d.umsatz());amount("300",d.vorjahresumsatz());amount("400",d.buchungen().getFirst().umsatz());
        amount("500",d.monate().get(0).umsatz());amount("-100",d.monate().get(1).umsatz());amount("24600",d.verbleibend());
    }
    @Test void kautionIstKeinUmsatzUndVerrechnungZaehltNurEinmal() {
        var b=booking(1,2026,Buchungsstatus.BESTAETIGT,"1000");
        var d=calc(List.of(move(b,"2026-01-01",Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,"500"),
            move(b,"2026-02-01",Finanzkategorie.KAUTION,Finanzbewegungsart.VERRECHNUNG,"100"),
            move(b,"2026-02-01",Finanzkategorie.VERBRAUCHSKOSTEN,Finanzbewegungsart.VERRECHNUNG,"100"),
            move(b,"2026-02-01",Finanzkategorie.KAUTION,Finanzbewegungsart.RUECKZAHLUNG,"400")),List.of());
        amount("100",d.umsatz());
    }
    @Test void planungZiehtFruehereAnzahlungenAbUndIgnoriertAnfragenStornosUndFremdeJahre() {
        var b=booking(1,2026,Buchungsstatus.RESERVIERT,"1000");
        var d=calc(List.of(move(b,"2025-12-01",Finanzkategorie.ANZAHLUNG,Finanzbewegungsart.EINGANG,"200"),
            move(b,"2026-02-01",Finanzkategorie.ANZAHLUNG,Finanzbewegungsart.EINGANG,"300")),
            List.of(b,booking(2,2026,Buchungsstatus.ANFRAGE,"9000"),booking(3,2026,Buchungsstatus.STORNIERT,"9000"),booking(4,2027,Buchungsstatus.BESTAETIGT,"9000")));
        amount("300",d.umsatz());amount("500",d.geplantOffen());amount("800",d.buchungsprognose());amount("800",d.prognose());
    }
    @Test void rabattUndAbgerechneterVerbrauchWerdenInPlanungBeruecksichtigt() {
        var b=booking(1,2026,Buchungsstatus.BESTAETIGT,"1000");b.setRabattart(Rabattart.INDIVIDUELL);
        b.setRabattwertTyp(RabattwertTyp.ABSOLUT);b.setRabattwert(new BigDecimal("100"));
        var cost=new BuchungVerbrauchskosten();cost.setBuchung(b);cost.setKosten(new BigDecimal("40"));
        var d=FinanzDashboardService.calculate(2026,today,limit,List.of(),List.of(b),List.of(cost));
        amount("940",d.geplantOffen());
    }
    @Test void trendNutztTatsaechlicheKalendertageUndDenHoeherenPrognosewert() {
        var b=booking(1,2026,Buchungsstatus.BESTAETIGT,"100");
        var d=calc(List.of(move(b,"2026-01-02",Finanzkategorie.MIETE,Finanzbewegungsart.EINGANG,"100")),List.of(b));
        amount("200.55",d.trendprognose());amount("200.55",d.prognose());amount("0",d.geplantOffen());
    }
    @Test void vergangeneUndZukuenftigeJahreHabenKeineLineareHochrechnung() {
        var b=booking(1,2026,Buchungsstatus.BESTAETIGT,"1000");
        var past=FinanzDashboardService.calculate(2026,LocalDate.of(2027,1,1),limit,List.of(),List.of(b),List.of());
        assertNull(past.trendprognose());amount("0",past.prognose());
        var future=FinanzDashboardService.calculate(2026,LocalDate.of(2025,12,1),limit,List.of(),List.of(b),List.of());
        assertNull(future.trendprognose());amount("1000",future.prognose());
    }
    @Test void negativeEinnahmenUndLeereDatenBleibenNachvollziehbar() {
        var b=booking(1,2026,Buchungsstatus.STORNIERT,"1000");
        var d=calc(List.of(move(b,"2026-01-02",Finanzkategorie.MIETE,Finanzbewegungsart.RUECKZAHLUNG,"500")),List.of());
        amount("-500",d.umsatz());amount("25500",d.verbleibend());
        var empty=calc(List.of(),List.of());amount("0",empty.umsatz());assertEquals(12,empty.monate().size());assertTrue(empty.buchungen().isEmpty());
    }
    @Test void unbefristeteBuchungenWerdenNichtAlsEinmalbetragHochgerechnet() {
        var b=booking(1,2026,Buchungsstatus.BESTAETIGT,"1000");b.setAbreise(LocalDate.of(3000,12,31).atTime(14,0));
        var d=calc(List.of(),List.of(b));assertEquals(1,d.unbefristeteBuchungen());amount("0",d.geplantOffen());
    }
    @Test void verbrauchszahlungenMindernNichtDieNochOffeneMiete() {
        var b=booking(1,2026,Buchungsstatus.BESTAETIGT,"1000");
        var d=calc(List.of(move(b,"2026-02-01",Finanzkategorie.VERBRAUCHSKOSTEN,Finanzbewegungsart.EINGANG,"100")),List.of(b));
        amount("1000",d.geplantOffen());amount("1100",d.buchungsprognose());
    }
    @Test void grenzeMussPositivSeinUndDarfNurZweiNachkommastellenHaben() {
        try(var factory=jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var validator=factory.getValidator();
            for(String value:List.of("0","-1","25.001","10000000000"))
                assertFalse(validator.validate(new FinanzDashboardEinstellungDTO(new BigDecimal(value))).isEmpty());
            assertTrue(validator.validate(new FinanzDashboardEinstellungDTO(new BigDecimal("30000.50"))).isEmpty());
        }
    }
    @Test void einstellbareGrenzeWirdJahresbezogenGespeichertUndUngueltigeJahreAbgelehnt() {
        var settings=mock(FinanzDashboardJahrRepository.class);var moves=mock(BuchungFinanzbewegungRepository.class);
        var books=mock(BuchungRepository.class);var costs=mock(BuchungVerbrauchskostenRepository.class);
        var saved=new FinanzDashboardJahr();saved.setJahr(2026);saved.setUmsatzgrenze(new BigDecimal("30000"));
        when(settings.findById(2026)).thenReturn(Optional.of(saved));
        var service=new FinanzDashboardService(moves,books,costs,settings);
        amount("30000",service.save(2026,new FinanzDashboardEinstellungDTO(new BigDecimal("30000"))).umsatzgrenze());
        verify(settings).save(saved);assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.get(1999));
    }
}
