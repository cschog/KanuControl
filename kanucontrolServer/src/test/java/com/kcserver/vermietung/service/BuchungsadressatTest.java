package com.kcserver.vermietung.service;

import com.kcserver.core.entity.*;
import com.kcserver.kjfp.enumtype.CountryCode;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.config.SmtpMailProperties;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungsadressatTest {
    Buchung b;
    Verein v;
    @BeforeEach void setup(){b=ReservierungsbestaetigungTest.beispiel();v=new Verein();v.setName("Feuerwehr Musterstadt");v.setStrasse("Organisationsstraße 12");v.setPlz("12345");v.setOrt("Musterstadt");}
    @Test void ohneVeranstalterBleibtMieteranschrift(){var d=Buchungsadressat.felder(b);assertEquals("Jürgen Müller",d.get("empfaenger_name"));assertEquals("Musterstraße 1",d.get("empfaenger_strasse"));}
    @Test void veranstalterNameDannMieterMitVeranstalteranschriftUndLand(){b.setVeranstalter(v);v.setCountryCode(CountryCode.AT);var d=Buchungsadressat.felder(b);assertEquals("Feuerwehr Musterstadt\nJürgen Müller",d.get("empfaenger_name"));assertEquals("Organisationsstraße 12",d.get("empfaenger_strasse"));assertEquals("12345 Musterstadt\nAT",d.get("empfaenger_ort"));}
    @Test void wechselUndEntfernenWirkenSofort(){b.setVeranstalter(v);assertTrue(Buchungsadressat.felder(b).get("empfaenger_name").startsWith("Feuerwehr"));v.setName("Schule");assertTrue(Buchungsadressat.felder(b).get("empfaenger_name").startsWith("Schule"));b.setVeranstalter(null);assertEquals("Jürgen Müller",Buchungsadressat.felder(b).get("empfaenger_name"));}
    @Test void unvollstaendigerVeranstalterHatKeinenFallback(){b.setVeranstalter(v);v.setStrasse(null);var ex=assertThrows(ResponseStatusException.class,()->Buchungsadressat.felder(b));assertTrue(ex.getReason().contains("Straße des Veranstalters"));}
    @Test void vollstaendigerVeranstalterBrauchtKeineMieteranschrift(){b.setVeranstalter(v);b.getMieter().setStrasse(null);assertEquals("Organisationsstraße 12",Buchungsadressat.felder(b).get("empfaenger_strasse"));}
    @Test void gemeinsameBasisUndAlteReservierungVerwendenIdentischenAdressaten(){
        b.setVeranstalter(v);var config=new HomepageAnbindung();config.setHausregelnUrl("https://example.org/regeln");
        var configs=mock(HomepageAnbindungRepository.class);when(configs.findById(7L)).thenReturn(Optional.of(config));
        var bereiche=mock(MietbereichRepository.class);var og=new Mietbereich();og.setId(2L);og.setMietobjekt(b.getMietobjekt());when(bereiche.findById(2L)).thenReturn(Optional.of(og));
        var legacy=new ReservierungsbestaetigungPdf(configs,bereiche).inhalt(b,java.time.LocalDate.now()).felder();
        var basis=new BuchungsnachrichtDaten(configs).basis(b,java.time.LocalDate.now()).felder();
        Buchungsadressat.felder(b).forEach((k,value)->{assertEquals(value,legacy.get(k));assertEquals(value,basis.get(k));});
    }
    @Test @SuppressWarnings("unchecked") void mailBleibtBeimMieterAuchMitVeranstalterKontakt(){
        try(var validator=Validation.buildDefaultValidatorFactory()){
            var kontakt=new Person();kontakt.setEmail("veranstalter@example.org");v.setKontoinhaber(kontakt);b.setVeranstalter(v);
            var service=new HomepageStatusMailService(mock(HomepageStatusMailRepository.class),mock(HomepageAnbindungRepository.class),validator.getValidator(),mock(ObjectProvider.class),new SmtpMailProperties());
            var route=service.nachrichtVorschau(b,"Betreff","Text");assertEquals("mieter@example.org",route.empfaenger());assertNull(route.cc());
            b.getMieter().setEmail(null);assertThrows(IllegalArgumentException.class,()->service.nachrichtVorschau(b,"Betreff","Text"));
        }
    }
    @Test void reservierungBestaetigungUndAbrechnungEnthaltenDenWirksamenAdressblock() throws Exception {
        com.kcserver.core.tenancy.TenantContext.setCurrentTenant("test");
        try {
            b.setVeranstalter(v);
            var papierRepo=mock(MandantenBriefpapierRepository.class);
            var papier=new MandantenBriefpapier();papier.setGruss("Mit freundlichen Grüßen");
            papier.setPdf(getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes());
            when(papierRepo.findById(1L)).thenReturn(Optional.of(papier));
            var vorlagen=new VorlagenService(papierRepo,mock(MietobjektVorlageRepository.class),mock(MietobjektRepository.class),new com.fasterxml.jackson.databind.ObjectMapper());
            var vorlage=new VorlagenModell.Vorlage(null,"Betreff","Mail",List.of(new VorlagenModell.Block("Buchung","Bestätigung des Aufenthalts",VorlagenModell.Bedingung.IMMER,List.of())));
            var basis=new BuchungsnachrichtDaten(mock(HomepageAnbindungRepository.class)).basis(b,java.time.LocalDate.now());
            var pdfs=new ArrayList<byte[]>();
            for(var vorgang:List.of(VorlagenModell.Vorgang.RESERVIERUNG,VorlagenModell.Vorgang.BESTAETIGUNG))
                pdfs.add(vorlagen.erstellenVorgang(b,basis,vorlage,vorgang,Map.of(),Set.of(),null).pdf());
            var rechnung=new AbrechnungVorlagenService(vorlagen,papierRepo,mock(BuchungVerbrauchskostenRepository.class),mock(BuchungFinanzenService.class));
            b.getMietbereiche().clear();
            for(var row:AbrechnungVorlagenTest.finanzen(new java.math.BigDecimal("500")).mietkosten()){
                var position=new BuchungMietbereich();position.setBuchung(b);position.setAnzahl(row.anzahl());position.setVereinbarterPreis(row.einzelpreis());position.setBerechnungsart(com.kcserver.vermietung.enumtype.Berechnungsart.valueOf(row.berechnungsart()));b.getMietbereiche().add(position);
            }
            pdfs.add(rechnung.entwurf(b,1,vorlage,AbrechnungVorlagenTest.finanzen(new java.math.BigDecimal("500")),List.of(),java.time.LocalDate.now()).pdf());
            int i=0;
            for(byte[] pdf:pdfs)try(var doc=org.apache.pdfbox.Loader.loadPDF(pdf)){
                var text=new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
                assertTrue(text.contains("Feuerwehr Musterstadt\nJürgen Müller\nOrganisationsstraße 12\n12345 Musterstadt"),text);
                assertFalse(text.contains("Musterstraße 1"),text);
                java.nio.file.Files.write(java.nio.file.Path.of("/private/tmp/kc-veranstalter-dokument-"+(i++)+".pdf"),pdf);
            }
            v.setStrasse(null);
            assertThrows(ResponseStatusException.class,()->rechnung.entwurf(b,1,vorlage,AbrechnungVorlagenTest.finanzen(new java.math.BigDecimal("500")),List.of(),java.time.LocalDate.now()));
        } finally { com.kcserver.core.tenancy.TenantContext.clear(); }
    }

}
