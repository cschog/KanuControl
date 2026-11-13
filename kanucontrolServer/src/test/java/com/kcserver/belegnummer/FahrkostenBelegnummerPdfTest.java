package com.kcserver.belegnummer;

import com.kcserver.core.entity.Person;
import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.entity.FinanzGruppe;
import com.kcserver.kjfp.entity.fahrkosten.*;
import com.kcserver.kjfp.repository.fahrkosten.*;
import com.kcserver.kjfp.service.pdf.PDFFahrkostenabrechnungService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.nio.file.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FahrkostenBelegnummerPdfTest {
    @Test void showsStoredNumberOnBothPagesAndPreservesFormAndNumberOnRepeat() throws Exception {
        var repository = mock(ReisekostenabrechnungRepository.class);
        var configs = mock(ReisekostenKonfigurationRepository.class);
        var date = LocalDate.of(2026, 11, 12);
        var config = new ReisekostenKonfiguration();
        config.setPkwSatz(new BigDecimal("0.30"));
        config.setAnhaengerSatz(BigDecimal.ZERO); config.setMitfahrerSatz(BigDecimal.ZERO);
        when(configs.findFirstByGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(date, date))
                .thenReturn(Optional.of(config));
        var event = new Veranstaltung(); event.setName("Testveranstaltung");
        event.setBeginnDatum(date); event.setEndeDatum(date);
        var driver = new Person(); driver.setId(1L); driver.setVorname("_____"); driver.setName("Muster");
        var rk = new Reisekostenabrechnung(); rk.setId(1L); rk.setFahrer(driver);
        rk.setVeranstaltung(event); rk.setFinanzGruppe(new FinanzGruppe()); rk.setBelegnummer("Hil_001");
        rk.setAbrechnungsdatum(date); rk.setGesamtKilometer(0); rk.setGesamtBetrag(BigDecimal.ZERO);
        when(repository.findById(1L)).thenReturn(Optional.of(rk));
        var service = new PDFFahrkostenabrechnungService(repository, configs);
        String previous = null;
        for (int export = 0; export < 2; export++) {
            var bytes = service.generate(1L);
            try (var pdf = Loader.loadPDF(bytes)) {
                assertThat(pdf.getNumberOfPages()).isEqualTo(2);
                for (int page = 1; page <= 2; page++) {
                    var stripper = new PDFTextStripper(); stripper.setStartPage(page); stripper.setEndPage(page);
                    assertThat(stripper.getText(pdf)).containsOnlyOnce("Belegnummer: Hil_001");
                }
                var form = pdf.getDocumentCatalog().getAcroForm();
                assertThat(form.getField("Name").getValueAsString()).isEqualTo("Muster");
                var text = new PDFTextStripper().getText(pdf);
                assertThat(text).doesNotContain("_____", "Muster,");
                if (previous != null) assertThat(text).isEqualTo(previous);
                previous = text;
            }
            if (export == 0 && System.getProperty("fahrkosten.preview") != null) {
                Files.write(Path.of(System.getProperty("fahrkosten.preview")), bytes);
            }
        }
        assertThat(rk.getBelegnummer()).isEqualTo("Hil_001");
    }
}
