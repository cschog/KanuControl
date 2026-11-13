package com.kcserver.unit;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.entity.abrechnung.AbrechnungBuchung;
import com.kcserver.kjfp.enumtype.FinanzKategorie;
import com.kcserver.kjfp.repository.fahrkosten.ReisekostenabrechnungRepository;
import com.kcserver.kjfp.service.finanz.FinanzService;
import com.kcserver.kjfp.service.pdf.PDFAbrechnungService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KjfpAbrechnungRegressionTest {
    @Mock ReisekostenabrechnungRepository reisen;
    @Spy FinanzService finanzen = new FinanzService();
    @InjectMocks PDFAbrechnungService pdf;

    @Test
    void kulturUndReisekostenStimmenMitUebersichtUeberein() throws Exception {
        var buchungen = List.of(position(FinanzKategorie.UNTERKUNFT, "1890.26"),
                position(FinanzKategorie.KULTUR, "97.00"),
                position(FinanzKategorie.TEILNEHMERBEITRAG, "1763.50"));
        var summary = finanzen.buildSummary(buchungen, 10, new BigDecimal("203.90"));
        try (var doc = formular(buchungen, "203.90")) {
            var form = doc.getDocumentCatalog().getAcroForm();
            assertThat(summary.getKosten()).isEqualByComparingTo("2191.16");
            assertThat(summary.getSaldo()).isEqualByComparingTo("-427.66");
            assertThat(form.getField("kosten_gesamt").getValueAsString()).isEqualTo("2.191,16");
            assertThat(form.getField("kosten_miete").getValueAsString()).isEqualTo("97,00");
            assertThat(form.getField("kosten_fahrkosten").getValueAsString()).isEqualTo("203,90");
            assertThat(form.getField("einnahmen_eigenleistung_veranstalter").getValueAsString()).isEqualTo("427,66");
        }
    }

    @ParameterizedTest
    @EnumSource(value = FinanzKategorie.class, names = {"UNTERKUNFT", "VERPFLEGUNG", "HONORARE", "FAHRKOSTEN", "VERBRAUCHSMATERIAL", "KULTUR", "MIETE", "TEILNEHMERBEITRAG_RUECKZAHLUNG", "SONSTIGE_KOSTEN"})
    void jedeKostenkategorieErscheintInEinerFormularposition(FinanzKategorie kategorie) throws Exception {
        try (var doc = formular(List.of(position(kategorie, "97.00")), "0")) {
            var form = doc.getDocumentCatalog().getAcroForm();
            String feld = switch (kategorie) {
                case UNTERKUNFT, VERPFLEGUNG -> "kosten_unterkunft_verpflegung";
                case HONORARE -> "kosten_leitung_verguetung";
                case FAHRKOSTEN -> "kosten_fahrkosten";
                case VERBRAUCHSMATERIAL -> "kosten_material";
                default -> "kosten_miete";
            };
            assertThat(form.getField(feld).getValueAsString()).isEqualTo("97,00");
            assertThat(form.getField("kosten_gesamt").getValueAsString()).isEqualTo("97,00");
        }
    }

    @Test
    void kostenaufteilungEnthaeltReisekostenUndTeilzuordnungen() {
        var repository = org.mockito.Mockito.mock(com.kcserver.kjfp.repository.abrechnung.AbrechnungRepository.class);
        var mapper = org.mockito.Mockito.mock(com.kcserver.kjfp.mapper.AbrechnungMapper.class);
        var teilnehmer = org.mockito.Mockito.mock(com.kcserver.core.repository.TeilnehmerRepository.class);
        var reisekosten = org.mockito.Mockito.mock(com.kcserver.kjfp.service.reisekosten.ReisekostenabrechnungService.class);
        var zahlungen = org.mockito.Mockito.mock(com.kcserver.kjfp.repository.zahlungsnachweis.ZahlungsnachweisRepository.class);
        var sync = org.mockito.Mockito.mock(com.kcserver.kjfp.service.abrechnung.AbrechnungSynchronisationsService.class);
        var service = new com.kcserver.kjfp.service.abrechnung.AbrechnungService(repository, null, teilnehmer,
                mapper, finanzen, null, null, sync, reisekosten, zahlungen);
        var abrechnung = new com.kcserver.kjfp.entity.abrechnung.Abrechnung();
        var beleg = new com.kcserver.kjfp.entity.abrechnung.AbrechnungBeleg();
        var unterkunft = position(FinanzKategorie.UNTERKUNFT, "1890.26");
        var kultur = position(FinanzKategorie.KULTUR, "97.00");
        beleg.setPositionen(List.of(unterkunft, kultur));
        abrechnung.setBelege(List.of(beleg));
        when(repository.findByVeranstaltungId(1L)).thenReturn(java.util.Optional.of(abrechnung));
        when(mapper.toDTO(abrechnung)).thenReturn(new com.kcserver.kjfp.dto.abrechnung.AbrechnungDetailDTO());
        when(teilnehmer.findAllKjfpWithPerson(1L)).thenReturn(List.of());
        when(reisekosten.getReisekostenSumme(1L)).thenReturn(new BigDecimal("203.90"));
        when(zahlungen.sumRueckzahlungenByVeranstaltung(1L)).thenReturn(BigDecimal.ZERO);
        var summary = service.getOrCreate(1L).getFinanz();
        assertThat(summary.getKjfpKosten()).isEqualByComparingTo("2191.16");
        assertThat(summary.getSonstigeKosten()).isEqualByComparingTo("0");
        kultur.setKjfpBetrag(new BigDecimal("47.00"));
        summary = service.getOrCreate(1L).getFinanz();
        assertThat(summary.getKjfpKosten()).isEqualByComparingTo("2141.16");
        assertThat(summary.getSonstigeKosten()).isEqualByComparingTo("50.00");
        assertThat(summary.getKjfpKosten().add(summary.getSonstigeKosten())).isEqualByComparingTo(summary.getKosten());
    }

    private PDDocument formular(List<AbrechnungBuchung> buchungen, String fahrkosten) throws Exception {
        when(reisen.sumGesamtBetragByVeranstaltung(1L)).thenReturn(new BigDecimal(fahrkosten));
        var v = new Veranstaltung();
        v.setId(1L);
        var doc = Loader.loadPDF(new ClassPathResource("pdf/abrechnung-JEM-FM_template.pdf").getInputStream().readAllBytes());
        ReflectionTestUtils.invokeMethod(pdf, "fillFinanzen", doc.getDocumentCatalog().getAcroForm(), v, buchungen);
        return doc;
    }

    static AbrechnungBuchung position(FinanzKategorie kategorie, String betrag) {
        var p = new AbrechnungBuchung();
        p.setKategorie(kategorie);
        p.setBetrag(new BigDecimal(betrag));
        return p;
    }
}
