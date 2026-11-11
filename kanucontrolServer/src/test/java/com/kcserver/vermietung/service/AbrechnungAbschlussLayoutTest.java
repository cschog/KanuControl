package com.kcserver.vermietung.service;
import org.junit.jupiter.api.Test;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AbrechnungAbschlussLayoutTest {
 byte[] render(int count)throws Exception{
  var rows=new ArrayList<Abrechnungsbereich.Zeile>();
  for(int i=1;i<=count;i++)rows.add(new Abrechnungsbereich.Zeile("Position "+i,"2 Tage","10,00 €","20,00 €"));
  var signature=new BufferedImage(160,60,BufferedImage.TYPE_INT_RGB);var g=signature.createGraphics();g.setColor(java.awt.Color.WHITE);g.fillRect(0,0,160,60);g.setColor(java.awt.Color.BLACK);g.drawString("Test-Unterschrift",10,35);g.dispose();var bytes=new ByteArrayOutputStream();ImageIO.write(signature,"png",bytes);
  return AbrechnungBriefLayout.render(getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes(),Map.of(),
   List.of("Abrechnung des Aufenthalts\nDie folgenden Positionen werden berechnet.","Bitte überweist die Restzahlung.\nZahlungsfrist: 25.11.2026", "Bank: Vereinsbank\nIBAN: DE00000000000000000000\nBIC: TESTDE00\nVerwendungszweck: 2026-021", "Vielen Dank für euren Besuch."),
   "\nMit freundlichen Grüßen\nHildegard Schog\n",bytes.toByteArray(),"2026-021",new Abrechnungsbereich(rows));
 }
 @Test void ueblicheAbrechnungMitUnterschriftBleibtEinseitig()throws Exception{
  byte[] pdf=render(12);try(var doc=Loader.loadPDF(pdf)){
   assertEquals(1,doc.getNumberOfPages());assertTrue(new PDFTextStripper().getText(doc).contains("Hildegard Schog"));
   java.nio.file.Files.write(java.nio.file.Path.of("/private/tmp/kc-closing/beispiel.pdf"),pdf);
   ImageIO.write(new PDFRenderer(doc).renderImageWithDPI(0,100),"png",new java.io.File("/private/tmp/kc-closing/beispiel.png"));
  }
 }
 @Test void langeAbrechnungBehaeltAbschlussUndUnterschriftGemeinsam()throws Exception{
  try(var doc=Loader.loadPDF(render(60))){
   assertTrue(doc.getNumberOfPages()>1);var text=new PDFTextStripper();text.setStartPage(doc.getNumberOfPages());text.setEndPage(doc.getNumberOfPages());assertTrue(text.getText(doc).contains("Hildegard Schog"));
   boolean image=false;for(var name:doc.getPage(doc.getNumberOfPages()-1).getResources().getXObjectNames())if(doc.getPage(doc.getNumberOfPages()-1).getResources().getXObject(name) instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject)image=true;assertTrue(image);
  }
 }
}
