package com.kcserver.vermietung.service;

import com.kcserver.core.entity.Verein;
import com.kcserver.core.repository.VereinRepository;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.controller.VeranstalterController;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VeranstalterServiceTest {
    VereinRepository repo;
    VeranstalterService service;
    com.kcserver.core.repository.MitgliedRepository mitglieder;
    com.kcserver.core.repository.VeranstaltungRepository veranstaltungen;
    com.kcserver.vermietung.repository.BuchungRepository buchungen;
    @BeforeEach void setup(){repo=mock(VereinRepository.class);buchungen=mock(com.kcserver.vermietung.repository.BuchungRepository.class);mitglieder=mock(com.kcserver.core.repository.MitgliedRepository.class);veranstaltungen=mock(com.kcserver.core.repository.VeranstaltungRepository.class);service=new VeranstalterService(repo,mock(PersonRepository.class),mitglieder,veranstaltungen,buchungen);TenantContext.setCurrentTenant("test");when(repo.save(any())).thenAnswer(i->i.getArgument(0));}
    @AfterEach void cleanup(){TenantContext.clear();}
    @Test void apiUpdateErhaeltAlleAusgeblendetenFelderUndGemeinsameIdentitaet() throws Exception {
        var v=new Verein();v.setId(7L);v.setName("Verein");v.setAbk("V");v.setTelefon("12345");v.setHausverein(true);
        v.setSchutzkonzept(LocalDate.of(2024,1,1));v.setKikZertifiziertSeit(LocalDate.of(2025,1,1));v.setKikZertifiziertBis(LocalDate.of(2027,1,1));
        when(repo.findById(7L)).thenReturn(Optional.of(v));
        var mvc=MockMvcBuilders.standaloneSetup(new VeranstalterController(service)).build();
        mvc.perform(put("/api/vermietung/veranstalter/7").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"Schule","abk":"S","countryCode":"DE","strasse":"Neue Straße 9","plz":"12345","ort":"Neustadt","iban":"DE123","bankName":"Bank","bic":"BANK","telefon":null,"schutzkonzept":null,"kikZertifiziertSeit":null,"kikZertifiziertBis":null,"hausverein":false,"kjfpVerein":false}
            """)).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(7));
        assertEquals("12345",v.getTelefon());assertEquals(LocalDate.of(2024,1,1),v.getSchutzkonzept());
        assertEquals(LocalDate.of(2025,1,1),v.getKikZertifiziertSeit());assertEquals(LocalDate.of(2027,1,1),v.getKikZertifiziertBis());
        assertTrue(v.isHausverein());assertTrue(v.isKjfpVerein());assertEquals("Neue Straße 9",v.getStrasse());assertEquals("DE123",v.getIban());
        verify(repo).save(same(v));
        mvc.perform(put("/api/vermietung/veranstalter/7").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"Schule","abk":"S","countryCode":"DE","ort":"Anderer Ort"}
            """)).andExpect(status().isOk());
        assertEquals("12345",v.getTelefon());assertNotNull(v.getSchutzkonzept());assertNotNull(v.getKikZertifiziertSeit());assertNotNull(v.getKikZertifiziertBis());
        assertEquals("Anderer Ort",service.lesen(7L).getOrt());
    }
    @Test void apiAnlageOhneKjfpUndBankdaten() throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new VeranstalterController(service)).build();
        mvc.perform(post("/api/vermietung/veranstalter").contentType(MediaType.APPLICATION_JSON).content("""
            {"name":"Schule","abk":"S","countryCode":"DE"}
            """)).andExpect(status().isCreated());
        var captor=org.mockito.ArgumentCaptor.forClass(Verein.class);verify(repo).save(captor.capture());
        var v=captor.getValue();assertFalse(v.isKjfpVerein());assertNull(v.getTelefon());assertNull(v.getIban());assertNull(v.getSchutzkonzept());
    }
    @Test void ohneMandantKeinRepositoryZugriff(){TenantContext.clear();assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.suchen(""));verifyNoInteractions(repo);}
    @Test void fehlendeOrganisationWirdNichtNeuAngelegt(){assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.lesen(99L));verify(repo,never()).save(any());}
    @Test void sucheLiefertBuchungszahlenOhneEinzelabfragen() throws Exception {
        var a=new Verein();a.setId(7L);a.setName("Schule");a.setAbk("S");
        var b=new Verein();b.setId(8L);b.setName("Feuerwehr");b.setAbk("F");
        when(repo.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Verein>>any())).thenReturn(java.util.List.of(a,b));
        var anzahl=mock(com.kcserver.vermietung.repository.BuchungRepository.VeranstalterBuchungszahl.class);
        when(anzahl.getVeranstalterId()).thenReturn(7L);when(anzahl.getAnzahl()).thenReturn(3L);
        when(buchungen.countNachVeranstalter()).thenReturn(java.util.List.of(anzahl));
        var mvc=MockMvcBuilders.standaloneSetup(new VeranstalterController(service)).build();
        mvc.perform(get("/api/vermietung/veranstalter"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("Feuerwehr"))
            .andExpect(jsonPath("$.data[0].buchungen").value(0))
            .andExpect(jsonPath("$.data[1].name").value("Schule"))
            .andExpect(jsonPath("$.data[1].buchungen").value(3));
        verify(buchungen).countNachVeranstalter();verify(buchungen,never()).countByVeranstalterId(anyLong());
    }

    private Verein loeschbarerVeranstalter() {
        var verein = new Verein(); verein.setId(7L); verein.setName("Schule"); verein.setKjfpVerein(false);
        when(repo.findLockedById(7L)).thenReturn(Optional.of(verein));
        return verein;
    }
    @Test void loeschtOhneBuchungUeberApi() throws Exception {
        var verein = loeschbarerVeranstalter();
        var mvc = MockMvcBuilders.standaloneSetup(new VeranstalterController(service)).build();
        mvc.perform(delete("/api/vermietung/veranstalter/7")).andExpect(status().isNoContent());
        verify(repo).delete(same(verein)); verify(repo).flush();
    }
    @Test void buchungVerhindertLoeschenAuchBeiVeraltetemClient() {
        loeschbarerVeranstalter(); when(buchungen.countByVeranstalterId(7L)).thenReturn(1L);
        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.loeschen(7L));
        assertEquals(409, ex.getStatusCode().value()); verify(repo,never()).delete(any(Verein.class));
    }
    @Test void mitgliederWerdenNichtKaskadierendGeloescht() {
        loeschbarerVeranstalter(); when(mitglieder.existsByVereinId(7L)).thenReturn(true);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.loeschen(7L));
        verify(repo,never()).delete(any(Verein.class));
    }
    @Test void kjfpVeranstaltungenVerhindernLoeschen() {
        loeschbarerVeranstalter(); when(veranstaltungen.existsByVereinId(7L)).thenReturn(true);
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.loeschen(7L));
        verify(repo,never()).delete(any(Verein.class));
    }
    @Test void fehlenderVeranstalterLiefert404() {
        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.loeschen(99L));
        assertEquals(404, ex.getStatusCode().value()); verify(repo,never()).delete(any(Verein.class));
    }
    @Test void loeschenOhneMandantBeruehrtKeineDaten() {
        TenantContext.clear();
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.loeschen(7L));
        verifyNoInteractions(repo, buchungen, mitglieder, veranstaltungen);
    }
    @Test void weitereVerknuepfungLiefertVerstaendlichenKonflikt() {
        loeschbarerVeranstalter(); doThrow(new org.springframework.dao.DataIntegrityViolationException("foreign key")).when(repo).flush();
        var ex = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.loeschen(7L));
        assertEquals(409, ex.getStatusCode().value()); assertTrue(ex.getReason().contains("anderen Daten"));
    }

}
