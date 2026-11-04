package kc.isolated;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.VerbrauchDashboardService;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerbrauchDashboardServiceTest {
    @Test void gemeinsamerZaehlerBeruecksichtigtAuchBuchungenAndererZugeordneterObjekte(){
        int year=LocalDate.now(ZoneId.of("Europe/Berlin")).getYear()-1;
        var objects=mock(MietobjektRepository.class);var meters=mock(ZaehlerRepository.class);
        var reads=mock(ZaehlerstandRepository.class);var books=mock(BuchungRepository.class);
        var a=new Mietobjekt();a.setId(1L);a.setBezeichnung("Bootshaus");var b=new Mietobjekt();b.setId(2L);
        var area=new Mietbereich();area.setId(10L);area.setMietobjekt(a);
        var other=new Mietbereich();other.setId(20L);other.setMietobjekt(b);
        var meter=new Zaehler();meter.setId(30L);meter.setBezeichnung("Strom");meter.setVerbrauchsart(Verbrauchsart.STROM);meter.setMengeneinheit("kWh");meter.setMietbereiche(List.of(area,other));
        when(objects.findById(1L)).thenReturn(Optional.of(a));when(meters.findForConsumptionDashboard(1L)).thenReturn(List.of(meter));
        var first=new Zaehlerstand();first.setZaehler(meter);first.setAblesedatum(LocalDateTime.of(year,1,1,0,0));first.setZaehlerstand(BigDecimal.ZERO);
        var last=new Zaehlerstand();last.setZaehler(meter);last.setAblesedatum(LocalDateTime.of(year,1,2,0,0));last.setZaehlerstand(BigDecimal.TEN);
        when(reads.findByZaehlerIdInAndAblesedatumLessThanEqualOrderByAblesedatumAscIdAsc(any(),any())).thenReturn(List.of(first,last));
        var booking=new Buchung();booking.setId(40L);booking.setAnreise(first.getAblesedatum());booking.setAbreise(last.getAblesedatum());booking.setStatus(Buchungsstatus.BESTAETIGT);
        var pos=new BuchungMietbereich();pos.setMietbereich(other);booking.setMietbereiche(List.of(pos));
        when(books.findForConsumptionDashboard(any(),any(),any(),any())).thenReturn(List.of(booking));
        var result=new VerbrauchDashboardService(objects,meters,reads,books).get(1L,year);
        assertTrue(result.zaehler().getFirst().gemeinsam());assertEquals(10,result.zaehler().getFirst().kennzahlen().mitBuchung().doubleValue(),0.00001);
        verify(books).findForConsumptionDashboard(eq(Set.of(1L,2L)),argThat(s->s.contains(Buchungsstatus.RESERVIERT)&&!s.contains(Buchungsstatus.STORNIERT)&&!s.contains(Buchungsstatus.ANFRAGE)),any(),any());
    }
    @Test void unbekannteObjekteUndUngueltigeJahreWerdenAbgewiesen(){
        var objects=mock(MietobjektRepository.class);
        var service=new VerbrauchDashboardService(objects,mock(ZaehlerRepository.class),mock(ZaehlerstandRepository.class),mock(BuchungRepository.class));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.get(1L,1999));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.get(1L,2026));
    }
}
