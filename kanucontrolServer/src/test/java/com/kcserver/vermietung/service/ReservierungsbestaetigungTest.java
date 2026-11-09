package com.kcserver.vermietung.service;

import com.kcserver.core.entity.Person;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservierungsbestaetigungTest {
    HomepageAnbindungRepository configs;
    MietbereichRepository bereiche;
    ReservierungsbestaetigungPdf pdf;
    Buchung b;
    Mietbereich eg, og;
    HomepageAnbindung config;
    @BeforeEach void setup() {
        configs = mock(HomepageAnbindungRepository.class); bereiche = mock(MietbereichRepository.class);
        pdf = new ReservierungsbestaetigungPdf(configs, bereiche);
        b = beispiel();
        eg = b.getMietbereiche().getFirst().getMietbereich();
        og = new Mietbereich(); og.setId(2L); og.setMietobjekt(b.getMietobjekt());
        config = new HomepageAnbindung(); config.setHausregelnUrl("https://example.org/hausregeln?stand=2026");
        when(configs.findById(7L)).thenReturn(Optional.of(config));
        when(bereiche.findById(2L)).thenReturn(Optional.of(og));
    }
    static Buchung beispiel() {
        var b = new Buchung(); b.setId(11L); b.setBuchungsjahr(2027); b.setLaufendeNummer(15);
        b.setBuchungsquelle(Buchungsquelle.DIREKT);
        b.setAnreise(LocalDateTime.of(2027,6,20,16,0)); b.setAbreise(LocalDateTime.of(2027,6,22,14,0));
        b.setAnzahlungBetrag(new BigDecimal("150")); b.setKautionBetrag(new BigDecimal("200"));
        var objekt = new Mietobjekt(); objekt.setId(7L); objekt.setBezeichnung("Bootshaus Obermaubach");
        objekt.setBankverbindungPrimaer(new Bankverbindung("Eschweiler Kanu Club e.V.", "Primärbank", "DE89370400440532013000", "COBADEFFXXX"));
        objekt.setBankverbindungAlternativ(new Bankverbindung("Alternativer Kontoinhaber", "Sekundärbank", "DE12500105170648489890", "INGDDEFFXXX"));
        b.setMietobjekt(objekt);
        var person = new Person(); person.setVorname("Jürgen"); person.setName("Müller"); person.setStrasse("Musterstraße 1");
        person.setPlz("52076"); person.setOrt("Aachen"); person.setEmail("mieter@example.org"); b.setMieter(person);
        var eg = new Mietbereich(); eg.setId(1L); eg.setBezeichnung("Beliebig umbenannt"); eg.setMietobjekt(objekt);
        var pos = new BuchungMietbereich(); pos.setBuchung(b); pos.setMietbereich(eg); pos.setAnzahl(1);
        pos.setBerechnungsart(Berechnungsart.TAG); pos.setVereinbarterPreis(new BigDecimal("250")); b.getMietbereiche().add(pos);
        return b;
    }
    @Test void zahlungsplanVerwendetVereinbarteBetraegeUndSiebenKalendertage() {
        var i = pdf.inhalt(b, LocalDate.of(2026,11,8));
        assertEquals("Die Miete beträgt 500,00 €.", i.felder().get("mietpreissatz"));
        assertEquals("Die Restzahlung von 350,00 € und die Kaution von 200,00 € müssen spätestens bis zum 13.06.2027 überwiesen werden.",i.felder().get("restzahlung_text"));
        assertTrue(i.felder().get("anzahlung_text").contains("150,00 €"));
        assertEquals("08.11.2026",i.felder().get("briefdatum"));
        assertFalse(i.ogGebucht());
    }
    @Test void rabattWirdGenauEinmalAbgezogen() {
        b.setRabattart(Rabattart.VEREIN);b.setRabattbetrag(new BigDecimal("100"));
        assertEquals("Die Miete beträgt für dich als EKC-Mitglied 400,00 €.",pdf.inhalt(b,LocalDate.now()).felder().get("mietpreissatz"));
        assertTrue(pdf.inhalt(b,LocalDate.now()).felder().get("restzahlung_text").contains("250,00 €"));
        b.setRabattart(Rabattart.VERBAND);
        assertTrue(pdf.inhalt(b,LocalDate.now()).felder().get("mietpreissatz").contains("DKV-Mitglied 400,00 €"));
    }
    @Test void positionsRabatteUndIndividuellerAbsolutrabattFolgenVorhandenerBerechnung() {
        b.setRabattart(Rabattart.VEREIN);
        var p=b.getMietbereiche().getFirst();p.setRabattwertTyp(RabattwertTyp.PROZENTUAL);p.setRabattwert(new BigDecimal("10"));
        b.setRabattbetrag(new BigDecimal("50"));
        assertTrue(pdf.inhalt(b,LocalDate.now()).felder().get("mietpreissatz").contains("450,00 €"));
        b.setRabattart(Rabattart.INDIVIDUELL);b.setRabattwertTyp(RabattwertTyp.ABSOLUT);b.setRabattwert(new BigDecimal("25"));
        assertEquals("Die Miete beträgt 475,00 €.",pdf.inhalt(b,LocalDate.now()).felder().get("mietpreissatz"));
    }
    @Test void ogIdUndIndirekteBelegungWerdenUnabhaengigVomNamenErkannt() {
        b.getMietbereiche().getFirst().setMietbereich(og);assertTrue(pdf.inhalt(b,LocalDate.now()).ogGebucht());
        b.getMietbereiche().getFirst().setMietbereich(eg);eg.getBelegteMietbereiche().add(og);
        og.getBelegteMietbereiche().add(eg);assertTrue(pdf.inhalt(b,LocalDate.now()).ogGebucht());
    }
    @Test void alternativeBankErsetztAlleBankangabenUndHatKeinenFallback() {
        b.setBankverbindungAuswahl(BankverbindungAuswahl.ALTERNATIV);
        var f=pdf.inhalt(b,LocalDate.now()).felder();
        assertEquals("Bank: Sekundärbank",f.get("bank_name"));
        assertFalse(f.containsKey("bank_kontoinhaber"));
        assertEquals("IBAN: DE12500105170648489890",f.get("bank_iban"));
        assertEquals("BIC: INGDDEFFXXX",f.get("bank_bic"));
        b.getMietobjekt().getBankverbindungAlternativ().setIban(null);
        assertTrue(assertThrows(ResponseStatusException.class,()->pdf.erstellen(b)).getReason().contains("Alternative Bankverbindung: IBAN"));
    }
    @Test void unvollstaendigeWerteUndAirbnbWerdenAbgewiesenAberNullbetraegeErlaubt() {
        b.setAnzahlungBetrag(BigDecimal.ZERO);b.setKautionBetrag(BigDecimal.ZERO);assertNotNull(pdf.inhalt(b,LocalDate.now()));
        b.setAnzahlungBetrag(null);assertThrows(ResponseStatusException.class,()->pdf.erstellen(b));
        b.setAnzahlungBetrag(BigDecimal.ZERO);b.getMieter().setStrasse(null);assertThrows(ResponseStatusException.class,()->pdf.erstellen(b));
        b.getMieter().setStrasse("Weg 1");b.setBuchungsquelle(Buchungsquelle.AIRBNB);assertThrows(ResponseStatusException.class,()->pdf.erstellen(b));
    }
    @Test void hausregelnMuessenEineKonfigurierteWebadresseSein() {
        for(String url:Arrays.asList(null,"","javascript:alert(1)","https://","https://user:pass@example.org")) {
            config.setHausregelnUrl(url);assertThrows(ResponseStatusException.class,()->pdf.erstellen(b));
        }
    }
    @Test void fremdesObjektBekommtKeineFalscheOgZuordnung() {
        og.setMietobjekt(new Mietobjekt());assertThrows(ResponseStatusException.class,()->pdf.erstellen(b));
    }
    @Test void geflatteteVariantenBewahrenHausregelnLinkUndEnthaltenKeineBeispieldaten() throws Exception {
        int n=0;
        for(var rabatt:List.of(Rabattart.KEIN_RABATT,Rabattart.VEREIN,Rabattart.VERBAND)) {
            b.setRabattart(rabatt);
            for(boolean mitOg:List.of(false,true)) {
                b.getMietbereiche().getFirst().setMietbereich(mitOg?og:eg);
                b.setBankverbindungAuswahl(mitOg?BankverbindungAuswahl.ALTERNATIV:BankverbindungAuswahl.PRIMAER);
                var d=pdf.erstellen(b);
                try(var doc=Loader.loadPDF(d.pdf())) {
                    assertNull(doc.getDocumentCatalog().getAcroForm());
                    assertTrue(doc.getPage(0).getAnnotations().stream().noneMatch(a->"Widget".equals(a.getSubtype())));
                    assertTrue(doc.getPage(0).getAnnotations().stream().anyMatch(a->a instanceof PDAnnotationLink link
                        && link.getAction() instanceof PDActionURI uri && config.getHausregelnUrl().equals(uri.getURI())));
                    String text=new PDFTextStripper().getText(doc);
                    assertTrue(text.contains("Jürgen Müller"));assertTrue(text.contains("13.06.2027"));
                    assertTrue(text.contains("350,00"));assertFalse(text.contains("Wortberg"));assertFalse(text.contains("verbindlich reservieren"));
                    assertFalse(text.contains(mitOg?"Primärbank":"Sekundärbank"));
                }
                Files.write(Path.of("/tmp/reservierung-test-"+n+".pdf"),d.pdf());
                Files.write(Path.of("/tmp/reservierung-test-"+n+".png"),ReservierungsbestaetigungPdf.vorschaubild(d.pdf()));n++;
            }
        }
    }
    @Test void stornotextWechseltNurBeiWenigerAlsAchtKalenderwochen() throws Exception {
        var heute=LocalDate.of(2026,11,8);
        for (int tage : new int[]{0, 1, 55, 56, 57}) {
            b.setAnreise(heute.plusDays(tage).atTime(16,0));
            b.setAbreise(b.getAnreise().plusDays(2));
            var inhalt=pdf.inhalt(b,heute);
            var text=inhalt.felder().get("stornierung_text");
            assertEquals(tage<56,text.startsWith("Da die Buchung in weniger als 8 Wochen beginnt"));
            assertEquals(tage>=56,text.startsWith("Abmeldungen und Änderungen"));
            byte[] result=ReservierungsbestaetigungPdf.render(inhalt);
            try(var doc=Loader.loadPDF(result)) {
                String ausgabe=new PDFTextStripper().getText(doc);
                assertFalse(ausgabe.contains("Kontoinhaber"));
                assertTrue(ausgabe.contains("Ich bitte um Beachtung der neuen IBAN"));
                assertEquals(tage<56,ausgabe.contains("Da die Buchung in weniger als 8 Wochen beginnt"));
                assertEquals(tage>=56,ausgabe.contains("Abmeldungen und Änderungen"));
            }
            if(tage==55 || tage==56) {
                Files.write(Path.of("/tmp/reservierung-frist-"+tage+".pdf"),result);
                Files.write(Path.of("/tmp/reservierung-frist-"+tage+".png"),ReservierungsbestaetigungPdf.vorschaubild(result));
            }
        }
    }
    @Test void fehlenderKontoinhaberBlockiertDieBestaetigungNichtMehr() {
        b.getMietobjekt().getBankverbindungPrimaer().setKontoinhaber(null);
        assertNotNull(pdf.erstellen(b));
    }
    @Test void zuLangeFelderWerdenNichtLautlosAbgeschnitten() {
        b.getMieter().setName("W".repeat(200));
        assertThrows(ResponseStatusException.class,()->pdf.erstellen(b));
    }
}
