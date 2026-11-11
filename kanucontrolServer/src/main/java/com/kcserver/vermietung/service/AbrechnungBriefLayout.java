package com.kcserver.vermietung.service;

import java.io.*;
import java.util.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import static com.kcserver.vermietung.service.VorlagenModell.fehler;

/** Reuses validated letterhead. Continuations have booking/page headers and repeat table headings. */
final class AbrechnungBriefLayout implements AutoCloseable {
 private final PDDocument doc; private final PDFont font; private final PDRectangle area; private final String nummer;
 private final PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
 private PDPageContentStream stream; private float y; private float bottom; private float x; private float width;
 private AbrechnungBriefLayout(PDDocument d,PDFont f,PDRectangle a,String n)throws IOException {
  doc=d;font=f;area=a;nummer=n;x=a.getLowerLeftX();width=a.getWidth();bottom=a.getLowerLeftY();y=a.getUpperRightY()-12;
  stream=new PDPageContentStream(doc,doc.getPage(0),PDPageContentStream.AppendMode.APPEND,true,true);
 }
 static byte[] render(byte[] geruest,Map<String,String> felder,List<String> texte,String gruss,byte[] bild,String nummer,Abrechnungsbereich t) {
  // Use exactly the same validation, field formatting and flattening as the reservation path.
  PDRectangle a;
  try(var source=Loader.loadPDF(geruest)){a=source.getDocumentCatalog().getAcroForm().getField("textbereich").getWidgets().getFirst().getRectangle();}
  catch(IOException e){throw fehler("Briefpapier nicht lesbar.");}
  byte[] basis=BriefpapierPdf.render(geruest,felder,List.of(),"",null,List.of(),nummer);
  try(var d=Loader.loadPDF(basis);var in=PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");var out=new ByteArrayOutputStream()) {
   var font=PDType0Font.load(d,Objects.requireNonNull(in));
   try(var l=new AbrechnungBriefLayout(d,font,a,nummer)) {
    if(!texte.isEmpty())l.absatz(texte.getFirst());
    l.header();for(var row:t.zeilen())l.row(row);
    l.y-=8;
    for(int i=1;i<texte.size();i++)l.absatz(texte.get(i));
    var abschluss=Objects.toString(gruss, "").strip();
    var lines=abschluss.isEmpty()?List.<String>of():BriefpapierPdf.wrap(abschluss,font,10,l.width);
    org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject image=bild==null?null:org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray(d,bild,"Unterschrift");
    float h=image==null?0:Math.min(45,120f*image.getHeight()/image.getWidth());
    float gap=image!=null&&!lines.isEmpty()?6:0;
    float closingHeight=lines.size()*12+gap+h+4;
    if(closingHeight<=700){
     // Den Abschluss nur einmal als ganzen Block einpassen, ohne Absatzabstand
     // und zusätzliche Platzprüfung zwischen Gruß und Unterschrift.
     l.ensure(closingHeight,false);
     for(var line:lines){l.text(line,l.x,l.y,10);l.y-=12;}
     l.y-=gap;
     if(image!=null)l.stream.drawImage(image,l.x,l.y-h,h*image.getWidth()/image.getHeight(),h);
    }else{
     l.absatz(abschluss);
     if(image!=null){l.ensure(h+4,false);l.stream.drawImage(image,l.x,l.y-h,h*image.getWidth()/image.getHeight(),h);}
    }
   }
   d.save(out);return out.toByteArray();
  }catch(IOException|IllegalArgumentException e){throw fehler("Abrechnungs-PDF konnte nicht erstellt werden: "+e.getMessage());}
 }
 private void page()throws IOException {
  stream.close();var p=new PDPage(PDRectangle.A4);doc.addPage(p);stream=new PDPageContentStream(doc,p);
  x=44;width=507;bottom=55;y=770;
  text("Abrechnung - Buchung "+nummer+" - Seite "+doc.getNumberOfPages(),44,800,10);
 }
 private void ensure(float h,boolean table)throws IOException {if(y-h<bottom){page();if(table)header();}}
 private void text(String v,float xx,float yy,float size)throws IOException {text(v,xx,yy,size,font);}
 private void text(String v,float xx,float yy,float size,PDFont selectedFont)throws IOException {stream.beginText();stream.setFont(selectedFont,size);stream.newLineAtOffset(xx,yy);stream.showText(v);stream.endText();}
 private void absatz(String s)throws IOException {for(var line:BriefpapierPdf.wrap(s,font,10,width)){ensure(12,false);text(line,x,y,10);y-=12;}y-=8;}
 private void header()throws IOException {ensure(25,false);cells(new Abrechnungsbereich.Zeile("Position / Zahlung","Menge","Einzelpreis","Betrag"),false);}
 private void row(Abrechnungsbereich.Zeile z)throws IOException {cells(z,true);}
 private void cells(Abrechnungsbereich.Zeile z,boolean repeat)throws IOException {
  PDFont rowFont=Set.of("Abrechnungssumme","Restzahlung").contains(z.beschreibung())?boldFont:font;
  float[] widths={width*.49f,width*.17f,width*.17f,width*.17f};
  var values=List.of(z.beschreibung(),z.menge(),z.preis(),z.betrag());var lines=new ArrayList<List<String>>();int count=1;
  for(int i=0;i<4;i++){var v=BriefpapierPdf.wrap(values.get(i),rowFont,9,widths[i]-8);lines.add(v);count=Math.max(count,v.size());}
  // Repeat the column header even when a single long row spans pages.
  ensure(Math.min(count*12+3,40),repeat);
  for(int line=0;line<count;line++){
   ensure(12,repeat);float xx=x;
   for(int col=0;col<4;col++){if(line<lines.get(col).size()){String v=lines.get(col).get(line);float offset=col>=2?widths[col]-4-rowFont.getStringWidth(v)*9/1000:4;text(v,xx+offset,y,9,rowFont);}xx+=widths[col];}y-=12;
  }
  stream.setLineWidth(.3f);stream.moveTo(x,y+7);stream.lineTo(x+width,y+7);stream.stroke();y-=3;
 }
 public void close()throws IOException {stream.close();}
}
