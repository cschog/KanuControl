package com.kcserver.unit;

import com.kcserver.vermietung.dto.BuchungMietbereichDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.BuchungService;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungHomepageVerfuegbarkeitTest {
    private BuchungService service;
    private BuchungRepository buchungen;
    private MietbereichRepository bereiche;
    private Mietbereich eg;
    private LocalDate start;
    @BeforeEach void setup() {
        buchungen = mock(BuchungRepository.class); bereiche = mock(MietbereichRepository.class);
        MietobjektRepository objekte = mock(MietobjektRepository.class);
        service = new BuchungService(buchungen, mock(MieterRepository.class), mock(BuchungsnummerCounterRepository.class),
            mock(com.kcserver.kjfp.repository.PersonRepository.class), mock(com.kcserver.core.repository.VereinRepository.class),
            objekte, bereiche, mock(MietpreisRepository.class), mock(MietbereichRabattRepository.class),
            mock(ZaehlerRepository.class), mock(ZaehlerstandRepository.class), mock(com.kcserver.vermietung.service.VerbrauchsabrechnungService.class),
            mock(BuchungVerbrauchskostenRepository.class), mock(BuchungFinanzbewegungRepository.class), mock(com.kcserver.vermietung.service.HomepageStatusMailService.class));
        Mietobjekt objekt = new Mietobjekt(); objekt.setId(7L); objekt.setAnreisezeit(LocalTime.of(16,0)); objekt.setAbreisezeit(LocalTime.of(14,0));
        when(objekte.findById(7L)).thenReturn(Optional.of(objekt));
        eg = new Mietbereich(); eg.setId(10L); eg.setBezeichnung("EG"); eg.setMietobjekt(objekt); eg.setBestand(1);
        when(bereiche.findAllById(List.of(10L))).thenReturn(List.of(eg));
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of());
        start = LocalDate.now(ZoneId.of("Europe/Berlin")).plusDays(5);
    }
    private BuchungMietbereichDTO position(int menge) {
        var p = new BuchungMietbereichDTO(); p.setMietbereichId(10L); p.setAnzahl(menge); return p;
    }
    @Test void freieAuswahlVerwendetStandardzeitenUndSpeichertNichts() {
        assertDoesNotThrow(() -> service.pruefeHomepageVerfuegbarkeit(7L,start,start.plusDays(2),List.of(position(1))));
        verify(buchungen).findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(
            eq(List.of(com.kcserver.vermietung.enumtype.Buchungsstatus.RESERVIERT,com.kcserver.vermietung.enumtype.Buchungsstatus.BESTAETIGT)),
            eq(start.plusDays(2).atTime(14,0)), eq(start.atTime(16,0)));
        verify(buchungen,never()).save(any());
    }
    @Test void belegteRessourceVerhindertAuswahl() {
        Buchung schon = new Buchung(); schon.setMietobjekt(eg.getMietobjekt());
        BuchungMietbereich p = new BuchungMietbereich(); p.setMietbereich(eg); p.setAnzahl(1); schon.getMietbereiche().add(p);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of(schon));
        assertThrows(IllegalArgumentException.class, () -> service.pruefeHomepageVerfuegbarkeit(7L,start,start.plusDays(2),List.of(position(1))));
    }
    @Test void mengeUndVoraussetzungWerdenGeprueft() {
        assertThrows(IllegalArgumentException.class, () -> service.pruefeHomepageVerfuegbarkeit(7L,start,start.plusDays(2),List.of(position(2))));
        Mietbereich erforderlich = new Mietbereich(); erforderlich.setId(11L); erforderlich.setBezeichnung("Voraussetzung");
        eg.getVoraussetzungenDirekt().add(erforderlich);
        assertThrows(IllegalArgumentException.class, () -> service.pruefeHomepageVerfuegbarkeit(7L,start,start.plusDays(2),List.of(position(1))));
    }
    @Test void doppelteBereicheUndUngueltigerZeitraumWerdenAbgelehnt() {
        assertThrows(IllegalArgumentException.class, () -> service.pruefeHomepageVerfuegbarkeit(7L,start,start.plusDays(2),List.of(position(1),position(1))));
        assertThrows(IllegalArgumentException.class, () -> service.pruefeHomepageVerfuegbarkeit(7L,start,start,List.of(position(1))));
        assertThrows(IllegalArgumentException.class, () -> service.pruefeHomepageVerfuegbarkeit(7L,start.minusDays(100),start,List.of(position(1))));
    }
    private Buchung belegung(Mietbereich bereich, int menge, String status, int von, int bis, int nummer) {
        Buchung b = new Buchung(); b.setMietobjekt(eg.getMietobjekt()); b.setBuchungsjahr(2027); b.setLaufendeNummer(nummer);
        b.setStatus(com.kcserver.vermietung.enumtype.Buchungsstatus.valueOf(status));
        b.setAnreise(LocalDate.of(2027,7,von).atTime(16,0)); b.setAbreise(LocalDate.of(2027,7,bis).atTime(14,0));
        BuchungMietbereich p = new BuchungMietbereich(); p.setMietbereich(bereich); p.setAnzahl(menge); b.getMietbereiche().add(p); return b;
    }
    private com.kcserver.vermietung.dto.HomepageIntegrationDTO.Kalender kalender(Mietbereich b, Set<Long> ids) {
        return service.homepageKalender(eg.getMietobjekt(),b,YearMonth.of(2027,7),ids);
    }
    @Test void kalenderZeigtNummernUndFreienWechseltagMitNurEinerMonatsabfrage() {
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any()))
            .thenReturn(List.of(belegung(eg,1,"BESTAETIGT",10,12,1),belegung(eg,1,"RESERVIERT",13,14,2)));
        var result = kalender(eg,Set.of(10L));
        assertEquals(31,result.tage().size()); assertEquals("BELEGT",result.tage().get(9).status());
        assertEquals(List.of("2027-001"),result.tage().get(9).buchungsnummern());
        assertEquals("FREI",result.tage().get(11).status()); assertEquals("RESERVIERT",result.tage().get(12).status());
        verify(buchungen,times(1)).findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any());
        verify(buchungen,never()).save(any());
    }
    @Test void kalenderLiefertTatsaechlicheZeitenUndWechselAmSelbenTag() {
        var erste = belegung(eg,1,"BESTAETIGT",10,12,1);
        var zweite = belegung(eg,1,"RESERVIERT",12,14,2);
        zweite.setAnreise(LocalDate.of(2027,7,12).atTime(17,30));
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any()))
            .thenReturn(List.of(erste,zweite));
        var tage = kalender(eg,Set.of(10L)).tage();
        assertEquals(LocalDate.of(2027,7,10).atTime(16,0), tage.get(9).belegungen().get(0).von());
        assertEquals(LocalDate.of(2027,7,11).atStartOfDay(), tage.get(9).belegungen().get(0).bis());
        assertEquals(2,tage.get(11).belegungen().size());
        assertEquals(LocalDate.of(2027,7,12).atTime(14,0), tage.get(11).belegungen().get(0).bis());
        assertEquals(LocalDate.of(2027,7,12).atTime(17,30), tage.get(11).belegungen().get(1).von());
        assertEquals("RESERVIERT",tage.get(11).belegungen().get(1).status());
        assertTrue(tage.get(14).belegungen().isEmpty());
    }
    @Test void kalenderErfasstAbreiseAmErstenMonatstag() {
        var b = belegung(eg,1,"BESTAETIGT",1,2,1);
        b.setAnreise(LocalDate.of(2027,6,30).atTime(16,0));
        b.setAbreise(LocalDate.of(2027,7,1).atTime(14,0));
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of(b));
        assertEquals(LocalDate.of(2027,7,1).atTime(14,0),kalender(eg,Set.of(10L)).tage().get(0).belegungen().get(0).bis());
        verify(buchungen).findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),eq(LocalDate.of(2027,7,1).atStartOfDay()));
    }
    @Test void kalenderBeruecksichtigtBestandUndReservierungenGemeinsam() {
        eg.setBestand(3);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any()))
            .thenReturn(List.of(belegung(eg,1,"BESTAETIGT",10,12,1),belegung(eg,1,"RESERVIERT",10,12,2)));
        assertEquals(1,kalender(eg,Set.of(10L)).tage().get(9).verfuegbar());
        eg.setBestand(2); assertEquals("RESERVIERT",kalender(eg,Set.of(10L)).tage().get(9).status());
    }
    @Test void kalenderPrueftVoraussetzungenUndZusammengesetzteRessourcen() {
        Mietbereich og = new Mietbereich(); og.setId(11L); og.setBestand(1); og.getVoraussetzungenDirekt().add(eg);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any()))
            .thenReturn(List.of(belegung(eg,1,"BESTAETIGT",10,12,1)));
        assertEquals("BELEGT",kalender(og,Set.of(10L,11L)).tage().get(9).status());
        assertThrows(IllegalArgumentException.class,()->kalender(og,Set.of(11L)));
        og.getVoraussetzungenDirekt().clear(); og.getBelegteMietbereiche().add(eg);
        assertEquals("BELEGT",kalender(og,Set.of(10L,11L)).tage().get(9).status());
    }
    @Test void kalenderSchliesstFremdeObjekteAusUndErhaeltAbgeschlosseneBelegung() {
        Buchung fremd = belegung(eg,1,"BESTAETIGT",10,12,9); Mietobjekt anderes = new Mietobjekt(); anderes.setId(99L); fremd.setMietobjekt(anderes);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any()))
            .thenReturn(List.of(fremd,belegung(eg,1,"ABGESCHLOSSEN",13,14,2)));
        var result=kalender(eg,Set.of(10L)); assertEquals("FREI",result.tage().get(9).status());
        assertEquals("BELEGT",result.tage().get(12).status()); assertEquals(List.of("2027-002"),result.tage().get(12).buchungsnummern());
    }
    private void pruefeZeiten(LocalDateTime von, LocalDateTime bis, boolean unbefristet) {
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(service,"pruefeBuchungszeiten",eg.getMietobjekt(),von,bis,unbefristet);
    }
    @Test void tagesveranstaltungUndUebernachtungSindErlaubt() {
        LocalDateTime von=LocalDate.of(2027,7,10).atTime(8,0);
        assertDoesNotThrow(()->pruefeZeiten(von,von.plusHours(10),false));
        assertThrows(IllegalArgumentException.class,()->pruefeZeiten(von,von,false));
        assertDoesNotThrow(()->pruefeZeiten(von,von.toLocalDate().plusDays(1).atTime(7,0),false));
    }
    @Test void uebernachtungIstKalendertagwechselUndKeine24StundenFrist() {
        LocalDateTime von=LocalDate.of(2027,3,27).atTime(16,0);
        assertDoesNotThrow(()->pruefeZeiten(von,von.plusDays(1).withHour(14),false));
        assertThrows(IllegalArgumentException.class,()->pruefeZeiten(von,von,false));
        assertDoesNotThrow(()->pruefeZeiten(von,null,true));
    }

    private void sperre(LocalDateTime von, LocalDateTime bis) {
        var s = new com.kcserver.vermietung.entity.MietobjektSperrzeit();
        s.setVon(von); s.setBis(bis); s.setGrund("Interne Wartung");
        eg.getMietobjekt().getSperrzeiten().add(s);
    }
    @Test void sperreVerhindertHomepageAnfrage() {
        sperre(start.atTime(0,0),start.plusDays(1).atTime(0,0));
        var e=assertThrows(IllegalArgumentException.class,()->service.pruefeHomepageVerfuegbarkeit(7L,start,start.plusDays(2),List.of(position(1))));
        assertTrue(e.getMessage().contains("gesperrt"));assertFalse(e.getMessage().contains("Wartung"));
    }
    @Test void sperreBlockiertAuchBestaetigungEinerAelterenAnfrage() {
        var b=belegung(eg,1,"ANFRAGE",10,12,1); b.setId(99L);
        sperre(b.getAnreise(),b.getAbreise());
        when(buchungen.findLockedById(99L)).thenReturn(Optional.of(b));
        assertThrows(IllegalArgumentException.class,()->service.aendereStatus(99L,com.kcserver.vermietung.enumtype.Buchungsstatus.BESTAETIGT));
        assertEquals(com.kcserver.vermietung.enumtype.Buchungsstatus.ANFRAGE,b.getStatus());
    }
    @Test void kalenderZeigtSperreMitUhrzeitOhneInternenGrund() throws Exception {
        sperre(LocalDate.of(2027,7,10).atTime(8,0),LocalDate.of(2027,7,10).atTime(18,0));
        var tag=kalender(eg,Set.of(10L)).tage().get(9);
        assertEquals("GESPERRT",tag.status());assertEquals(0,tag.verfuegbar());
        assertEquals("GESPERRT",tag.belegungen().get(0).status());
        assertEquals(LocalDate.of(2027,7,10).atTime(8,0),tag.belegungen().get(0).von());
        String json=new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules().writeValueAsString(tag);
        assertFalse(json.contains("Wartung"));assertFalse(json.contains("grund"));
    }
    @Test void morgensperreLaesstAnreiseAmNachmittagZu() {
        sperre(start.atTime(0,0),start.atTime(14,0));
        assertDoesNotThrow(()->service.pruefeHomepageVerfuegbarkeit(7L,start,start.plusDays(1),List.of(position(1))));
    }
    @Test void ganztagSperreErfasstAuchAnreiseAmVortag() {
        sperre(start.plusDays(1).atStartOfDay(),start.plusDays(2).atStartOfDay());
        assertThrows(IllegalArgumentException.class,()->service.pruefeHomepageVerfuegbarkeit(7L,start,start.plusDays(1),List.of(position(1))));
    }

    @Test void kalenderGibtFreigegebenenVollstaendigenMieternamenOhneKontaktdatenAus() throws Exception {
        var b=belegung(eg,1,"RESERVIERT",10,12,1);
        var person=new com.kcserver.core.entity.Person();person.setId(555L);person.setVorname("  Erika ");person.setName(" Musterfrau  ");
        person.setEmail("privat@example.invalid");person.setTelefon("privates-telefon");person.setIban("private-bankdaten");b.setMieter(person);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of(b));
        var result=kalender(eg,Set.of(10L));
        assertEquals("Erika Musterfrau",result.tage().get(9).belegungen().get(0).mietername());
        assertEquals("Erika Musterfrau",result.tage().get(11).belegungen().get(0).mietername());
        String json=new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules().writeValueAsString(result);
        assertTrue(json.contains("Erika Musterfrau"));assertFalse(json.contains("privat@example.invalid"));assertFalse(json.contains("privates-telefon"));assertFalse(json.contains("private-bankdaten"));assertFalse(json.contains("personId"));
    }
    @Test void kalenderVertraegtFehlendeNamenUndVeroeffentlichtKeineAnderenMietbereiche() {
        var b=belegung(eg,1,"BESTAETIGT",10,12,1);var person=new com.kcserver.core.entity.Person();person.setName(" Musterfrau ");b.setMieter(person);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of(b));
        assertEquals("Musterfrau",kalender(eg,Set.of(10L)).tage().get(9).belegungen().get(0).mietername());
        person.setName("  ");assertNull(kalender(eg,Set.of(10L)).tage().get(9).belegungen().get(0).mietername());
        var anderer=new Mietbereich();anderer.setId(999L);anderer.setBestand(1);anderer.setMietobjekt(eg.getMietobjekt());
        var fremdeBelegung=belegung(anderer,1,"BESTAETIGT",10,12,2);person.setName("Nicht im EG");fremdeBelegung.setMieter(person);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of(fremdeBelegung));
        assertTrue(kalender(eg,Set.of(10L)).tage().get(9).belegungen().isEmpty());
    }
    @Test void sperrzeitenErhaltenKeinenMieternamen() {
        sperre(LocalDate.of(2027,7,10).atTime(8,0),LocalDate.of(2027,7,10).atTime(18,0));
        var item=kalender(eg,Set.of(10L)).tage().get(9).belegungen().get(0);
        assertEquals("GESPERRT",item.status());assertNull(item.mietername());assertNull(item.buchungsnummer());
    }
}
