package com.kcserver.kjfp.usecase;

import com.kcserver.kjfp.service.pdf.*;
import com.kcserver.kjfp.enumtype.*;
import com.kcserver.core.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.core.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.core.dto.person.PersonRefDTO;
import com.kcserver.kjfp.service.AltersService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@Transactional
class WeiterePdfUseCaseTest extends KjfpUseCaseBasis {
    @Autowired PDFTeilnehmerDatenkontrolleService kontrolle;
    @Autowired PDFErhebungsbogenService erhebung;
    @Autowired DokumentValidationService validation;
    @ParameterizedTest @CsvSource({"1,1","15,1","16,2","31,3"})
    void teilnehmerlisteHatAlleNamenUndKeineZusaetzlicheLeerseite(int anzahl,int seiten)throws Exception {
        var v=new VeranstaltungDetailDTO();v.setName("Übungsfahrt");v.setBeginnDatum(START);v.setEndeDatum(START.plusDays(4));v.setTyp(VeranstaltungTyp.JEM);v.setLeiterId(999L);
        var teilnehmer=new ArrayList<TeilnehmerDetailDTO>();
        for(int i=0;i<anzahl;i++){var p=new PersonRefDTO();p.setId((long)i);p.setVorname("Jörg");p.setName("Müller"+String.format("%02d",i));var t=new TeilnehmerDetailDTO();t.setPersonId((long)i);t.setPerson(p);t.setGeburtsdatum(START.minusYears(12));teilnehmer.add(t);}
        byte[] bytes=new PDFTeilnehmerlisteService(new AltersService()).generate(v,teilnehmer);
        try(var pdf=Loader.loadPDF(bytes)) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(seiten);
            var names=java.util.stream.StreamSupport.stream(pdf.getDocumentCatalog().getAcroForm().getFieldTree().spliterator(),false).map(f->f.getValueAsString()).filter(s->s.contains("Müller")).toList();
            for(int i=0;i<anzahl;i++){String name="Müller"+String.format("%02d",i);assertThat(names).anyMatch(s->s.contains(name));}
            assertThat(names).hasSize(anzahl);
        }
    }
    @Test void datenKontrolleIstAuchBeiFehlendenPflichtdatenMoeglichDieTeilnehmerlisteNicht()throws Exception {
        var v=event();var t=person(v,null);t.getPerson().setGeburtsdatum(null);em.flush();
        assertThat(validation.validate(v.getId(),PdfDokumentTyp.TEILNEHMER_DATENKONTROLLE).isValid()).isTrue();
        assertThat(validation.validate(v.getId(),PdfDokumentTyp.TEILNEHMERLISTE).isValid()).isFalse();
        try(var pdf=Loader.loadPDF(kontrolle.generate(v.getId()))) {String text=new PDFTextStripper().getText(pdf);assertThat(text).contains(t.getPerson().getName());assertThat(pdf.getNumberOfPages()).isEqualTo(1);}
    }
    @Test void erhebungsbogenEnthaeltGespeicherteDauerUndAltersgruppe()throws Exception {
        var v=event();var t=person(v,null);t.getPerson().setGeburtsdatum(START.minusYears(12));t.getPerson().setSex(Sex.WEIBLICH);em.flush();
        try(var pdf=Loader.loadPDF(erhebung.generate(v.getId()))) {var form=pdf.getDocumentCatalog().getAcroForm();assertThat(form.getField("veranstaltung_tage").getValueAsString()).isEqualTo("5");assertThat(form.getField("veranstaltung_anz_uebernachtungen").getValueAsString()).isEqualTo("4");assertThat(form.getField("10_bis_unter_14_Jahre_weiblich").getValueAsString()).isEqualTo("1");}
    }
}
