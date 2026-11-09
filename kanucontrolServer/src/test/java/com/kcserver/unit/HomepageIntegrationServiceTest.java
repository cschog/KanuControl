package com.kcserver.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.dto.HomepageIntegrationDTO.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomepageIntegrationServiceTest {
    private ValidatorFactory factory;
    private HomepageAnbindungRepository configs;
    private HomepageAnfrageRepository anfragen;
    private BuchungRepository buchungen;
    private BuchungService buchungService;
    private MieterService mieterService;
    private HomepageIntegrationService service;
    private HomepageAnbindung config;
    private ObjectMapper mapper;
    private Auswahl auswahl;
    @BeforeEach void setup() {
        factory = Validation.buildDefaultValidatorFactory(); mapper = new ObjectMapper().findAndRegisterModules();
        configs = mock(HomepageAnbindungRepository.class); anfragen = mock(HomepageAnfrageRepository.class);
        buchungen = mock(BuchungRepository.class); buchungService = mock(BuchungService.class); mieterService = mock(MieterService.class);
        service = new HomepageIntegrationService(configs, anfragen, buchungen, buchungService, mieterService, mapper, factory.getValidator());
        Mietobjekt objekt = new Mietobjekt(); objekt.setId(7L); objekt.setBezeichnung("Bootshaus");
        objekt.setAnreisezeit(LocalTime.of(16, 0)); objekt.setAbreisezeit(LocalTime.of(14, 0));
        config = new HomepageAnbindung(); config.setMietobjektId(7L); config.setMietobjekt(objekt);
        config.setAktiv(true); config.setTechnischerClientId("verein-homepage"); config.setVersion(2L);
        config.setDatenschutzUrl("https://verein.example/datenschutz"); config.setHausregelnUrl("https://verein.example/regeln");
        config.setErlaubteMietbereichIds(new HashSet<>(Set.of(10L)));
        config.setEmpfaengerEmail("buchung@verein.example"); config.setVerwaltungsBenachrichtigung(true);
        when(configs.findById(7L)).thenReturn(Optional.of(config)); when(configs.lockById(7L)).thenReturn(Optional.of(config));
        when(anfragen.findByMietobjektIdAndAnfrageId(any(), any())).thenReturn(Optional.empty());
        LocalDate start = LocalDate.now(ZoneId.of("Europe/Berlin")).plusDays(30);
        auswahl = new Auswahl(start, start.plusDays(2), List.of(new Position(10L, 1)));
    }
    @AfterEach void close() { factory.close(); }
    private Anfrage request(UUID id, long version, String name) {
        return new Anfrage(id, version, auswahl, "Test", name, "gast@example.org", "0123456789",
            "Teststraße 1", "12345", "Testort", "DE", "Schule", "Bitte prüfen", 8, 12, 16, 2, true, true, null, null, null);
    }
    @Test void deaktivierteOderFremdeAnbindungHatKeinenZugriff() {
        assertThrows(ResponseStatusException.class, () -> service.katalog(7L, "anderer-client"));
        assertThrows(ResponseStatusException.class, () -> service.anfrage(7L, null, request(UUID.randomUUID(), 2L, "Name")));
        config.setAktiv(false);
        assertThrows(ResponseStatusException.class, () -> service.verfuegbarkeit(7L, "verein-homepage", auswahl));
        verifyNoInteractions(buchungService, mieterService, anfragen);
    }
    @Test void katalogEnthaeltKeineInternenMailAdressenOderZugangsdaten() throws Exception {
        Mietbereich b = new Mietbereich(); b.setId(10L); b.setBezeichnung("EG"); config.getMietobjekt().getMietbereiche().add(b);
        String json = mapper.writeValueAsString(service.katalog(7L, "verein-homepage"));
        assertTrue(json.contains("EG")); assertFalse(json.contains("buchung@")); assertFalse(json.contains("verein-homepage"));
    }
    @Test void auswahlIstAufFreigabenBeschraenkt() {
        var andere = new Auswahl(auswahl.anreise(), auswahl.abreise(), List.of(new Position(999L, 1)));
        assertThrows(IllegalArgumentException.class, () -> service.verfuegbarkeit(7L, "verein-homepage", andere));
        verifyNoInteractions(buchungService);
    }
    @Test void belegungskonfliktLiefertVerstaendlicheAntwort() {
        doThrow(new IllegalArgumentException("Bereich bereits belegt")).when(buchungService)
            .pruefeHomepageVerfuegbarkeit(eq(7L), any(), any(), any());
        var answer = service.verfuegbarkeit(7L, "verein-homepage", auswahl);
        assertFalse(answer.verfuegbar()); assertEquals("Bereich bereits belegt", answer.hinweis());
    }
    @Test void geaenderteHinweiseVerhindernNeueAnfrage() {
        var ex = assertThrows(ResponseStatusException.class, () -> service.anfrage(7L, "verein-homepage", request(UUID.randomUUID(), 1L, "Name")));
        assertEquals(409, ex.getStatusCode().value()); verifyNoInteractions(mieterService, buchungService);
    }
    @Test void ablehnungDerVerfuegbarkeitErzeugtKeinePerson() {
        doThrow(new IllegalArgumentException("Belegt")).when(buchungService).pruefeHomepageVerfuegbarkeit(any(), any(), any(), any());
        assertThrows(IllegalArgumentException.class, () -> service.anfrage(7L, "verein-homepage", request(UUID.randomUUID(), 2L, "Name")));
        verifyNoInteractions(mieterService); verify(buchungService, never()).create(any());
    }
    @Test void neueAnfrageSpeichertHerkunftKontaktUndReplayZusammen() {
        MieterDTO mieter = new MieterDTO(); mieter.setPersonId(55L); when(mieterService.create(any())).thenReturn(mieter);
        BuchungDTO dto = new BuchungDTO(); dto.setId(99L); dto.setBuchungsnummer("2026-042"); when(buchungService.create(any())).thenReturn(dto);
        Buchung entity = new Buchung(); when(buchungen.findById(99L)).thenReturn(Optional.of(entity));
        UUID id = UUID.randomUUID(); var answer = service.anfrage(7L, "verein-homepage", request(id, 2L, "Name"));
        assertEquals("ANFRAGE", answer.status()); assertEquals("HOMEPAGE", entity.getHerkunft());
        var saved = ArgumentCaptor.forClass(BuchungDTO.class); verify(buchungService).create(saved.capture());
        assertEquals(55L, saved.getValue().getMieterId()); assertEquals(8, saved.getValue().getAnzahlJugendliche());
        assertTrue(saved.getValue().getBemerkung().contains("Schule"));
        var replay = ArgumentCaptor.forClass(HomepageAnfrage.class); verify(anfragen).saveAndFlush(replay.capture());
        assertEquals(id.toString(), replay.getValue().getAnfrageId());
        assertTrue(replay.getValue().getHinweisStand().contains("https://verein.example/regeln"));
        assertNotNull(replay.getValue().getCreatedAt());
        when(anfragen.findByMietobjektIdAndAnfrageId(7L, id.toString())).thenReturn(Optional.of(replay.getValue()));
        assertEquals(answer, service.anfrage(7L, "verein-homepage", request(id, 2L, "Name")));
        verify(mieterService, times(1)).create(any()); verify(buchungService, times(1)).create(any());
        assertThrows(ResponseStatusException.class, () -> service.anfrage(7L, "verein-homepage", request(id, 2L, "Andere Angaben")));
    }
    @Test void nichtBestaetigteHinweiseVerhindernUebernahme() throws Exception {
        for (String key : List.of("hausregelnAkzeptiert", "datenschutzGelesen")) {
            var json = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.valueToTree(request(UUID.randomUUID(), 2L, "Name"));
            json.put(key, false);
            Anfrage dto = mapper.treeToValue(json, Anfrage.class);
            assertThrows(IllegalArgumentException.class, () -> service.anfrage(7L, "verein-homepage", dto));
        }
        verifyNoInteractions(mieterService, buchungService);
    }
    @Test void unvollstaendigeTeilnehmerdatenVerhindernUebernahme() throws Exception {
        var json = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.valueToTree(request(UUID.randomUUID(), 2L, "Name"));
        json.put("jugendAlterVon", 17); json.put("jugendAlterBis", 12);
        Anfrage dto = mapper.treeToValue(json, Anfrage.class);
        assertThrows(IllegalArgumentException.class, () -> service.anfrage(7L, "verein-homepage", dto));
        json.putNull("anzahlJugendliche"); json.putNull("jugendAlterVon"); json.putNull("jugendAlterBis");
        Anfrage fehlend = mapper.treeToValue(json, Anfrage.class);
        assertThrows(IllegalArgumentException.class, () -> service.anfrage(7L, "verein-homepage", fehlend));
        verifyNoInteractions(mieterService, buchungService);
    }
    @Test void ungueltigeKontaktdatenErzeugenKeineAnfrage() {
        assertThrows(IllegalArgumentException.class, () -> service.anfrage(7L, "verein-homepage", request(UUID.randomUUID(), 2L, "")));
        verifyNoInteractions(mieterService, buchungService);
    }
    @Test void kalenderIstAufMonatUndFreigegebeneBereicheBegrenzt() {
        Mietbereich b = new Mietbereich(); b.setId(10L); config.getMietobjekt().getMietbereiche().add(b);
        assertThrows(ResponseStatusException.class, () -> service.kalender(7L,"anderer-client",new KalenderAuswahl("2027-07",10L)));
        assertThrows(ResponseStatusException.class, () -> service.kalender(7L,"verein-homepage",new KalenderAuswahl("2027-07",99L)));
        assertThrows(IllegalArgumentException.class, () -> service.kalender(7L,"verein-homepage",new KalenderAuswahl("2027-13",10L)));
        assertThrows(IllegalArgumentException.class, () -> service.kalender(7L,"verein-homepage",new KalenderAuswahl("9999-01",10L)));
        service.kalender(7L,"verein-homepage",new KalenderAuswahl("2027-07",10L));
        verify(buchungService).homepageKalender(config.getMietobjekt(),b,YearMonth.of(2027,7),Set.of(10L));
    }
    @Test void bankverbindungWirdNormalisiertUndAmNeuenMieterGespeichert() throws Exception {
        var data=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.valueToTree(request(UUID.randomUUID(),2L,"Name"));
        data.put("bankName"," Erika Musterfrau ");data.put("iban","de89 3704 0044 0532 0130 00");data.put("bic","cobadeffxxx");
        var dto=mapper.treeToValue(data,Anfrage.class);
        var mieter=new MieterDTO();mieter.setPersonId(55L);when(mieterService.create(any())).thenReturn(mieter);
        var saved=new BuchungDTO();saved.setId(99L);saved.setBuchungsnummer("2026-042");when(buchungService.create(any())).thenReturn(saved);
        when(buchungen.findById(99L)).thenReturn(Optional.of(new Buchung()));
        service.anfrage(7L,"verein-homepage",dto);
        var kontakt=ArgumentCaptor.forClass(com.kcserver.vermietung.dto.MieterSaveDTO.class);verify(mieterService).create(kontakt.capture());
        assertEquals("Erika Musterfrau",kontakt.getValue().getBankName());assertEquals("DE89370400440532013000",kontakt.getValue().getIban());
        assertEquals("COBADEFFXXX",kontakt.getValue().getBic());
    }
    @Test void ungueltigeBankangabenWerdenVorKontaktUndBuchungAbgewiesen() throws Exception {
        for(var key:List.of("iban","bic")) {
            var data=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.valueToTree(request(UUID.randomUUID(),2L,"Name"));data.put(key,"ungueltig");
            var dto=mapper.treeToValue(data,Anfrage.class);
            assertThrows(IllegalArgumentException.class,()->service.anfrage(7L,"verein-homepage",dto));
        }
        verifyNoInteractions(mieterService,buchungService,anfragen);
    }
    @Test void alteAnfragenOhneBankangabenBehaltenIhrJsonUndDamitDenReplayHash() throws Exception {
        var legacy=request(UUID.randomUUID(),2L,"Name");var json=mapper.writeValueAsString(legacy);
        assertFalse(json.contains("bankName"));assertFalse(json.contains("iban"));assertFalse(json.contains("bic"));
        assertEquals(legacy,mapper.readValue(json,Anfrage.class));
        var data=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.readTree(json);
        data.put("bankName","");data.put("iban","");data.put("bic","");
        assertEquals(json,mapper.writeValueAsString(mapper.treeToValue(data,Anfrage.class)));
    }

}
