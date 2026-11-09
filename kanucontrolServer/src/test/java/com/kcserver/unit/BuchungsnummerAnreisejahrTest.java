package com.kcserver.unit;

import com.kcserver.vermietung.dto.BuchungDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungsnummerAnreisejahrTest {
    private BuchungService service;
    private BuchungRepository buchungen;
    private BuchungsnummerCounterRepository counters;
    private MieterRepository mieter;
    private com.kcserver.kjfp.repository.PersonRepository personen;
    @BeforeEach void setup() {
        buchungen=mock(BuchungRepository.class); counters=mock(BuchungsnummerCounterRepository.class);
        personen=mock(com.kcserver.kjfp.repository.PersonRepository.class); mieter=mock(MieterRepository.class);
        var objekte=mock(MietobjektRepository.class);
        var person=new com.kcserver.core.entity.Person();person.setId(1L);when(personen.findLockedById(1L)).thenReturn(Optional.of(person));
        Mietobjekt objekt=new Mietobjekt();objekt.setId(7L);objekt.setAnreisezeit(LocalTime.of(16,0));objekt.setAbreisezeit(LocalTime.of(14,0));
        when(objekte.findById(7L)).thenReturn(Optional.of(objekt));
        Map<Integer,BuchungsnummerCounter> map=new HashMap<>();
        when(counters.findByJahr(anyInt())).thenAnswer(i->Optional.ofNullable(map.get(i.getArgument(0))));
        when(counters.save(any())).thenAnswer(i->{BuchungsnummerCounter c=i.getArgument(0);map.put(c.getJahr(),c);return c;});
        when(buchungen.saveAndFlush(any())).thenAnswer(i->{Buchung b=i.getArgument(0);b.setId(1L);return b;});
        service=new BuchungService(buchungen,mieter,counters,personen,mock(com.kcserver.core.repository.VereinRepository.class),objekte,
            mock(MietbereichRepository.class),mock(MietpreisRepository.class),mock(MietbereichRabattRepository.class),mock(ZaehlerRepository.class),mock(ZaehlerstandRepository.class),
            mock(VerbrauchsabrechnungService.class),mock(BuchungVerbrauchskostenRepository.class),mock(BuchungFinanzbewegungRepository.class), mock(com.kcserver.vermietung.service.HomepageStatusMailService.class));
    }
    private BuchungDTO neu(int jahr) {
        var dto=new BuchungDTO();dto.setMieterId(1L);dto.setMietobjektId(7L);dto.setAnreise(LocalDateTime.of(jahr,6,10,16,0));dto.setAbreise(LocalDateTime.of(jahr,6,12,14,0));dto.setPositionen(List.of());
        return service.create(dto);
    }
    @Test void jedesAnreisejahrBeginntBeiEinsUndZaehltEigenstaendig() {
        assertEquals("2027-001",neu(2027).getBuchungsnummer());
        assertEquals("2028-001",neu(2028).getBuchungsnummer());
        assertEquals("2027-002",neu(2027).getBuchungsnummer());
    }
    @Test void vorhandeneNummernWerdenAuchOhnePassendenCounterBeruecksichtigt() {
        Buchung alt=new Buchung();alt.setLaufendeNummer(12);when(buchungen.findTopByBuchungsjahrOrderByLaufendeNummerDesc(2027)).thenReturn(Optional.of(alt));
        assertEquals("2027-013",neu(2027).getBuchungsnummer());
    }
    @Test void kjfpPersonWirdBeimAnlegenUndBeimMieterwechselOhnePersonenkopieVerknuepft() {
        neu(2027);
        var captured=org.mockito.ArgumentCaptor.forClass(Buchung.class);
        verify(buchungen).saveAndFlush(captured.capture());
        Buchung gespeichert=captured.getValue();
        assertEquals(1L,gespeichert.getMieter().getId());
        verify(mieter).save(argThat(m->m.getPerson().getId().equals(1L)));
        var anderePerson=new com.kcserver.core.entity.Person(); anderePerson.setId(2L); anderePerson.setKjfpSichtbar(true);
        when(personen.findLockedById(2L)).thenReturn(Optional.of(anderePerson));
        when(buchungen.findLockedById(1L)).thenReturn(Optional.of(gespeichert));
        var dto=new BuchungDTO();dto.setMieterId(2L);dto.setMietobjektId(7L);
        dto.setAnreise(gespeichert.getAnreise());dto.setAbreise(gespeichert.getAbreise());dto.setPositionen(List.of());
        assertEquals(2L,service.update(1L,dto).getMieterId());
        assertSame(anderePerson,gespeichert.getMieter());
        verify(mieter).save(argThat(m->m.getPerson()==anderePerson));
        verify(personen,never()).save(any());
        assertTrue(anderePerson.isKjfpSichtbar());
    }
    @Test void geloeschterNurVermietungsMieterKannNichtAusAltemFormularWiederangelegtWerden() {
        var person=new com.kcserver.core.entity.Person();person.setId(1L);person.setKjfpSichtbar(false);
        when(personen.findLockedById(1L)).thenReturn(Optional.of(person));
        when(mieter.existsByPersonId(1L)).thenReturn(false);
        var e=assertThrows(IllegalArgumentException.class,()->neu(2027));
        assertTrue(e.getMessage().contains("nicht mehr vorhanden"));verify(buchungen,never()).saveAndFlush(any());verify(mieter,never()).save(any());
    }
    @Test void vorhandenerNurVermietungsMieterKannWeiterhinBuchen() {
        var person=new com.kcserver.core.entity.Person();person.setId(1L);person.setKjfpSichtbar(false);
        when(personen.findLockedById(1L)).thenReturn(Optional.of(person));when(mieter.existsByPersonId(1L)).thenReturn(true);
        assertEquals("2027-001",neu(2027).getBuchungsnummer());
    }
    @Test void automatischeVergabeFolgtAufManuellErhoehteNummer() {
        neu(2027);
        var b=new Buchung();b.setId(1L);b.setBuchungsjahr(2027);b.setLaufendeNummer(1);
        when(buchungen.findLockedById(1L)).thenReturn(Optional.of(b));
        new BuchungsnummerService(buchungen,counters).aendern(1L,"2027-099","2027-001");
        assertEquals("2027-100",neu(2027).getBuchungsnummer());
    }

}
