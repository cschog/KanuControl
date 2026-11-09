package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.enumtype.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class VermietungPdfTest {
 @Test void optionaleAbschnitteInterneTexteUndMehrseitigeListen()throws Exception{
  var b=new Buchung();b.setId(1L);b.setBuchungsjahr(2026);b.setLaufendeNummer(1);b.setStatus(Buchungsstatus.ABGESCHLOSSEN);b.setBuchungsquelle(Buchungsquelle.AIRBNB);
  var m=new com.kcserver.core.entity.Person();m.setName("Müller");b.setMieter(m);var o=new Mietobjekt();o.setBezeichnung("Bootshaus");b.setMietobjekt(o);
  var finance=new BuchungFinanzenService(mock(BuchungRepository.class),mock(BuchungFinanzbewegungRepository.class),mock(BuchungVerbrauchskostenRepository.class),mock(VerbrauchsabrechnungService.class));
  var directory=Path.of("target/entschaedigung-pdf");Files.createDirectories(directory);
  for(int count:new int[]{0,1,100}){
   b.getEntschaedigungen().clear();for(int i=0;i<count;i++){var p=new BuchungEntschaedigung();p.setBezeichnung("Gegenstand "+i+" - "+"Lange Bezeichnung mit Umlauten äöü und Spiegel 60 × 60. ".repeat(3));p.setAnlass(BuchungEntschaedigung.Anlass.VERLUST);p.setAnzahl(2);p.setEinzelpreis(new BigDecimal("3"));p.setBemerkung("GEHEIM");p.setPreisbegruendung("GEHEIM");b.getEntschaedigungen().add(p);}
   var dto=finance.toDto(b);byte[] bytes=VermietungAbrechnungPdf.render(dto,1);Files.write(directory.resolve("abrechnung-"+count+".pdf"),bytes);
   try(var doc=org.apache.pdfbox.Loader.loadPDF(bytes)){
    String text=new org.apache.pdfbox.text.PDFTextStripper().getText(doc);assertEquals(count>0,text.contains("Entschädigung"));assertFalse(text.contains("GEHEIM"));
    if(count>0)assertTrue(text.contains("Gegenstand "+(count-1)));if(count==100)assertTrue(doc.getNumberOfPages()>1);
    var renderer=new org.apache.pdfbox.rendering.PDFRenderer(doc);for(int page:new java.util.TreeSet<>(java.util.List.of(0,doc.getNumberOfPages()-1)))javax.imageio.ImageIO.write(renderer.renderImageWithDPI(page,100),"png",directory.resolve("abrechnung-"+count+"-"+page+".png").toFile());
   }
  }
 }
}
