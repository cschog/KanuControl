package com.kcserver.vermietung.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.controller.VorlagenController;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VorlagenVorschauTest {
    private final MandantenBriefpapierRepository papier=mock(MandantenBriefpapierRepository.class);
    private final MietobjektVorlageRepository vorlagen=mock(MietobjektVorlageRepository.class);
    private final MietobjektRepository objekte=mock(MietobjektRepository.class);
    private final ObjectMapper mapper=new ObjectMapper();
    private final VorlagenService service=new VorlagenService(papier,vorlagen,objekte,mapper);
    private Vorlage entwurf;

    @BeforeEach void setup() throws Exception {
        TenantContext.setCurrentTenant("ekc_test");
        var objekt=new Mietobjekt();objekt.setId(7L);objekt.setBezeichnung("Bootshaus am See");
        when(objekte.findById(7L)).thenReturn(Optional.of(objekt));when(objekte.existsById(7L)).thenReturn(true);
        var b=new MandantenBriefpapier();b.setVersion(3L);
        b.setPdf(getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes());
        b.setGruss("Mit freundlichen Grüßen\nErika Musterfrau");
        when(papier.findById(1L)).thenReturn(Optional.of(b));
        entwurf=new Vorlage(null,"{{buchungsnummer}} Reservierungsbestätigung","Hallo {{vorname}},\nfür {{mietobjekt}}.",List.of(
            new Block("Reservierung","Aufenthalt vom {{anreise}} bis {{abreise}} in {{mietobjekt}}.",Bedingung.IMMER,List.of()),
            new Block("Mietpreis","Miete: {{mietbetrag}} €.",Bedingung.IMMER,List.of(
                new Variante(Bedingung.OG_GEBUCHT,"Das OG ist mitgebucht."),new Variante(Bedingung.OG_NICHT_GEBUCHT,"Das OG ist nicht mitgebucht."))),
            new Block("Zahlung","Anzahlung: {{anzahlung}} €. Restmiete: {{restmiete}} €. Kaution: {{kaution}} €. Zahlungsfrist: {{zahlungsfrist}}.",Bedingung.ANZAHLUNG_POSITIV,List.of()),
            new Block("Stornierung","",Bedingung.IMMER,List.of(new Variante(Bedingung.STORNO_UNTER_56_TAGEN,"Kurzfristige Reservierung."),new Variante(Bedingung.STORNO_AB_56_TAGEN,"Langfristige Reservierung."))),
            new Block("Nutzungshinweise","Hausregeln: {{hausregeln_url}}",Bedingung.IMMER,List.of())));
    }
    @AfterEach void cleanup(){TenantContext.clear();}
    private String text(byte[] pdf) throws Exception {
        try(var doc=Loader.loadPDF(pdf)){
            assertEquals(1,doc.getNumberOfPages());assertNull(doc.getDocumentCatalog().getAcroForm());
            assertTrue(doc.getPage(0).getAnnotations().stream().noneMatch(a->"Widget".equals(a.getSubtype())));
            return new PDFTextStripper().getText(doc);
        }
    }
    @Test void vollstaendigeUngespeicherteVorlageMitBriefpapierUndAbschluss() throws Exception {
        var result=service.beispielVorschau(7L,Vorgang.RESERVIERUNG,entwurf,false,false);
        var text=text(result.pdf());
        for(var erwartung:List.of("Bootshaus am See","500,00","150,00","350,00","200,00","https://example.org/hausregeln","Erika Musterfrau","Erika Mustermann","Das OG ist nicht mitgebucht.","Langfristige Reservierung."))assertTrue(text.contains(erwartung),erwartung);
        for(var titel:List.of("Mietpreis","Zahlung","Stornierung","Nutzungshinweise"))assertFalse(text.lines().anyMatch(line -> line.strip().equals(titel)),titel);
        assertFalse(text.contains("{{"));assertFalse(text.contains("Beispielüberschrift"));
        assertTrue(result.betreff().contains("BEISPIEL"));assertTrue(result.mailtext().contains("Hallo Erika"));
        verifyNoInteractions(vorlagen);verify(papier,never()).saveAndFlush(any());verify(vorlagen,never()).saveAndFlush(any());
        java.nio.file.Files.write(java.nio.file.Path.of("/private/tmp/kc-vorlage-vollstaendige-vorschau.pdf"),result.pdf());
    }
    @Test void anredeIstKonfigurierbarUndBleibtBeimSpeichernUndLadenErhalten() throws Exception {
        var v=new Vorlage(null,entwurf.betreff(),entwurf.mailtext(),entwurf.bloecke(),"Hallo {{vorname}} {{nachname}},");
        assertTrue(text(service.beispielVorschau(7L,Vorgang.RESERVIERUNG,v,false,false).pdf()).contains("Hallo Erika Mustermann,"));
        when(vorlagen.saveAndFlush(any())).thenAnswer(i -> { var saved=(MietobjektVorlage)i.getArgument(0);saved.setVersion(1L);return saved; });
        var saved=service.speichern(7L,Vorgang.RESERVIERUNG,v);
        assertEquals(v.anrede(),saved.anrede());
        var entity=new MietobjektVorlage();entity.setVersion(1L);entity.setInhalt(mapper.writeValueAsString(saved));
        when(vorlagen.findByMietobjektIdAndVorgang(7L,Vorgang.RESERVIERUNG)).thenReturn(Optional.of(entity));
        assertEquals(v.anrede(),service.vorlage(7L,Vorgang.RESERVIERUNG).anrede());
        assertTrue(text(service.beispielVorschau(7L,Vorgang.RESERVIERUNG,null,false,false).pdf()).contains("Hallo Erika Mustermann,"));
    }
    @Test void alteVorlagenBehaltenIhreAnredeUndUngueltigeAnredenWerdenAbgewiesen() throws Exception {
        var old=mapper.readValue("{\"version\":null,\"betreff\":\"B\",\"mailtext\":\"M\",\"bloecke\":[]}",Vorlage.class);
        assertEquals("Hallo {{vorname}},",old.anrede());
        for(var anrede:List.of("Hallo\nName", "{{unbekannt}}", "x".repeat(251)))
            assertThrows(ResponseStatusException.class,()->pruefe(new Vorlage(null,entwurf.betreff(),entwurf.mailtext(),entwurf.bloecke(),anrede)));
    }
    @Test void alternativeBedingungenWerdenWieImVersandAusgewaehlt() throws Exception {
        var text=text(service.beispielVorschau(7L,Vorgang.RESERVIERUNG,entwurf,true,true).pdf());
        assertTrue(text.contains("Das OG ist mitgebucht."));assertTrue(text.contains("Kurzfristige Reservierung."));
        assertFalse(text.contains("Das OG ist nicht mitgebucht."));assertFalse(text.contains("Langfristige Reservierung."));
    }
    @Test void jederErlaubtePlatzhalterIstAuchInBeispieldatenVerfuegbar() {
        var mail=FELDER.stream().sorted().map(f->"{{"+f+"}}").collect(java.util.stream.Collectors.joining("\n"));
        var v=new Vorlage(null,entwurf.betreff(),mail,entwurf.bloecke());
        assertFalse(service.beispielVorschau(7L,Vorgang.BESTAETIGUNG,v,false,false).mailtext().contains("{{"));
    }
    @Test void gespeicherteVorlageWirdBeiFehlendemEntwurfVerwendet() throws Exception {
        var saved=new MietobjektVorlage();saved.setVersion(2L);saved.setInhalt(mapper.writeValueAsString(entwurf));
        when(vorlagen.findByMietobjektIdAndVorgang(7L,Vorgang.STORNIERUNG)).thenReturn(Optional.of(saved));
        assertTrue(text(service.beispielVorschau(7L,Vorgang.STORNIERUNG,null,false,false).pdf()).contains("Miete: 500,00"));
    }
    @Test void fremdesObjektUndFehlenderMandantWerdenAbgewiesen() {
        assertEquals(404,assertThrows(ResponseStatusException.class,()->service.beispielVorschau(99L,Vorgang.RESERVIERUNG,entwurf,false,false)).getStatusCode().value());
        TenantContext.clear();clearInvocations(objekte,papier,vorlagen);
        assertEquals(403,assertThrows(ResponseStatusException.class,()->service.beispielVorschau(7L,Vorgang.RESERVIERUNG,entwurf,false,false)).getStatusCode().value());
        verifyNoInteractions(objekte,papier,vorlagen);
    }
    @Test void httpVorschauOhneBuchungsIdLiestKeineBuchungUndWirdNichtGecacht() throws Exception {
        var buchungen=mock(BuchungRepository.class);var pdf=mock(ReservierungsbestaetigungPdf.class);var archiv=mock(VorlagenVersandRepository.class);
        var controller=new VorlagenController(service,buchungen,pdf,archiv,mock(AbrechnungVorlagenService.class));
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(post("/api/vermietung/vorlagen/7/RESERVIERUNG/beispiel-vorschau").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new VorlagenController.BeispielEntwurf(entwurf,false,false))))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
                .andExpect(jsonPath("$.data.pdf").isNotEmpty()).andExpect(jsonPath("$.data.text").value("Hallo Erika,\nfür Bootshaus am See."));
        verifyNoInteractions(buchungen,pdf,archiv);
    }
    @Test void ausgewaehltesBriefpapierFunktioniertVorDemErstenSpeichern() throws Exception {
        when(papier.findById(1L)).thenReturn(Optional.empty());
        byte[] asset=getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes();
        var result=service.beispielVorschauMitBriefpapier(7L,Vorgang.RESERVIERUNG,entwurf,false,false,
                null,"Entwurfsgruß für {{vorname}}",asset,null,false);
        assertTrue(text(result.pdf()).contains("Entwurfsgruß für Erika"));
        verify(papier,never()).saveAndFlush(any());verifyNoInteractions(vorlagen);
    }
    @Test void fehlendesBriefpapierUndVeralteteVersionLiefernVerstaendlicheFehler() {
        when(papier.findById(1L)).thenReturn(Optional.empty());
        var missing=assertThrows(ResponseStatusException.class,()->service.beispielVorschauMitBriefpapier(
                7L,Vorgang.RESERVIERUNG,entwurf,false,false,null,"Gruß",null,null,false));
        assertEquals(422,missing.getStatusCode().value());assertTrue(missing.getReason().contains("auswählen"));
        var saved=new MandantenBriefpapier();saved.setVersion(4L);when(papier.findById(1L)).thenReturn(Optional.of(saved));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.beispielVorschauMitBriefpapier(
                7L,Vorgang.RESERVIERUNG,entwurf,false,false,3L,"Gruß",null,null,false)).getStatusCode().value());
    }
    @Test void multipartVorschauVerwendetUploadsUndSpeichertNichts() throws Exception {
        when(papier.findById(1L)).thenReturn(Optional.empty());
        var buchungen=mock(BuchungRepository.class);var pdf=mock(ReservierungsbestaetigungPdf.class);var archiv=mock(VorlagenVersandRepository.class);
        var controller=new VorlagenController(service,buchungen,pdf,archiv,mock(AbrechnungVorlagenService.class));
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        var asset=new org.springframework.mock.web.MockMultipartFile("pdf","Briefpapier.pdf","application/pdf",
                getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes());
        var template=new org.springframework.mock.web.MockMultipartFile("vorlage","vorlage.json","application/json",mapper.writeValueAsBytes(entwurf));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/vermietung/vorlagen/7/RESERVIERUNG/beispiel-vorschau")
                .file(asset).file(template).param("gruss","Entwurfsgruß").param("ogGebucht","true"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andExpect(jsonPath("$.data.pdf").isNotEmpty());
        verifyNoInteractions(buchungen,pdf,archiv);verify(papier,never()).saveAndFlush(any());verify(vorlagen,never()).saveAndFlush(any());
    }

}
