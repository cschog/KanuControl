package com.kcserver.kjfp.service.pdf;

import com.kcserver.core.entity.*;
import com.kcserver.kjfp.dto.validation.*;
import com.kcserver.kjfp.entity.planung.Planung;
import com.kcserver.kjfp.repository.PlanungRepository;
import com.kcserver.kjfp.service.VereinDataStatusService;
import com.kcserver.kjfp.service.veranstaltung.Programmentwurf;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgrammentwurfTest {
    @Test void sanitizesAndPreservesSupportedFormatting() {
        String html = Programmentwurf.inhalt("<div style='color:red'>Grüße <b><i>Welt</i></b><br>Zeile</div><script>alert(1)</script><img src='https://bad'><a href='javascript:bad()'>Text</a><ul><li onclick='bad()'>A</li></ul>");
        assertThat(html).contains("<strong><em>Welt</em></strong>","<br>","<ul><li>A</li></ul>","Text").doesNotContain("style=","onclick","href","src=","script","alert");
        assertThat(Programmentwurf.inhalt(html)).isEqualTo(html);
        assertThat(Programmentwurf.inhalt("<ul><li>A<ul><li>B</li></ul></li></ul>")).doesNotContain("<li>A<ul>").contains("B");
    }
    @Test void emptinessAndLimits() {
        for (String empty : List.of("", "  ", "<p><strong> </strong></p><p><br></p>","<p>&nbsp;\u200b</p>")) assertThat(Programmentwurf.inhalt(empty)).isNull();
        assertThat(Programmentwurf.inhalt("<p>"+"ä".repeat(50000)+"</p>")).isNotNull();
        assertThatThrownBy(() -> Programmentwurf.inhalt("<p>"+"a".repeat(50001)+"</p>")).isInstanceOf(ResponseStatusException.class).hasMessageContaining("50.000");
        assertThatThrownBy(() -> Programmentwurf.inhalt(" ".repeat(500001))).hasMessageContaining("technisch");
        assertThatThrownBy(() -> Programmentwurf.titel("x".repeat(201))).hasMessageContaining("200");
        assertThat(Programmentwurf.sichtbarerText("<p>A<br>B</p><ul><li>C</li><li>D</li></ul>")).isEqualTo("A\nB\nC\nD");
    }
    private Veranstaltung event() {
        Veranstaltung v = new Veranstaltung(); v.setId(42L); v.setName("Herbstfreizeit – Grüße & Spaß");
        v.setBeginnDatum(LocalDate.of(2026,11,13)); v.setEndeDatum(LocalDate.of(2026,11,15));
        Verein verein = new Verein(); verein.setName("Kanuverein Beispiel"); verein.setOrt("Köln"); v.setVerein(verein);
        return v;
    }
    private byte[] generate(Veranstaltung event, java.math.BigDecimal... fahrkosten) {
        Planung p = new Planung(); p.setVeranstaltung(event);
        for (var betrag : fahrkosten) {
            var position = new com.kcserver.kjfp.entity.planung.PlanungPosition();
            position.setKategorie(com.kcserver.kjfp.enumtype.FinanzKategorie.FAHRKOSTEN);
            position.setBetrag(betrag);
            p.addPosition(position);
        }
        return generate(p);
    }
    private byte[] generate(Planung p) {
        PlanungRepository repository = mock(PlanungRepository.class);
        when(repository.findByVeranstaltungIdWithPositionen(42L)).thenReturn(Optional.of(p));
        VereinDataStatusService statusService = mock(VereinDataStatusService.class);
        VereinDataStatusDTO status = mock(VereinDataStatusDTO.class);
        when(statusService.determineStatus(p.getVeranstaltung().getVerein(),true)).thenReturn(status);
        return new PDFAnmeldungFmJemService(repository,statusService).generate(42L);
    }
    @Test void kulturIsIncludedInSonstigeKostenAndBalancedTotals() throws Exception {
        Planung p = new Planung(); p.setVeranstaltung(event());
        var kultur = new com.kcserver.kjfp.entity.planung.PlanungPosition();
        kultur.setKategorie(com.kcserver.kjfp.enumtype.FinanzKategorie.KULTUR);
        kultur.setBetrag(new java.math.BigDecimal("234.56"));
        p.addPosition(kultur);
        try (var doc = Loader.loadPDF(generate(p))) {
            var form = doc.getDocumentCatalog().getAcroForm();
            assertThat(form.getField("sonstige_kosten").getValueAsString()).isEqualTo("234,56");
            assertThat(form.getField("summe_ausgaben").getValueAsString()).isEqualTo("234,56");
            assertThat(form.getField("summe_einnahmen").getValueAsString()).isEqualTo("234,56");
        }
        var categories = new com.kcserver.kjfp.enumtype.FinanzKategorie[] {
            com.kcserver.kjfp.enumtype.FinanzKategorie.SONSTIGE_KOSTEN,
            com.kcserver.kjfp.enumtype.FinanzKategorie.MIETE,
            com.kcserver.kjfp.enumtype.FinanzKategorie.FAHRKOSTEN
        };
        String[] amounts = {"45.67", "80.00", "12.34"};
        for (int i=0; i<categories.length; i++) {
            var position = new com.kcserver.kjfp.entity.planung.PlanungPosition();
            position.setKategorie(categories[i]); position.setBetrag(new java.math.BigDecimal(amounts[i])); p.addPosition(position);
        }
        try (var doc = Loader.loadPDF(generate(p))) {
            var form = doc.getDocumentCatalog().getAcroForm();
            assertThat(form.getField("sonstige_kosten").getValueAsString()).isEqualTo("280,23");
            assertThat(form.getField("mietkosten").getValueAsString()).isEqualTo("80,00");
            assertThat(form.getField("fahrtkosten").getValueAsString()).isEqualTo("12,34");
            assertThat(form.getField("summe_ausgaben").getValueAsString()).isEqualTo("372,57");
            assertThat(form.getField("summe_einnahmen").getValueAsString()).isEqualTo("372,57");
            form.flatten();
            assertThat(new PDFTextStripper().getText(doc)).contains("280,23", "372,57");
            javax.imageio.ImageIO.write(new PDFRenderer(doc).renderImage(1), "png", Path.of(System.getProperty("java.io.tmpdir"), "kjfp-kultur.png").toFile());
        }
    }
    @Test void fahrkostenAppearInActualTemplateFieldAndTotal() throws Exception {
        byte[] pdf = generate(event(), new java.math.BigDecimal("123.45"));
        try (var doc = Loader.loadPDF(pdf)) {
            var form = doc.getDocumentCatalog().getAcroForm();
            assertThat(form.getField("fahrtkosten").getValueAsString()).isEqualTo("123,45");
            assertThat(form.getField("summe_ausgaben").getValueAsString()).isEqualTo("123,45");
            var widgets = form.getField("fahrtkosten").getWidgets();
            assertThat(widgets).isNotEmpty();
            assertThat(widgets.get(0).getAppearance().getNormalAppearance().getAppearanceStream().getCOSObject().getLength()).isPositive();
            // TextStripper liest Widget-Appearances erst nach Flatten der Testkopie.
            form.flatten();
            assertThat(new PDFTextStripper().getText(doc)).contains("123,45");
            javax.imageio.ImageIO.write(new PDFRenderer(doc).renderImage(1), "png", Path.of(System.getProperty("java.io.tmpdir"), "kjfp-fahrkosten.png").toFile());
        }
        try (var doc = Loader.loadPDF(generate(event()))) {
            assertThat(doc.getDocumentCatalog().getAcroForm().getField("fahrtkosten").getValueAsString()).isEqualTo("0,00");
        }
    }
    @Test void pdfExamplesAndUnchangedFirstPage() throws Exception {
        Veranstaltung event = event(); byte[] baseline = generate(event);
        event.setProgrammentwurfTitel("Nur Überschrift");
        try (var doc=Loader.loadPDF(generate(event))) { assertThat(doc.getNumberOfPages()).isEqualTo(2); }
        event.setProgrammentwurfTitel("");
        event.setProgrammentwurfInhalt("<p><strong>Begrüßung</strong> und <em>Kennenlernen</em> – <strong><em>gemeinsam</em></strong>.</p><p>Grüße, Spaß, ÄÖÜ äöü ß €<br>Manueller Zeilenumbruch</p><ul><li>Boote vorbereiten</li><li>Sicherheit besprechen</li></ul><ol><li>Aufwärmen</li><li>Paddeln</li></ol>");
        byte[] shortPdf = generate(event);
        Path examples=Path.of(System.getProperty("java.io.tmpdir"),"kjfp-pdf-examples"); Files.createDirectories(examples); Files.write(examples.resolve("programmentwurf-kurz.pdf"),shortPdf);
        try (var before=Loader.loadPDF(baseline); var doc=Loader.loadPDF(shortPdf)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(3);
            String text=new PDFTextStripper().getText(doc); assertThat(text).contains("Programmentwurf","Begrüßung","Kennenlernen","ÄÖÜ äöü ß €","1. Aufwärmen","2. Paddeln","2026-11-13 bis 2026-11-15");
            assertThat(text).doesNotContain("Veranstaltung 42");
            var a = new PDFRenderer(before).renderImage(0); var b = new PDFRenderer(doc).renderImage(0);
            assertThat(a.getRGB(0,0,a.getWidth(),a.getHeight(),null,0,a.getWidth())).isEqualTo(b.getRGB(0,0,b.getWidth(),b.getHeight(),null,0,b.getWidth()));
            assertThat(doc.getDocumentCatalog().getAcroForm().getField("veranstaltung_name").getValueAsString()).isEqualTo(event.getName());
        }
        event.setProgrammentwurfTitel("Unser mehrtägiges Programm");
        event.setProgrammentwurfInhalt(("<p><strong>Gemeinsam unterwegs:</strong> "+"Wir paddeln, üben und erleben die Natur. ".repeat(12)+"</p><ol><li>Besprechung</li><li>"+"Donaudampfschifffahrtsgesellschaft".repeat(7)+"</li></ol>").repeat(30)+"<p>ENDE-DES-PROGRAMMS</p>");
        byte[] longPdf=generate(event); Files.write(examples.resolve("programmentwurf-mehrseitig.pdf"),longPdf);
        try(var doc=Loader.loadPDF(longPdf)) {
            assertThat(doc.getNumberOfPages()).isGreaterThan(3);
            assertThat(new PDFTextStripper().getText(doc)).contains("ENDE-DES-PROGRAMMS");
            javax.imageio.ImageIO.write(new PDFRenderer(doc).renderImage(doc.getNumberOfPages()-1), "png", examples.resolve("letzte-seite.png").toFile());
        }
        event.setProgrammentwurfInhalt("");
        try(var doc=Loader.loadPDF(generate(event))) { assertThat(doc.getNumberOfPages()).isEqualTo(2); }
    }
}
