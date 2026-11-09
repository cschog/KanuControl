package com.kcserver.vermietung.service;
import com.kcserver.vermietung.dto.BuchungFinanzenDTO;
import com.kcserver.vermietung.enumtype.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
public final class VermietungAbrechnungPdf {
 private VermietungAbrechnungPdf(){}
 private static String money(BigDecimal value){return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(value);}
 public static byte[] render(BuchungFinanzenDTO r,int revision){
  try(var doc=new PDDocument();var out=new ByteArrayOutputStream();var input=PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf")){
   var font=PDType0Font.load(doc,Objects.requireNonNull(input));
   try(var l=new Layout(doc,font)){
    l.line("Vermietungsabrechnung "+r.buchungsnummer(),18);l.line("Revision "+revision+(revision>1?" - Korrektur der vorherigen Revision":""),11);
    l.line(r.mietobjekt()+" | "+r.mieter(),12);l.line((r.anreise()==null?"":r.anreise().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")))+" bis "+(r.unbefristet()?"unbefristet":r.abreise()==null?"":r.abreise().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")))+" | "+r.buchungsquelle(),10);
    l.line("Mietkosten",14);for(var p:r.mietkosten())l.line(p.bezeichnung()+": "+p.anzahl()+" x "+p.abrechnungseinheiten()+" Einheit(en) x "+money(p.einzelpreis())+" = "+money(p.gesamtbetrag()),10);
    if(r.mietrabatt().signum()>0)l.line("Mietrabatt: -"+money(r.mietrabatt()),11);
    l.line("Miete gesamt: "+money(r.mieteSoll()),12);l.line("Verbrauchskosten: "+money(r.verbrauchSoll()),12);
    if(!r.entschaedigungen().isEmpty()){
     l.line("Entschädigung",14);l.header();
     for(var p:r.entschaedigungen())l.row(new String[]{p.anzahl().toPlainString(),p.bezeichnung(),p.anlass().name().equals("VERLUST")?"Verlust":"Beschädigung",money(p.einzelpreis()),money(p.gesamtbetrag())},true);
     l.line("Entschädigung gesamt: "+money(r.entschaedigungSoll()),12);
    }
    l.ensure(100);l.line("Abrechnungssumme: "+money(r.abrechnungGesamt()),14);
    var saldo=r.saldo();
    l.line("Anrechnungen / Ausgleich: "+money(r.abrechnungGesamt().subtract(saldo)),10);
    l.line((saldo.signum()<0?"Erstattung: ":"Noch zu zahlen: ")+money(saldo.abs()),14);
   }doc.save(out);return out.toByteArray();
  }catch(IOException e){throw new IllegalStateException("PDF konnte nicht erstellt werden.",e);}
 }
 private static final class Layout implements AutoCloseable{
  final PDDocument doc;final PDFont font;PDPageContentStream stream;float y;final float[] widths={38,200,88,90,91};
  Layout(PDDocument d,PDFont f)throws IOException{doc=d;font=f;page();}
  void page()throws IOException{if(stream!=null)stream.close();var p=new PDPage(PDRectangle.A4);doc.addPage(p);stream=new PDPageContentStream(doc,p);y=795;text("KanuControl | Seite "+doc.getNumberOfPages(),44,28,9);}
  void ensure(float h)throws IOException{if(y-h<55)page();}
  void text(String s,float x,float baseline,float size)throws IOException{stream.beginText();stream.setFont(font,size);stream.newLineAtOffset(x,baseline);stream.showText(s);stream.endText();}
  List<String> wrap(String s,float width,float size)throws IOException{
   var result=new ArrayList<String>();String current="";
   for(String word:Objects.toString(s,"").replaceAll("\\p{Cntrl}"," ").split(" +")){
    String next=current.isEmpty()?word:current+" "+word;
    if(font.getStringWidth(next)*size/1000<=width){current=next;continue;}
    if(!current.isEmpty()){result.add(current);current="";}
    for(int cp:word.codePoints().toArray()){String ch=new String(Character.toChars(cp));if(!current.isEmpty()&&font.getStringWidth(current+ch)*size/1000>width){result.add(current);current="";}current+=ch;}
   }result.add(current);return result;
  }
  void line(String s,float size)throws IOException{for(String line:wrap(s,507,size)){ensure(size+7);text(line,44,y,size);y-=size+7;}}
  void header()throws IOException{row(new String[]{"Anzahl","Gegenstand","Anlass","Einzelpreis","Betrag"},false);}
  void row(String[] cells,boolean repeat)throws IOException{
   var lines=new ArrayList<List<String>>();int height=1;for(int i=0;i<cells.length;i++){var list=wrap(cells[i],widths[i]-6,9);lines.add(list);height=Math.max(height,list.size());}
   if(y-height*14-8<55){page();if(repeat)header();}
   for(int row=0;row<height;row++){if(y-14<55){page();if(repeat)header();}float x=44;for(int col=0;col<cells.length;col++){if(row<lines.get(col).size())text(lines.get(col).get(row),x,y,9);x+=widths[col];}y-=14;}y-=8;
  }
  public void close()throws IOException{stream.close();}
 }
}
