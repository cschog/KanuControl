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
        service = new HomepageIntegrationService(configs, anfragen, buchungen, buchungService, mieterService, mapper, factory.getValidator(), org.mockito.Mockito.mock(com.kcserver.core.repository.VereinRepository.class));
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
    @Test void veranstalterErsetztMieterbankUndVerlangtVollstaendigeDaten() throws Exception {
        var data=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.valueToTree(request(UUID.randomUUID(),2L,"Name"));
        for(var key:List.of("bankName","iban","bic","kontoinhaber"))data.remove(key);
        var v=data.putObject("veranstalterDaten");
        v.put("name","Testverein");v.put("strasse","Testweg 1");v.put("plz","12345");v.put("ort","Testort");v.put("countryCode","DE");
        v.put("bankName","Testbank");v.put("iban","DE89370400440532013000");v.put("bic","COBADEFFXXX");
        assertTrue(factory.getValidator().validate(mapper.treeToValue(data,Anfrage.class)).isEmpty());
        v.put("iban","DE00000000000000000000");
        assertFalse(factory.getValidator().validate(mapper.treeToValue(data,Anfrage.class)).isEmpty());
        v.remove("iban");
        assertFalse(factory.getValidator().validate(mapper.treeToValue(data,Anfrage.class)).isEmpty());
        data.remove("veranstalterDaten");
        assertFalse(factory.getValidator().validate(mapper.treeToValue(data,Anfrage.class)).isEmpty());
    }
    private Anfrage organisationsAnfrage() throws Exception {
        var data=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.valueToTree(request(UUID.randomUUID(),2L,"Name"));
        var v=data.putObject("veranstalterDaten");
        v.put("name","Testverein");v.put("strasse","Testweg 1");v.put("plz","12345");v.put("ort","Testort");v.put("countryCode","DE");
        v.put("bankName","Testbank");v.put("iban","DE89370400440532013000");v.put("bic","COBADEFFXXX");
        return mapper.treeToValue(data,Anfrage.class);
    }
    private com.kcserver.core.repository.VereinRepository vereine() {
        return (com.kcserver.core.repository.VereinRepository)org.springframework.test.util.ReflectionTestUtils.getField(service,"veranstalterRepository");
    }
    private com.kcserver.core.entity.Verein passenderVerein() {
        var v=new com.kcserver.core.entity.Verein();v.setId(77L);v.setKjfpVerein(true);v.setName("Testverein");
        v.setStrasse("Testweg 1");v.setPlz("12345");v.setOrt("Testort");v.setCountryCode(com.kcserver.kjfp.enumtype.CountryCode.DE);
        v.setBankName("Testbank");v.setIban("DE89370400440532013000");v.setBic("COBADEFFXXX");return v;
    }
    private void erfolgreicheBuchung() {
        var m=new MieterDTO();m.setPersonId(55L);when(mieterService.create(any())).thenReturn(m);
        var b=new BuchungDTO();b.setId(99L);b.setBuchungsnummer("2026-099");when(buchungService.create(any())).thenReturn(b);
        when(buchungen.findById(99L)).thenReturn(Optional.of(new Buchung()));
        when(vereine().save(any())).thenAnswer(i->{var v=(com.kcserver.core.entity.Verein)i.getArgument(0);v.setId(78L);return v;});
    }
    @Test void vorhandenerKjfpVeranstalterWirdNurBeiVollstaendigerUebereinstimmungWiederverwendet() throws Exception {
        erfolgreicheBuchung();var v=passenderVerein();when(vereine().findAll()).thenReturn(List.of(v));
        service.anfrage(7L,"verein-homepage",organisationsAnfrage());
        var booking=ArgumentCaptor.forClass(BuchungDTO.class);verify(buchungService).create(booking.capture());
        assertEquals(77L,booking.getValue().getVeranstalterVereinId());verify(vereine(),never()).save(any());
        assertTrue(v.isKjfpVerein());assertEquals("Testverein",v.getName());
        var person=ArgumentCaptor.forClass(MieterSaveDTO.class);verify(mieterService).create(person.capture());
        assertNull(person.getValue().getIban());assertNull(person.getValue().getBankName());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"name","strasse","plz","ort","countryCode","iban","bankName","bic","merged"})
    void abweichenderOderZusammengefuehrterVereinWirdNichtUeberschrieben(String field) throws Exception {
        erfolgreicheBuchung();var existing=passenderVerein();
        switch(field){
            case "name"->existing.setName("Anderer Verein");case "strasse"->existing.setStrasse("Andere Straße");
            case "plz"->existing.setPlz("99999");case "ort"->existing.setOrt("Anderer Ort");
            case "countryCode"->existing.setCountryCode(com.kcserver.kjfp.enumtype.CountryCode.FR);
            case "iban"->existing.setIban("DE12500105170648489890");case "bankName"->existing.setBankName("Andere Bank");
            case "bic"->existing.setBic("DEUTDEFFXXX");case "merged"->existing.setVermietungZusammengefuehrtIn(123L);
            default->throw new AssertionError(field);
        }
        when(vereine().findAll()).thenReturn(List.of(existing));
        service.anfrage(7L,"verein-homepage",organisationsAnfrage());
        var created=ArgumentCaptor.forClass(com.kcserver.core.entity.Verein.class);verify(vereine()).save(created.capture());
        assertNotSame(existing,created.getValue());assertFalse(created.getValue().isKjfpVerein());
        assertEquals("Testverein",created.getValue().getName());assertEquals("DE89370400440532013000",created.getValue().getIban());
        var booking=ArgumentCaptor.forClass(BuchungDTO.class);verify(buchungService).create(booking.capture());
        assertEquals(78L,booking.getValue().getVeranstalterVereinId());
        assertTrue(existing.isKjfpVerein());
    }
    @Test void mehrereExakteVeranstalterTrefferVerhindernBuchungsanlage() throws Exception {
        erfolgreicheBuchung();when(vereine().findAll()).thenReturn(List.of(passenderVerein(),passenderVerein()));
        Anfrage request=organisationsAnfrage();
        var error=assertThrows(ResponseStatusException.class,()->service.anfrage(7L,"verein-homepage",request));
        assertEquals(409,error.getStatusCode().value());verify(buchungService,never()).create(any());
        verify(vereine(),never()).save(any());verify(anfragen,never()).saveAndFlush(any());
    }
    @AfterEach void close() { factory.close(); }
    private Anfrage request(UUID id, long version, String name) {
        return new Anfrage(id, version, auswahl, "Test", name, "gast@example.org", "0123456789",
            "Teststraße 1", "12345", "Testort", "DE", "Schule", "Bitte prüfen", 8, 12, 16, 2, true, true, "Musterbank", "DE89370400440532013000", "COBADEFFXXX", "Test Name");
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
        assertEquals("RESERVIERT", answer.status()); assertEquals("HOMEPAGE", entity.getHerkunft());
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
        assertEquals("Test Name",kontakt.getValue().getKontoinhaber());
    }
    @Test void alleVierBankfelderSindPflichtOhneNebenwirkungen() throws Exception {
        for(var key:List.of("bankName","iban","bic","kontoinhaber"))for(var value:List.of("", "   ")) {
            var data=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.valueToTree(request(UUID.randomUUID(),2L,"Name"));
            data.put(key,value);
            assertThrows(IllegalArgumentException.class,()->service.anfrage(7L,"verein-homepage",mapper.treeToValue(data,Anfrage.class)));
            data.remove(key);
            assertThrows(IllegalArgumentException.class,()->service.anfrage(7L,"verein-homepage",mapper.treeToValue(data,Anfrage.class)));
        }
        verifyNoInteractions(mieterService,buchungService,anfragen);
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
        var source=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.valueToTree(request(UUID.randomUUID(),2L,"Name"));
        for(var key:List.of("bankName","iban","bic","kontoinhaber"))source.remove(key);
        var legacy=mapper.treeToValue(source,Anfrage.class);var json=mapper.writeValueAsString(legacy);
        assertFalse(json.contains("bankName"));assertFalse(json.contains("iban"));assertFalse(json.contains("bic"));
        assertEquals(legacy,mapper.readValue(json,Anfrage.class));
        var data=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.readTree(json);
        data.put("bankName","");data.put("iban","");data.put("bic","");
        assertEquals(json,mapper.writeValueAsString(mapper.treeToValue(data,Anfrage.class)));
    }

}
