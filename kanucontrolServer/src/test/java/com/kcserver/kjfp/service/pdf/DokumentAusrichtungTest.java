package com.kcserver.kjfp.service.pdf;

import com.kcserver.kjfp.entity.Dokument;
import com.kcserver.kjfp.enumtype.ReferenzObjekt;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class DokumentAusrichtungTest {
    final DokumentAusrichtung service=new DokumentAusrichtung();
    @TempDir Path temp;
    static byte[] pdf(int... rotations)throws Exception {
        try(var pdf=new PDDocument();var out=new ByteArrayOutputStream()) {
            for(int i=0;i<rotations.length;i++) {
                var page=new PDPage(new PDRectangle(400,300));page.setCropBox(new PDRectangle(20,30,300,200));page.setRotation(rotations[i]);pdf.addPage(page);
                try(var stream=new PDPageContentStream(pdf,page)) {
                    Color[] colors={Color.RED,Color.BLUE,Color.GREEN,Color.YELLOW};
                    for(int n=0;n<4;n++){stream.setNonStrokingColor(colors[n]);stream.addRect(20+(n%2)*150,30+(n/2)*100,150,100);stream.fill();}
                    stream.beginText();stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),14);stream.newLineAtOffset(30,205);stream.setNonStrokingColor(Color.BLACK);stream.showText("OBEN SEITE "+(i+1));stream.endText();
                }
            }
            pdf.save(out);return out.toByteArray();
        }
    }
    static Dokument dokument(byte[] bytes,String mime){var d=new Dokument();d.setId(9L);d.setInhalt(bytes);d.setMimeType(mime);d.setOriginalDateiname(mime.equals("application/pdf")?"beleg.pdf":"foto.jpg");d.setReferenzObjekt(ReferenzObjekt.DIN_A4);d.setAusrichtungVersion(0L);return d;}
    @Test void perPageRotationPreservesOriginalAndRejectsInvalidAndStaleChanges()throws Exception {
        var d=dokument(pdf(90,180),"application/pdf");byte[] original=d.getInhalt().clone();
        assertThat(service.info(d).drehungen()).containsExactly(0,0);
        service.setzen(d,new DokumentAusrichtung.Aenderung(List.of(180,90),0L));
        assertThat(service.info(d).drehungen()).containsExactly(180,90);
        try(var out=Loader.loadPDF(service.ausgabe(d).bytes())) {assertThat(out.getPage(0).getRotation()).isEqualTo(270);assertThat(out.getPage(1).getRotation()).isEqualTo(270);}
        assertThat(d.getInhalt()).isEqualTo(original);
        for(var values:List.of(List.of(0),List.of(45,0),List.of(360,0)))assertThatThrownBy(()->service.setzen(d,new DokumentAusrichtung.Aenderung(values,0L))).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->service.setzen(d,new DokumentAusrichtung.Aenderung(List.of(0,0),1L))).isInstanceOf(ResponseStatusException.class).hasMessageContaining("inzwischen");
        assertThat(service.info(d).drehungen()).containsExactly(180,90);
        service.setzen(d,new DokumentAusrichtung.Aenderung(List.of(0,0),0L));assertThat(service.ausgabe(d).bytes()).isEqualTo(original);
    }
    @Test void composerRespectsIntrinsicRotationAndNonZeroCropBox()throws Exception {
        for(int intrinsic:new int[]{0,90,180,270})for(int manual:new int[]{0,90,180,270}) {
            var d=dokument(pdf(intrinsic),"application/pdf");service.setzen(d,new DokumentAusrichtung.Aenderung(List.of(manual),0L));
            var page=service.seiten(d).getFirst();float w=page.breite(),h=page.hoehe();
            byte[] result=new PDFDocumentComposer(new PDFLayoutService()).composeWithoutFooter(Map.of("p",page.bytes()),List.of(new A4LayoutPlacement("p",1,40,100,w,h,0,h,0,false)),Map.of());
            try(var expected=Loader.loadPDF(service.ausgabe(d).bytes());var actual=Loader.loadPDF(result)) {
                var source=new PDFRenderer(expected).renderImage(0);var target=new PDFRenderer(actual).renderImage(0);
                for(double fx:new double[]{0.25,0.75})for(double fy:new double[]{0.25,0.75}) {
                    int a=source.getRGB((int)(source.getWidth()*fx),(int)(source.getHeight()*fy));
                    int b=target.getRGB((int)(40+w*fx),(int)(target.getHeight()-100-h*(1-fy)));
                    assertThat(new Color(b).getRGB()).as("Intrinsic %s, manuell %s, x=%s y=%s",intrinsic,manual,fx,fy).isEqualTo(new Color(a).getRGB());
                }
            }
        }
    }
    static byte[] jpegExif(int orientation)throws Exception {
        var image=new BufferedImage(120,80,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();g.setColor(Color.RED);g.fillRect(0,0,60,80);g.setColor(Color.BLUE);g.fillRect(60,0,60,80);g.dispose();
        var out=new ByteArrayOutputStream();ImageIO.write(image,"jpeg",out);byte[] jpg=out.toByteArray();
        byte[] exif={69,120,105,102,0,0,73,73,42,0,8,0,0,0,1,0,18,1,3,0,1,0,0,0,(byte)orientation,0,0,0,0,0,0,0};
        out.reset();out.write(jpg,0,2);out.write(255);out.write(225);out.write(0);out.write(exif.length+2);out.write(exif);out.write(jpg,2,jpg.length-2);return out.toByteArray();
    }
    @Test void allExifOrientationsAndManualQuarterTurnsAreAppliedExactlyOnce()throws Exception {
        for(int orientation=1;orientation<=8;orientation++) {
            byte[] original=jpegExif(orientation);var base=DokumentBild.lesen(original);
            Color corner=new Color(base.getRGB(5,5));
            boolean red=Set.of(1,4,5,6).contains(orientation);
            assertThat(red?corner.getRed():corner.getBlue()).as("EXIF %s: richtige Ecke",orientation).isGreaterThan(200);
            assertThat(red?corner.getBlue():corner.getRed()).isLessThan(50);
            assertThat(base.getWidth()).isEqualTo(orientation>=5?80:120);assertThat(base.getHeight()).isEqualTo(orientation>=5?120:80);
            var d=dokument(original,"image/jpeg");service.setzen(d,new DokumentAusrichtung.Aenderung(List.of(180),0L));
            var image=ImageIO.read(new ByteArrayInputStream(service.ausgabe(d).bytes()));
            assertThat(image.getWidth()).isEqualTo(base.getWidth());assertThat(image.getRGB(5,5)).isEqualTo(base.getRGB(base.getWidth()-6,base.getHeight()-6));
            assertThat(d.getInhalt()).isEqualTo(original);
            var turned=base;for(int i=0;i<4;i++)turned=DokumentBild.drehen(turned,90);
            assertThat(turned.getWidth()).isEqualTo(base.getWidth());assertThat(turned.getRGB(5,5)).isEqualTo(base.getRGB(5,5));
        }
    }
    @Test void producesVisualExamplesAndIncludesEveryPageInReport()throws Exception {
        var d=dokument(pdf(0,90),"application/pdf");service.setzen(d,new DokumentAusrichtung.Aenderung(List.of(180,270),0L));
        Path dir=Path.of(System.getProperty("java.io.tmpdir"),"kc-belegrotation-beispiele");Files.createDirectories(dir);
        Files.write(dir.resolve("gedrehter-beleg.pdf"),service.ausgabe(d).bytes());
        var event=new com.kcserver.core.entity.Veranstaltung();event.setName("Ausrichtungspruefung");event.setBeginnDatum(java.time.LocalDate.of(2026,11,16));event.setEndeDatum(event.getBeginnDatum());
        var abrechnung=new com.kcserver.kjfp.entity.abrechnung.Abrechnung();abrechnung.setVeranstaltung(event);
        var beleg=new com.kcserver.kjfp.entity.abrechnung.AbrechnungBeleg();beleg.setId(1L);beleg.setBelegnummer("VK_001");beleg.setDatum(event.getBeginnDatum());beleg.setBeschreibung("Mehrseitiger Beleg");beleg.setAbrechnung(abrechnung);beleg.addDokument(d);
        var booking=new com.kcserver.kjfp.entity.abrechnung.AbrechnungBuchung();booking.setHerkunft(com.kcserver.kjfp.enumtype.BuchungsHerkunft.MANUELL);booking.setKategorie(com.kcserver.kjfp.enumtype.FinanzKategorie.KULTUR);booking.setBetrag(java.math.BigDecimal.TEN);beleg.addPosition(booking);
        var repo=org.mockito.Mockito.mock(com.kcserver.kjfp.repository.abrechnung.AbrechnungBelegRepository.class);org.mockito.Mockito.when(repo.findByVeranstaltungIdWithDokumente(1L)).thenReturn(List.of(beleg));var layout=new PDFLayoutService();
        var report=new PDFBelegDokumenteService(new PDFDocumentSizeService(),repo,layout,new A4LayoutEngine(layout),new PDFDocumentComposer(layout)).generate(1L);
        Files.write(dir.resolve("beleg-report.pdf"),report);
        try(var pdf=Loader.loadPDF(report)) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(3);
            String text=new org.apache.pdfbox.text.PDFTextStripper().getText(pdf);assertThat(text.replaceAll("\\s", "")).contains("OBENSEITE1","OBENSEITE2");
            for(int i=0;i<pdf.getNumberOfPages();i++)ImageIO.write(new PDFRenderer(pdf).renderImage(i),"png",dir.resolve("report-"+(i+1)+".png").toFile());
        }
    }
}
