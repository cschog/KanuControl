package com.kcserver.kjfp.service.pdf;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.entity.FinanzGruppe;
import com.kcserver.kjfp.entity.abrechnung.*;
import com.kcserver.kjfp.enumtype.*;
import com.kcserver.kjfp.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.kjfp.repository.fahrkosten.ReisekostenabrechnungRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanzausgleichKategorienPdfTest {
    @Test void allManualIncomeAndExpenseCategoriesAppearInDetailsAndNetSum() throws Exception {
        var service=mock(PDFFinanzausgleichService.class,CALLS_REAL_METHODS);
        var belege=mock(AbrechnungBelegRepository.class); var reisen=mock(ReisekostenabrechnungRepository.class);
        ReflectionTestUtils.setField(service,"belegRepository",belege); ReflectionTestUtils.setField(service,"reisekostenRepository",reisen);
        var event=new Veranstaltung();event.setId(1L);var konto=new FinanzGruppe();konto.setId(1L);konto.setKuerzel("RZ");
        var beleg=new AbrechnungBeleg();beleg.setBelegnummer("RZ_001");beleg.setDatum(LocalDate.of(2026,11,12));
        for(var category:FinanzKategorie.values()) {
            var b=new AbrechnungBuchung();b.setKategorie(category);b.setHerkunft(BuchungsHerkunft.MANUELL);b.setBetrag(BigDecimal.TEN);beleg.addPosition(b);
        }
        var auto=new AbrechnungBuchung();auto.setKategorie(FinanzKategorie.TEILNEHMERBEITRAG);auto.setHerkunft(BuchungsHerkunft.TEILNEHMERBEITRAG);auto.setBetrag(new BigDecimal("100"));beleg.addPosition(auto);
        when(belege.findByAbrechnung_Veranstaltung_IdAndFinanzGruppe_IdOrderByDatumAscLfdNrAsc(1L,1L)).thenReturn(List.of(beleg));
        var gruppe=new PDFFinanzausgleichService.FinanzausgleichGruppe(1,konto,null,List.of(),List.of());
        PDFFinanzausgleichService.FinanzausgleichDetailDaten daten=ReflectionTestUtils.invokeMethod(service,"createDetailDaten",event,gruppe,List.of(),Map.of());
        assertThat(daten.buchungen()).hasSize(FinanzKategorie.values().length-1);
        assertThat(daten.buchungen().stream().map(d->d.buchung().getKategorie())).contains(FinanzKategorie.PFAND,FinanzKategorie.EIGENANTEIL,FinanzKategorie.TEILNEHMERBEITRAG_RUECKZAHLUNG).doesNotContain(FinanzKategorie.FINANZAUSGLEICH);
        assertThat(daten.sonstigeEinnahmen()).isEqualByComparingTo("50");
        try(var pdf=new PDDocument()) {
            try(var page=new PDFPageWriter(pdf)) {ReflectionTestUtils.invokeMethod(service,"writeBelegTabelle",page,daten.buchungen());}
            assertThat(new PDFTextStripper().getText(pdf)).contains("Pfand","Eigenanteil Verein","TN-Rückzahlung","Summe Netto","40,00");
        }
    }    @Test void vkIncludesPfandAndEigenanteilButCountsKjfpOnlyInItsOwnSubtotal() {
        var service=mock(PDFFinanzausgleichService.class,CALLS_REAL_METHODS);
        var konten=mock(com.kcserver.kjfp.repository.finanz.FinanzGruppeRepository.class);
        var abrechnungen=mock(com.kcserver.kjfp.repository.abrechnung.AbrechnungRepository.class);
        var buchungen=mock(com.kcserver.kjfp.repository.abrechnung.AbrechnungBuchungRepository.class);
        var belege=mock(AbrechnungBelegRepository.class);
        ReflectionTestUtils.setField(service,"finanzGruppeRepository",konten);
        ReflectionTestUtils.setField(service,"abrechnungRepository",abrechnungen);
        ReflectionTestUtils.setField(service,"abrechnungBuchungRepository",buchungen);
        ReflectionTestUtils.setField(service,"belegRepository",belege);
        ReflectionTestUtils.setField(service,"reisekostenRepository",mock(ReisekostenabrechnungRepository.class));
        var event=new Veranstaltung();event.setId(1L);var konto=new FinanzGruppe();konto.setId(1L);konto.setKuerzel("VK");
        var abrechnung=new Abrechnung();abrechnung.setId(1L);
        var beleg=new AbrechnungBeleg();
        for(var category:List.of(FinanzKategorie.PFAND,FinanzKategorie.EIGENANTEIL,FinanzKategorie.KJFP_ZUSCHUSS,FinanzKategorie.TEILNEHMERBEITRAG_RUECKZAHLUNG)) {
            var b=new AbrechnungBuchung();b.setKategorie(category);b.setHerkunft(BuchungsHerkunft.MANUELL);b.setBetrag(BigDecimal.TEN);beleg.addPosition(b);
        }
        when(konten.findWithTeilnehmerByVeranstaltungId(1L)).thenReturn(List.of(konto));
        when(abrechnungen.findByVeranstaltungId(1L)).thenReturn(Optional.of(abrechnung));
        when(buchungen.findByBeleg_Abrechnung_Id(1L)).thenReturn(beleg.getPositionen());
        when(belege.findByAbrechnung_Veranstaltung_IdAndFinanzGruppe_IdOrderByDatumAscLfdNrAsc(1L,1L)).thenReturn(List.of(beleg));
        PDFFinanzausgleichService.VKEigenanteilDaten daten=ReflectionTestUtils.invokeMethod(service,"createVKEigenanteilDaten",event,List.of(),List.of());
        assertThat(daten.sonstigeEinnahmen()).isEqualByComparingTo("20");
        assertThat(daten.kjfp()).isEqualByComparingTo("10");
        assertThat(daten.gesamtEinnahmen()).isEqualByComparingTo("30");
        assertThat(daten.direkteAusgaben()).isEqualByComparingTo("10");
    }

}
