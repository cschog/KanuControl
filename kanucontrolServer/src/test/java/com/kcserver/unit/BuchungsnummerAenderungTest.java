package com.kcserver.unit;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.BuchungsnummerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungsnummerAenderungTest {
    private final BuchungRepository buchungen=mock(BuchungRepository.class);
    private final BuchungsnummerCounterRepository counters=mock(BuchungsnummerCounterRepository.class);
    private final BuchungsnummerService service=new BuchungsnummerService(buchungen,counters);
    private Buchung b;
    private BuchungsnummerCounter counter;
    @BeforeEach void setup() {
        b=new Buchung();b.setId(26L);b.setBuchungsjahr(2026);b.setLaufendeNummer(22);b.setStatus(Buchungsstatus.BESTAETIGT);
        when(buchungen.findLockedById(26L)).thenReturn(Optional.of(b));
        counter=new BuchungsnummerCounter(2026,22);when(counters.findByJahr(2026)).thenReturn(Optional.of(counter));
        when(counters.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
    }
    @Test void nummerWirdGeaendertUndJahreszaehlerAngehobenOhneStatuswechsel() {
        var result=service.aendern(26L,"2026-099","2026-022");
        assertEquals("2026-099",result.buchungsnummer());assertEquals(26L,result.id());assertEquals(99,counter.getLetzteNummer());
        assertEquals(Buchungsstatus.BESTAETIGT,b.getStatus());verify(buchungen).saveAndFlush(b);
    }
    @Test void niedrigereFreieNummerSenktDenZaehlerNicht() {
        counter.setLetzteNummer(100);
        assertEquals("2026-5",service.aendern(26L,"2026-5","2026-022").buchungsnummer());
        assertEquals(100,counter.getLetzteNummer());
    }
    @Test void nummerKannAuchInEinAnderesJahrVerschobenWerden() {
        assertEquals("2027-015",service.aendern(26L,"2027-015","2026-022").buchungsnummer());
        var captured=org.mockito.ArgumentCaptor.forClass(BuchungsnummerCounter.class);verify(counters).save(captured.capture());
        assertEquals(2027,captured.getValue().getJahr());assertEquals(15,captured.getValue().getLetzteNummer());
        assertEquals(22,counter.getLetzteNummer());
    }
    @Test void doppelteNummerWirdVorAenderungAbgewiesen() {
        when(buchungen.existsByBuchungsnummerAndIdNot("2026-099",26L)).thenReturn(true);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.aendern(26L,"2026-099","2026-022")).getStatusCode().value());
        assertEquals("2026-022",b.getBuchungsnummer());verify(buchungen,never()).saveAndFlush(any());verify(counters,never()).save(any());
    }
    @Test void veraltetesFormularUeberschreibtKeineZwischenzeitlicheAenderung() {
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.aendern(26L,"2026-099","2026-021")).getStatusCode().value());
        assertEquals("2026-022",b.getBuchungsnummer());verifyNoInteractions(counters);verify(buchungen,never()).saveAndFlush(any());
    }
    @ParameterizedTest @ValueSource(strings={"", "ABC", "2026-000", "2026--1", "2026-2147483647", "2026-9999999999", "26-015", "2026-1.5"})
    void ungueltigesFormatWirdOhneDatenbankzugriffAbgewiesen(String nummer) {
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service.aendern(26L,nummer,"2026-022")).getStatusCode().value());
        verifyNoInteractions(buchungen,counters);
    }
    @Test void unveraenderteNummerIstOhneSchreibzugriffMoeglich() {
        assertEquals("2026-022",service.aendern(26L,"2026-022","2026-022").buchungsnummer());
        verifyNoInteractions(counters);verify(buchungen,never()).saveAndFlush(any());
    }
    @Test void gleichzeitigerUniqueKonfliktLiefert409() {
        doThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate")).when(buchungen).saveAndFlush(any());
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.aendern(26L,"2026-099","2026-022")).getStatusCode().value());
    }
    @Test void fuehrendeNullenWerdenUnveraendertGespeichertUndSindEineEchteAenderung() {
        b.setLaufendeNummer(1);b.setBuchungsnummer("2026-001");
        assertEquals("2026-01",service.aendern(26L,"2026-01","2026-001").buchungsnummer());
        assertEquals("2026-01",b.getBuchungsnummer());assertEquals(1,b.getLaufendeNummer());
        verify(buchungen).existsByBuchungsnummerAndIdNot("2026-01",26L);verify(buchungen).saveAndFlush(b);
    }
    @Test void nurExaktGleicherTextWirdAlsDoppelteNummerAbgewiesen() {
        when(buchungen.existsByBuchungsnummerAndIdNot("2026-01",26L)).thenReturn(true);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.aendern(26L,"2026-01","2026-022")).getStatusCode().value());
        assertEquals("2026-001",service.aendern(26L,"2026-001","2026-022").buchungsnummer());
    }

}
