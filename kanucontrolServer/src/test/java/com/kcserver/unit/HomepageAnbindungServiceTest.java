package com.kcserver.unit;

import com.kcserver.vermietung.dto.HomepageAnbindungDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.HomepageAnbindungService;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomepageAnbindungServiceTest {
    private ValidatorFactory factory;
    private HomepageAnbindungRepository repository;
    private MietobjektRepository objekte;
    private MietbereichRepository bereiche;
    private HomepageAnbindungService service;
    private Mietobjekt objekt;

    @BeforeEach void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        repository = mock(HomepageAnbindungRepository.class);
        objekte = mock(MietobjektRepository.class);
        bereiche = mock(MietbereichRepository.class);
        service = new HomepageAnbindungService(repository, objekte, bereiche, factory.getValidator());
        objekt = new Mietobjekt();
        objekt.setId(7L);
        when(objekte.findById(7L)).thenReturn(Optional.of(objekt));
        when(repository.findById(7L)).thenReturn(Optional.empty());
        when(bereiche.findAllById(any())).thenReturn(List.of());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            HomepageAnbindung entity = invocation.getArgument(0);
            entity.setVersion(entity.getVersion() == null ? 0L : entity.getVersion() + 1);
            return entity;
        });
    }
    @AfterEach void close() { factory.close(); }

    private Mietbereich bereich(long id) {
        Mietbereich b = new Mietbereich(); b.setId(id); b.setMietobjekt(objekt);
        b.setBezeichnung("Bereich " + id); return b;
    }

    @Test void lesenOhneKonfigurationErzeugtKeineDaten() {
        HomepageAnbindungDTO dto = service.get(7L);
        assertNull(dto.getVersion());
        assertNull(dto.getEmpfaengerEmail());
        assertTrue(dto.getErlaubteMietbereichIds().isEmpty());
        assertFalse(dto.isVerwaltungsBenachrichtigung());
        verify(repository, never()).saveAndFlush(any());
    }
    @Test void unbekanntesObjektWirdNichtAngelegt() {
        assertThrows(IllegalArgumentException.class, () -> service.get(99L));
        assertThrows(IllegalArgumentException.class, () -> service.save(99L, new HomepageAnbindungDTO()));
        verifyNoInteractions(repository);
    }
    @Test void speichernIstVereinsneutralUndNormalisiertUrls() {
        HomepageAnbindungDTO dto = new HomepageAnbindungDTO();
        dto.setBezeichnung("  Vereinsseite  ");
        dto.setHomepageUrl(" https://verein.example/anfrage ");
        dto.setDatenschutzUrl("https://verein.example/datenschutz");
        dto.setHausregelnUrl("https://verein.example/regeln#haus");
        dto.setEmpfaengerEmail("buchung@verein.example");
        dto.setVerwaltungsBenachrichtigung(true);
        dto.setTeilnehmerPflicht(false);
        var saved = service.save(7L, dto);
        assertEquals("Vereinsseite", saved.getBezeichnung());
        assertEquals("https://verein.example/anfrage", saved.getHomepageUrl());
        assertEquals("buchung@verein.example", saved.getEmpfaengerEmail());
        assertTrue(saved.isVerwaltungsBenachrichtigung());
        assertFalse(saved.isTeilnehmerPflicht());
        assertEquals(0L, saved.getVersion());
    }
    @Test void ungueltigeOderUnsichereLinksWerdenAbgelehnt() {
        for (String url : List.of("http://verein.example", "javascript:alert(1)", "/regeln", "https://user:pass@verein.example", "https://")) {
            var dto = new HomepageAnbindungDTO(); dto.setHomepageUrl(url);
            assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto), url);
        }
        verify(repository, never()).saveAndFlush(any());
    }
    @Test void verwaltungsMailBrauchtEineGueltigeAdresse() {
        var dto = new HomepageAnbindungDTO();
        dto.setVerwaltungsBenachrichtigung(true);
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
        dto.setEmpfaengerEmail("keine mail");
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
        dto.setEmpfaengerEmail("a@example.org\r\nBcc:x@example.org");
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
    }
    @Test void ungueltigeBereichIdsWerdenAbgelehnt() {
        var dto = new HomepageAnbindungDTO(); dto.setErlaubteMietbereichIds(Set.of(123L));
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
        dto.setErlaubteMietbereichIds(Set.of(-1L));
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
        dto.setErlaubteMietbereichIds(null);
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
    }
    @Test void bereichEinesAnderenObjektsWirdAbgelehnt() {
        Mietbereich b = bereich(10L); Mietobjekt anderes = new Mietobjekt(); anderes.setId(8L);
        b.setMietobjekt(anderes);
        when(bereiche.findAllById(any())).thenReturn(List.of(b));
        var dto = new HomepageAnbindungDTO(); dto.setErlaubteMietbereichIds(Set.of(10L));
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
    }
    @Test void nichtDirektBuchbareBereicheWerdenAbgelehnt() {
        Mietbereich b = bereich(10L); b.setDirektbuchungAktiv(false);
        when(bereiche.findAllById(any())).thenReturn(List.of(b));
        var dto = new HomepageAnbindungDTO(); dto.setErlaubteMietbereichIds(Set.of(10L));
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
        b.setDirektbuchungAktiv(true); b.setMietbar(false);
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
    }
    @Test void erforderlicheBereicheMuessenEbenfallsFreigegebenSein() {
        Mietbereich eg = bereich(10L), og = bereich(11L);
        og.getVoraussetzungenDirekt().add(eg);
        when(bereiche.findAllById(any())).thenReturn(List.of(og));
        var dto = new HomepageAnbindungDTO(); dto.setErlaubteMietbereichIds(Set.of(11L));
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
        when(bereiche.findAllById(any())).thenReturn(List.of(eg, og));
        dto.setErlaubteMietbereichIds(Set.of(10L, 11L));
        assertEquals(Set.of(10L, 11L), service.save(7L, dto).getErlaubteMietbereichIds());
    }
    @Test void fremdeVersionVerhindertUeberschreiben() {
        var entity = new HomepageAnbindung(); entity.setVersion(3L);
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        var dto = new HomepageAnbindungDTO(); dto.setVersion(2L);
        var error = assertThrows(ResponseStatusException.class, () -> service.save(7L, dto));
        assertEquals(409, error.getStatusCode().value());
        verify(repository, never()).saveAndFlush(any());
    }
    @Test void freischaltungBrauchtVollstaendigeKonfiguration() {
        var dto = new HomepageAnbindungDTO(); dto.setAktiv(true);
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
        dto.setTechnischerClientId("verein-homepage");
        dto.setHomepageUrl("https://verein.example/anfrage"); dto.setDatenschutzUrl("https://verein.example/datenschutz");
        dto.setHausregelnUrl("https://verein.example/regeln"); dto.setErlaubteMietbereichIds(Set.of(10L));
        when(bereiche.findAllById(any())).thenReturn(List.of(bereich(10L)));
        assertThrows(IllegalArgumentException.class, () -> service.save(7L, dto));
        objekt.setAnreisezeit(java.time.LocalTime.of(16,0)); objekt.setAbreisezeit(java.time.LocalTime.of(14,0));
        var saved = service.save(7L, dto);
        assertTrue(saved.isAktiv()); assertEquals("verein-homepage", saved.getTechnischerClientId());
    }
    @Test void rueckgabeKannGespeicherteFreigabenNichtVeraendern() {
        var entity = new HomepageAnbindung(); entity.getErlaubteMietbereichIds().add(10L);
        when(repository.findById(7L)).thenReturn(Optional.of(entity));
        service.get(7L).getErlaubteMietbereichIds().clear();
        assertEquals(Set.of(10L), entity.getErlaubteMietbereichIds());
    }
}
