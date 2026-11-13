package com.kcserver.belegnummer;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.repository.VeranstaltungRepository;
import com.kcserver.kjfp.entity.Dokument;
import com.kcserver.kjfp.entity.abrechnung.*;
import com.kcserver.kjfp.entity.beitraege.Zahlungsnachweis;
import com.kcserver.kjfp.enumtype.*;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.kjfp.service.pdf.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BelegnummerPdfTest {
    final PDFLayoutService layout = new PDFLayoutService();
    final PDFDocumentSizeService sizes = mock(PDFDocumentSizeService.class);
    final Veranstaltung event = new Veranstaltung();
    BelegnummerPdfTest() {
        event.setId(1L); event.setName("Nummernprüfung");
        event.setBeginnDatum(LocalDate.of(2026, 1, 1)); event.setEndeDatum(LocalDate.of(2026, 1, 2));
        when(sizes.determine(any())).thenReturn(new PDFDocumentSize(595, 842, PdfDocumentDensity.HIGH, ReferenzObjekt.DIN_A4));
    }
    Dokument document(long id) throws Exception {
        var d = new Dokument(); d.setId(id); d.setReihenfolge((int) id);
        try (var pdf = new PDDocument(); var out = new ByteArrayOutputStream()) {
            pdf.addPage(new PDPage()); pdf.save(out); d.setInhalt(out.toByteArray());
        }
        return d;
    }
    String text(byte[] bytes) throws Exception {
        try (var pdf = Loader.loadPDF(bytes)) { return new PDFTextStripper().getText(pdf); }
    }
    AbrechnungBeleg beleg(long id, String number) throws Exception {
        var a = new Abrechnung(); a.setVeranstaltung(event);
        var b = new AbrechnungBeleg(); b.setId(id); b.setAbrechnung(a); b.setBelegnummer(number);
        b.setDatum(event.getBeginnDatum()); b.setBeschreibung("Beispiel");
        var booking = new AbrechnungBuchung(); booking.setHerkunft(BuchungsHerkunft.MANUELL);
        booking.setBetrag(BigDecimal.TEN); b.addPosition(booking);
        b.getDokumente().add(document(id)); return b;
    }
    @Test void sortsOverviewAndAttachmentsAndRepeatsWithoutChangingNumbers() throws Exception {
        var repo = mock(AbrechnungBelegRepository.class);
        var high = beleg(10, "RZ_010"); var low = beleg(2, "RZ_002");
        low.getDokumente().add(document(3));
        when(repo.findByVeranstaltungIdWithDokumente(1L)).thenReturn(List.of(high, low));
        var service = new PDFBelegDokumenteService(sizes, repo, layout, new A4LayoutEngine(layout), new PDFDocumentComposer(layout));
        var first = text(service.generate(1L));
        assertThat(first).contains("Belegnummer", "Datum", "#Docs").doesNotContain("#01", "#02")
                .doesNotContainPattern("(?<![0-9])0,00\\s*€");
        assertThat(first.indexOf("RZ_002")).isLessThan(first.indexOf("RZ_010"));
        assertThat(first.split("RZ_002", -1)).hasSize(4); // Übersicht und zwei Dokumente
        assertThat(first.split("RZ_010", -1)).hasSize(3);
        assertThat(text(service.generate(1L))).isEqualTo(first);
        assertThat(low.getBelegnummer()).isEqualTo("RZ_002");
    }
    @Test void printsSubtotalsAtAccountChangesAndIncludesLastAccount() throws Exception {
        var repo = mock(AbrechnungBelegRepository.class);
        var hil = beleg(1, "Hil_001"); var rz1 = beleg(2, "RZ_002"); var rz2 = beleg(3, "RZ_003");
        when(repo.findByVeranstaltungIdWithDokumente(1L)).thenReturn(List.of(rz2, hil, rz1));
        var service = new PDFBelegDokumenteService(sizes, repo, layout, new A4LayoutEngine(layout), new PDFDocumentComposer(layout));
        var bytes = service.generate(1L);
        var result = text(bytes);
        assertThat(result).containsOnlyOnce("Zwischensumme Hil").containsOnlyOnce("Zwischensumme RZ")
                .contains("Gesamtsumme", "20,00", "30,00");
        assertThat(result.indexOf("Zwischensumme Hil")).isBetween(result.indexOf("Hil_001"), result.indexOf("RZ_002"));
        assertThat(result.indexOf("Zwischensumme RZ")).isGreaterThan(result.indexOf("RZ_003"));
        assertThat(text(service.generate(1L))).isEqualTo(result);
        if (System.getProperty("belege.preview") != null) java.nio.file.Files.write(
                java.nio.file.Path.of(System.getProperty("belege.preview")), bytes);
    }

    @Test void separatesPfandIncomeFromVerpflegungExpensesIncludingMixedReceipts() throws Exception {
        var repo = mock(AbrechnungBelegRepository.class);
        var mixed = beleg(1, "RZ_001"); var expense = beleg(2, "RZ_002");
        mixed.getPositionen().getFirst().setKategorie(FinanzKategorie.VERPFLEGUNG);
        expense.getPositionen().getFirst().setKategorie(FinanzKategorie.VERPFLEGUNG);
        var pfand = new AbrechnungBuchung(); pfand.setKategorie(FinanzKategorie.PFAND);
        pfand.setHerkunft(BuchungsHerkunft.MANUELL); pfand.setBetrag(new BigDecimal("5")); mixed.addPosition(pfand);
        when(repo.findByVeranstaltungIdWithDokumente(1L)).thenReturn(List.of(expense, mixed));
        var service = new PDFBelegDokumenteService(sizes, repo, layout, new A4LayoutEngine(layout), new PDFDocumentComposer(layout));
        var bytes = service.generate(1L);
        try (var pdf = Loader.loadPDF(bytes)) {
            var columns = new org.apache.pdfbox.text.PDFTextStripperByArea();
            columns.addRegion("einnahmen", new java.awt.geom.Rectangle2D.Float(415, 0, 71, 800));
            columns.addRegion("ausgaben", new java.awt.geom.Rectangle2D.Float(486, 0, 71, 800));
            columns.extractRegions(pdf.getPage(0));
            assertThat(columns.getTextForRegion("einnahmen")).contains("Einnahmen", "5,00").doesNotContain("20,00");
            assertThat(columns.getTextForRegion("ausgaben")).contains("Ausgaben", "20,00").doesNotContain("25,00");
        }
        assertThat(mixed.getPositionen().getFirst().getBetrag()).isEqualByComparingTo("10");
        assertThat(pfand.getBetrag()).isEqualByComparingTo("5");
        if (System.getProperty("belege.preview") != null) java.nio.file.Files.write(
                java.nio.file.Path.of(System.getProperty("belege.preview") + ".mixed.pdf"), bytes);
    }

    Zahlungsnachweis payment(long id, String number) throws Exception {
        var z = new Zahlungsnachweis(); z.setId(id); z.setBelegnummer(number); z.setVeranstaltung(event);
        z.setDatum(event.getBeginnDatum()); z.setBetrag(BigDecimal.TEN);
        z.setZahlungsweg(number == null ? Zahlungsweg.UEBERWEISUNG : Zahlungsweg.QUITTUNG);
        z.getDokumente().add(document(id)); return z;
    }
    @Test void sortsReceiptsBeforeUnnumberedPaymentsAndLabelsTheirAttachments() throws Exception {
        var repo = mock(ZahlungsnachweisRepository.class); var events = mock(VeranstaltungRepository.class);
        when(events.findById(1L)).thenReturn(Optional.of(event));
        var unnumbered = payment(1, null); var high = payment(10, "RZ_010"); var low = payment(2, "RZ_002");
        when(repo.findForPdfByVeranstaltungId(1L)).thenReturn(List.of(unnumbered, high, low));
        when(repo.findForPdfParticipantsByVeranstaltungId(1L)).thenReturn(List.of(unnumbered, high, low));
        var service = new PDFZahlungsnachweiseService(sizes, events, repo, layout, new A4LayoutEngine(layout), new PDFDocumentComposer(layout));
        var first = text(service.generate(1L));
        assertThat(first).contains("Belegnummer").doesNotContain("#01", "#02", "null");
        assertThat(first.indexOf("RZ_002")).isLessThan(first.indexOf("RZ_010"));
        assertThat(first.split("RZ_002", -1)).hasSize(3);
        assertThat(first.split("RZ_010", -1)).hasSize(3);
        assertThat(text(service.generate(1L))).isEqualTo(first);
        assertThat(unnumbered.getBelegnummer()).isNull();
    }
    @Test void comparesNumericSuffixesAndKeepsMissingNumbersLast() {
        var values = new ArrayList<>(Arrays.asList(null, "RZ_010", "RZ_2", "RZ_001"));
        values.sort(BelegnummerSortierung.NUMMERN);
        assertThat(values).containsExactly("RZ_001", "RZ_2", "RZ_010", null);
    }
}
