package com.kcserver.unit;
import com.kcserver.vermietung.dto.BuchungDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BuchungBankauswahlTest {
    private Mietobjekt objekt;
    private BuchungFinanzbewegungRepository bewegungen;
    private BuchungService service;
    private BuchungRepository buchungen;
    private BuchungsnummerCounterRepository counters;
    private MieterRepository mieter;
    private com.kcserver.kjfp.repository.PersonRepository personen;
    @BeforeEach void setup() {
        bewegungen=mock(BuchungFinanzbewegungRepository.class);
        buchungen=mock(BuchungRepository.class); counters=mock(BuchungsnummerCounterRepository.class);
        personen=mock(com.kcserver.kjfp.repository.PersonRepository.class); mieter=mock(MieterRepository.class);
        var objekte=mock(MietobjektRepository.class);
        var person=new com.kcserver.core.entity.Person();person.setId(1L);when(personen.findLockedById(1L)).thenReturn(Optional.of(person));
        objekt=new Mietobjekt();objekt.setId(7L);objekt.setAnreisezeit(LocalTime.of(16,0));objekt.setAbreisezeit(LocalTime.of(14,0));
        when(objekte.findById(7L)).thenReturn(Optional.of(objekt));
        Map<Integer,BuchungsnummerCounter> map=new HashMap<>();
        when(counters.findByJahr(anyInt())).thenAnswer(i->Optional.ofNullable(map.get(i.getArgument(0))));
        when(counters.save(any())).thenAnswer(i->{BuchungsnummerCounter c=i.getArgument(0);map.put(c.getJahr(),c);return c;});
        when(buchungen.saveAndFlush(any())).thenAnswer(i->{Buchung b=i.getArgument(0);b.setId(1L);return b;});
        service=new BuchungService(buchungen,mieter,counters,personen,mock(com.kcserver.core.repository.VereinRepository.class),objekte,
            mock(MietbereichRepository.class),mock(MietpreisRepository.class),mock(MietbereichRabattRepository.class),mock(ZaehlerRepository.class),mock(ZaehlerstandRepository.class),
            mock(VerbrauchsabrechnungService.class),mock(BuchungVerbrauchskostenRepository.class),bewegungen, mock(com.kcserver.vermietung.service.HomepageStatusMailService.class));
    }

    private BuchungDTO eingabe() {
        var dto=new BuchungDTO();dto.setMieterId(1L);dto.setMietobjektId(7L);
        dto.setAnreise(LocalDateTime.of(2028,6,10,16,0));dto.setAbreise(LocalDateTime.of(2028,6,12,14,0));dto.setPositionen(List.of());return dto;
    }
    @Test void anlegenAendernWiederlesenUndStornosperre() {
        objekt.setBankverbindungAlternativ(new Bankverbindung("Verein", "Bank", "DE89370400440532013000", "COBADEFFXXX"));
        var dto=eingabe();assertEquals(BankverbindungAuswahl.PRIMAER,service.create(dto).getBankverbindungAuswahl());
        var captor=org.mockito.ArgumentCaptor.forClass(Buchung.class);verify(buchungen).saveAndFlush(captor.capture());
        var gespeichert=captor.getValue();when(buchungen.findLockedById(1L)).thenReturn(Optional.of(gespeichert));
        when(buchungen.findById(1L)).thenReturn(Optional.of(gespeichert));
        dto.setBankverbindungAuswahl(BankverbindungAuswahl.ALTERNATIV);
        assertEquals(BankverbindungAuswahl.ALTERNATIV,service.update(1L,dto).getBankverbindungAuswahl());
        assertEquals(BankverbindungAuswahl.ALTERNATIV,service.getById(1L).getBankverbindungAuswahl());
        dto.setBankverbindungAuswahl(null);
        assertEquals(BankverbindungAuswahl.ALTERNATIV,service.update(1L,dto).getBankverbindungAuswahl());
        gespeichert.setStatus(Buchungsstatus.STORNIERT);dto.setBankverbindungAuswahl(BankverbindungAuswahl.PRIMAER);
        assertThrows(IllegalArgumentException.class,()->service.update(1L,dto));
    }
    @Test void optionaleVeranstaltungSpeichernAendernWiederlesenUndLeeren() {
        var dto = eingabe();
        dto.setVeranstaltung("Kanutour");
        assertEquals("Kanutour", service.create(dto).getVeranstaltung());
        var captor = org.mockito.ArgumentCaptor.forClass(Buchung.class);
        verify(buchungen).saveAndFlush(captor.capture());
        var gespeichert = captor.getValue();
        when(buchungen.findLockedById(1L)).thenReturn(Optional.of(gespeichert));
        when(buchungen.findById(1L)).thenReturn(Optional.of(gespeichert));
        assertEquals("Kanutour", service.getById(1L).getVeranstaltung());
        dto.setVeranstaltung("Sommerfest");
        assertEquals("Sommerfest", service.update(1L, dto).getVeranstaltung());
        assertEquals("Sommerfest", service.getById(1L).getVeranstaltung());
        dto.setVeranstaltung("");
        assertEquals("", service.update(1L, dto).getVeranstaltung());
        assertEquals("", service.getById(1L).getVeranstaltung());
        dto.setVeranstaltung(null);
        assertNull(service.update(1L, dto).getVeranstaltung());
        assertNull(service.getById(1L).getVeranstaltung());
    }

    @Test void alternativOhneKontoSpeichertKeineBuchung() {
        var dto=eingabe();dto.setBankverbindungAuswahl(BankverbindungAuswahl.ALTERNATIV);
        assertThrows(IllegalArgumentException.class,()->service.create(dto));
        verify(buchungen,never()).saveAndFlush(any());
    }
    @Test void kostenloseVarianteWirdGespeichertUndAnzahlungSowieKautionWerdenNull() {
        var dto=eingabe();dto.setZahlungsvariante(Zahlungsvariante.OHNE_ABRECHNUNG);
        dto.setAnzahlungBetrag(new java.math.BigDecimal("100"));dto.setKautionBetrag(new java.math.BigDecimal("200"));
        var result=service.create(dto);
        assertEquals(Zahlungsvariante.OHNE_ABRECHNUNG,result.getZahlungsvariante());
        assertEquals(0,result.getAnzahlungBetrag().signum());assertEquals(0,result.getKautionBetrag().signum());
        var captor=org.mockito.ArgumentCaptor.forClass(Buchung.class);verify(buchungen).saveAndFlush(captor.capture());
        var b=captor.getValue();when(buchungen.findLockedById(1L)).thenReturn(Optional.of(b));when(buchungen.findById(1L)).thenReturn(Optional.of(b));
        assertEquals(Zahlungsvariante.OHNE_ABRECHNUNG,service.getById(1L).getZahlungsvariante());
        dto.setZahlungsvariante(Zahlungsvariante.BEI_ABRECHNUNG);service.update(1L,dto);
        dto.setZahlungsvariante(Zahlungsvariante.OHNE_ABRECHNUNG);
        assertEquals(0,service.update(1L,dto).getKautionBetrag().signum());
    }
    @Test void wechselZuKostenlosMitNochNichtErstatteterZahlungWirdAbgelehnt() {
        var dto=eingabe();service.create(dto);
        var captor=org.mockito.ArgumentCaptor.forClass(Buchung.class);verify(buchungen).saveAndFlush(captor.capture());
        var b=captor.getValue();when(buchungen.findLockedById(1L)).thenReturn(Optional.of(b));when(buchungen.findById(1L)).thenReturn(Optional.of(b));
        var zahlung=new BuchungFinanzbewegung();zahlung.setArt(Finanzbewegungsart.EINGANG);zahlung.setKategorie(Finanzkategorie.MIETE);zahlung.setBetrag(new java.math.BigDecimal("100"));
        when(bewegungen.findByBuchungIdOrderByDatumAscIdAsc(1L)).thenReturn(List.of(zahlung));
        dto.setZahlungsvariante(Zahlungsvariante.OHNE_ABRECHNUNG);
        assertThrows(IllegalArgumentException.class,()->service.update(1L,dto));
        assertNotEquals(Zahlungsvariante.OHNE_ABRECHNUNG,b.getZahlungsvariante());
    }

    @Test void fruehereNummerBleibtMetadatumUndIstAuchBeiStornoAenderbar() {
        var dto=eingabe();dto.setFruehereBuchungsnummer("ALT-4711");var result=service.create(dto);
        assertEquals("ALT-4711",result.getFruehereBuchungsnummer());String nummer=result.getBuchungsnummer();
        var captor=org.mockito.ArgumentCaptor.forClass(Buchung.class);verify(buchungen).saveAndFlush(captor.capture());var b=captor.getValue();
        when(buchungen.findLockedById(1L)).thenReturn(Optional.of(b));when(buchungen.save(any())).thenAnswer(i->i.getArgument(0));
        b.setStatus(Buchungsstatus.STORNIERT);dto.setFruehereBuchungsnummer("ALT-9999");result=service.update(1L,dto);
        assertEquals("ALT-9999",result.getFruehereBuchungsnummer());assertEquals(nummer,result.getBuchungsnummer());assertEquals(Buchungsstatus.STORNIERT,result.getStatus());
        verify(counters,times(1)).findByJahr(anyInt());
    }

}
