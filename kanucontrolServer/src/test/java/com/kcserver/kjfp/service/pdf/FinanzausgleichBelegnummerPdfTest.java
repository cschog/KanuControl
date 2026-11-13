package com.kcserver.kjfp.service.pdf;

import com.kcserver.core.entity.Person;
import com.kcserver.kjfp.entity.abrechnung.*;
import com.kcserver.kjfp.entity.fahrkosten.Reisekostenabrechnung;
import com.kcserver.kjfp.enumtype.FinanzKategorie;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanzausgleichBelegnummerPdfTest {
    @Test void rendersNumbersInAllThreeTablesIncludingContinuationHeaders() throws Exception {
        var service = mock(PDFFinanzausgleichService.class, CALLS_REAL_METHODS);
        var date = LocalDate.of(2026, 11, 12);
        var payment = new PDFFinanzausgleichService.ZahlungsDetail("RZ_028", date, "Max Muster", "Quittung", BigDecimal.TEN, false);
        var transfer = new PDFFinanzausgleichService.ZahlungsDetail(null, date, "Max Muster", "Überweisung", BigDecimal.ONE, false);
        var beleg = new AbrechnungBeleg(); beleg.setBelegnummer("Hil_001"); beleg.setDatum(date); beleg.setAussteller("Laden");
        var buchung = new AbrechnungBuchung(); buchung.setBetrag(BigDecimal.TEN); buchung.setKategorie(FinanzKategorie.FAHRKOSTEN); buchung.setBeschreibung("Kosten");
        var driver = new Person(); driver.setVorname("Max"); driver.setName("Muster");
        var travel = new Reisekostenabrechnung(); travel.setBelegnummer("Hil_002"); travel.setFahrer(driver); travel.setAbrechnungsdatum(date);
        travel.setGesamtBetrag(new BigDecimal("30")); travel.setGesamtKilometer(100); travel.setBemerkung("Anreise");
        try (var pdf = new PDDocument()) {
            try (var page = new PDFPageWriter(pdf)) {
                var rows = new ArrayList<PDFFinanzausgleichService.ZahlungsDetail>(Collections.nCopies(45, payment)); rows.add(transfer);
                ReflectionTestUtils.invokeMethod(service, "writeZahlungsTabelle", page, rows);
            }
            try (var page = new PDFPageWriter(pdf)) {
                ReflectionTestUtils.invokeMethod(service, "writeBelegTabelle", page,
                        List.of(new PDFFinanzausgleichService.BuchungsDetail(beleg, buchung)));
            }
            try (var page = new PDFPageWriter(pdf)) {
                ReflectionTestUtils.invokeMethod(service, "writeFahrkostenTabelle", page, Collections.nCopies(45, travel));
            }
            var text = new PDFTextStripper().getText(pdf);
            assertThat(text).contains("RZ_028", "Hil_001", "Hil_002", "451,00", "1.350,00").doesNotContain("null");
            for (int n=1; n<=pdf.getNumberOfPages(); n++) {
                var stripper = new PDFTextStripper(); stripper.setStartPage(n); stripper.setEndPage(n);
                assertThat(stripper.getText(pdf)).contains("Belegnummer");
            }
            if (System.getProperty("finanz.preview") != null) pdf.save(System.getProperty("finanz.preview"));
        }
    }
}
