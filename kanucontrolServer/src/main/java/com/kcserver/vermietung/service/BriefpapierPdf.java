package com.kcserver.vermietung.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import java.io.*;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.fehler;

/** Fixed one-page letter; only compensation rows may create continuation pages. */
public final class BriefpapierPdf {
    private BriefpapierPdf() {}
    public static final Set<String> PFLICHT = Set.of("empfaenger_name","empfaenger_strasse","empfaenger_ort","briefdatum",
        "buchungsnummer","anrede","bank_name","bank_iban","bank_bic","textbereich");
    public static void pruefen(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > 5_000_000) throw fehler("PDF muss zwischen 1 Byte und 5 MB groß sein.");
        try (var doc=Loader.loadPDF(bytes)) {
            if (doc.isEncrypted() || doc.getNumberOfPages()!=1) throw fehler("Briefpapier muss unverschlüsselt sein und genau eine Seite enthalten.");
            var page=doc.getPage(0); var box=page.getMediaBox();
            if (page.getRotation()!=0 || Math.abs(box.getWidth()-PDRectangle.A4.getWidth())>2 || Math.abs(box.getHeight()-PDRectangle.A4.getHeight())>2
                || box.getLowerLeftX()!=0 || box.getLowerLeftY()!=0 || !page.getCropBox().equals(box)) throw fehler("Briefpapier benötigt A4 hochkant ohne Rotation oder Beschnitt.");
            pruefeObjekt(doc.getDocumentCatalog().getCOSObject(), Collections.newSetFromMap(new IdentityHashMap<>()), new int[]{0},0);
            var form=doc.getDocumentCatalog().getAcroForm();
            if (form==null || form.hasXFA()) throw fehler("PDF benötigt normale Formularfelder, kein XFA.");
            var names=new HashSet<String>(); var rects=new LinkedHashMap<String,PDRectangle>();
            for (var field:form.getFieldTree()) {
                String name=field.getFullyQualifiedName();
                if (!names.add(name) || !PFLICHT.contains(name) || !(field instanceof PDTextField)) throw fehler("Unerwartetes oder doppeltes Textfeld: "+name);
                if (field.getWidgets().size()!=1 || !page.getAnnotations().contains(field.getWidgets().getFirst())) throw fehler("Ungültige Feldzuordnung: "+name);
                var r=field.getWidgets().getFirst().getRectangle();
                if (r==null || r.getWidth()<10 || r.getHeight()<10 || r.getLowerLeftX()<0 || r.getLowerLeftY()<0 || r.getUpperRightX()>box.getWidth() || r.getUpperRightY()>box.getHeight()) throw fehler("Ungültige Feldfläche: "+name);
                rects.put(name,r);
            }
            if (!names.equals(PFLICHT)) { var missing=new TreeSet<>(PFLICHT);missing.removeAll(names);throw fehler("Pflichtfelder fehlen: "+String.join(", ",missing)); }
            long widgets=page.getAnnotations().stream().filter(a->"Widget".equals(a.getSubtype())).count();
            if(widgets!=PFLICHT.size())throw fehler("PDF enthält nicht zugeordnete Formularfelder.");
            var area=rects.get("textbereich");
            if (area.getWidth()<350 || area.getHeight()<150) throw fehler("Der textbereich ist zu klein (mindestens 350 × 150 PDF-Punkte).");
            for (var e:rects.entrySet()) if (!e.getKey().equals("textbereich") && area.toGeneralPath().intersects(e.getValue().toGeneralPath().getBounds2D())) throw fehler("textbereich überschneidet Feld "+e.getKey());
        } catch (IOException e) { throw fehler("PDF ist nicht lesbar: "+e.getMessage()); }
    }
    static void pruefeObjekt(COSBase b, Set<COSBase> seen, int[] count,int depth) {
        if(depth>100)throw fehler("PDF ist zu tief verschachtelt.");
        if (b instanceof COSObject o) { pruefeObjekt(o.getObject(),seen,count,depth+1);return; }
        if (b==null || !seen.add(b)) return;
        if (++count[0]>50000) throw fehler("PDF ist zu komplex.");
        if (b instanceof COSDictionary d) {
            for (String key:List.of("JS","JavaScript","OpenAction","AA","EmbeddedFiles","EF","XFA")) if (d.containsKey(COSName.getPDFName(key))) throw fehler("PDF enthält unerlaubte aktive oder eingebettete Inhalte: "+key);
            String uri=d.getString(COSName.URI);
            if(uri!=null && !(uri.startsWith("https://")||uri.startsWith("http://")||uri.startsWith("mailto:")))throw fehler("PDF enthält einen unerlaubten Link.");
            String subtype=d.getNameAsString(COSName.SUBTYPE);
            if(subtype!=null && List.of("RichMedia","Movie","Sound","Screen","FileAttachment").contains(subtype))throw fehler("PDF enthält unerlaubte Medien oder Anhänge.");
            String action=d.getNameAsString(COSName.S);
            if (action!=null && List.of("JavaScript","Launch","SubmitForm","ImportData","GoToR","GoToE","Rendition").contains(action)) throw fehler("PDF enthält eine unerlaubte Aktion.");
            for (COSBase child:d.getValues()) pruefeObjekt(child,seen,count,depth+1);
        } else if (b instanceof COSArray a) for (COSBase child:a) pruefeObjekt(child,seen,count,depth+1);
    }
    public static void pruefeAnhang(byte[] bytes){
        if(bytes==null || bytes.length==0 || bytes.length>10_000_000)throw fehler("PDF-Anhang muss zwischen 1 Byte und 10 MB groß sein.");
        try(var doc=org.apache.pdfbox.Loader.loadPDF(bytes)){
            if(doc.isEncrypted() || doc.getNumberOfPages()<1 || doc.getNumberOfPages()>100)throw fehler("PDF muss unverschlüsselt sein und 1 bis 100 Seiten enthalten.");
            pruefeObjekt(doc.getDocumentCatalog().getCOSObject(),Collections.newSetFromMap(new IdentityHashMap<>()),new int[]{0},0);
            var out=new org.apache.pdfbox.text.PDFTextStripper();out.setEndPage(doc.getNumberOfPages());out.getText(doc);
        }catch(IOException|IllegalArgumentException ex){throw fehler("PDF-Anhang ist nicht lesbar.");}
    }
    public static void pruefeBild(byte[] image) {
        if (image==null) return;
        if (image.length>1_000_000) throw fehler("Unterschriftsbild darf höchstens 1 MB groß sein.");
        try (var in=javax.imageio.ImageIO.createImageInputStream(new ByteArrayInputStream(image))) {
            var readers=javax.imageio.ImageIO.getImageReaders(in);
            if (!readers.hasNext()) throw fehler("Bild muss PNG oder JPEG sein.");
            var reader=readers.next();
            try { reader.setInput(in);String format=reader.getFormatName();
                if (!(format.equalsIgnoreCase("png") || format.equalsIgnoreCase("jpeg")) || reader.getWidth(0)>4000 || reader.getHeight(0)>2000) throw fehler("Bild muss PNG/JPEG und höchstens 4000 × 2000 Pixel groß sein.");
            } finally { reader.dispose(); }
        } catch (IOException e) { throw fehler("Unterschriftsbild ist nicht lesbar."); }
    }
    public static byte[] render(byte[] geruest, Map<String,String> felder, List<String> absaetze, String gruss, byte[] bild, List<String> entschaedigung, String nummer) {
        pruefen(geruest);pruefeBild(bild);
        try (var doc=Loader.loadPDF(geruest);var input=PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");var out=new ByteArrayOutputStream()) {
            var font=PDType0Font.load(doc,Objects.requireNonNull(input),false);var form=doc.getDocumentCatalog().getAcroForm();
            var area=form.getField("textbereich").getWidgets().getFirst().getRectangle();
            // Organisation + contact need four address lines. Reuse the combined existing address area,
            // without changing the stored letterhead contract or drawing outside its field bounds.
            PDRectangle adresse=null;List<String> adresszeilen=List.of();
            var adressfelder=Set.of("empfaenger_name","empfaenger_strasse","empfaenger_ort");
            if(felder.getOrDefault("empfaenger_name", "").contains("\n")){
                float left=Float.MAX_VALUE,bottom=Float.MAX_VALUE,right=0,top=0;
                for(String key:adressfelder){var r=form.getField(key).getWidgets().getFirst().getRectangle();left=Math.min(left,r.getLowerLeftX());bottom=Math.min(bottom,r.getLowerLeftY());right=Math.max(right,r.getUpperRightX());top=Math.max(top,r.getUpperRightY());}
                adresse=new PDRectangle(left,bottom,right-left,top-bottom);
                for(var field:form.getFields())if(!adressfelder.contains(field.getFullyQualifiedName()) && adresse.toGeneralPath().intersects(field.getWidgets().getFirst().getRectangle().toGeneralPath().getBounds2D()))throw fehler("Der gemeinsame Empfängerbereich überschneidet ein anderes Feld.");
                adresszeilen=wrap(felder.getOrDefault("empfaenger_name", "")+"\n"+felder.getOrDefault("empfaenger_strasse", "")+"\n"+felder.getOrDefault("empfaenger_ort", ""),font,10,adresse.getWidth()-4);
                if(adresszeilen.size()*12>adresse.getHeight())throw fehler("Organisation und Anschrift passen nicht in den Empfängerbereich. Bitte Briefpapier-Feldflächen vergrößern.");
            }
            for (var field:form.getFields()) {
                var text=(PDTextField)field;String value=field.getFullyQualifiedName().equals("textbereich")?"":felder.getOrDefault(field.getFullyQualifiedName(),"");
                if(adresse!=null && adressfelder.contains(field.getFullyQualifiedName()))value="";
                var r=text.getWidgets().getFirst().getRectangle();float size=field.getFullyQualifiedName().startsWith("bank_")?8:field.getFullyQualifiedName().equals("briefdatum")?9.5f:10;
                List<String> lines;
                try { lines=wrap(value,font,size,r.getWidth()-4); }
                catch(org.springframework.web.server.ResponseStatusException e){throw fehler("Feld "+field.getFullyQualifiedName()+": "+e.getReason());}
                if (lines.size()*size*1.2>r.getHeight()) throw fehler("Inhalt passt nicht in Feld "+field.getFullyQualifiedName());
                text.setDefaultAppearance("/KC "+size+" Tf 0 g");
                if(form.getDefaultResources()==null)form.setDefaultResources(new PDResources());
                form.getDefaultResources().put(COSName.getPDFName("KC"),font);
                text.setMultiline(true);text.setValue(String.join("\n",lines));
                ReservierungsbestaetigungPdf.textAppearance(doc,text,font,size,String.join("\n",lines));
            }
            form.setNeedAppearances(false);form.flatten();doc.getDocumentCatalog().setAcroForm(null);
            var lines=new ArrayList<String>();
            for(String absatz:absaetze){lines.addAll(wrap(absatz,font,10,area.getWidth()));lines.add("");}
            lines.addAll(wrap(gruss,font,10,area.getWidth()));
            float imageHeight=0; org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject image=null;
            if(bild!=null){image=org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray(doc,bild,"Unterschrift");imageHeight=Math.min(45,120f*image.getHeight()/image.getWidth());}
            if(lines.size()*12+imageHeight>area.getHeight()) throw fehler("Textblöcke und Abschluss passen nicht auf Seite 1. Bitte Texte kürzen; das Hauptschreiben bleibt einseitig.");
            try(var stream=new PDPageContentStream(doc,doc.getPage(0),PDPageContentStream.AppendMode.APPEND,true,true)){
                if(adresse!=null){float ay=adresse.getUpperRightY()-10;for(String line:adresszeilen){text(stream,font,line,adresse.getLowerLeftX()+2,ay,10);ay-=12;}}
                float y=area.getUpperRightY()-10;
                for(String line:lines){text(stream,font,line,area.getLowerLeftX(),y,10);links(doc.getPage(0),font,line,area.getLowerLeftX(),y);y-=12;}
                if(image!=null)stream.drawImage(image,area.getLowerLeftX(),y-imageHeight, imageHeight*image.getWidth()/image.getHeight(),imageHeight);
            }
            PDPageContentStream table=null;float y=0;
            try {
                for(String row:entschaedigung) for(String line:wrap(row,font,10,507)) {
                    if(table==null || y<55){if(table!=null)table.close();var p=new PDPage(PDRectangle.A4);doc.addPage(p);table=new PDPageContentStream(doc,p);text(table,font,"Entschädigungsliste · Buchung "+nummer+" · Seite "+doc.getNumberOfPages(),44,800,11);text(table,font,"Anzahl · Gegenstand · Anlass · Einzelpreis · Betrag",44,778,10);y=754;}
                    text(table,font,line,44,y,10);y-=14;
                }
            } finally {if(table!=null)table.close();}
            doc.save(out);return out.toByteArray();
        } catch(IOException|IllegalArgumentException e){throw fehler("PDF konnte nicht erstellt werden. Gerüst und Sonderzeichen prüfen: "+e.getMessage());}
    }
    static List<String> wrap(String value, PDFont font,float size,float width)throws IOException{
        var lines=new ArrayList<String>();
        for(String paragraph:value.split("\n",-1)){
            String line="";
            for(String word:paragraph.replace('\t',' ').split(" +")){
                if(font.getStringWidth(word)*size/1000>width)throw fehler("Ein Wort oder Feldwert ist zu breit für den vorgesehenen Bereich.");
                String next=line.isEmpty()?word:line+" "+word;
                if(font.getStringWidth(next)*size/1000>width){lines.add(line);line=word;}else line=next;
            }lines.add(line);
        }return lines;
    }
    private static void links(PDPage page,PDFont font,String line,float x,float y)throws IOException{
        var matcher=java.util.regex.Pattern.compile("https?://[^\\s]+").matcher(line);
        while(matcher.find()){
            var link=new org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink();
            link.setRectangle(new PDRectangle(x+font.getStringWidth(line.substring(0,matcher.start()))/100,y-2,font.getStringWidth(matcher.group())/100,12));
            var border=new org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary();border.setWidth(0);link.setBorderStyle(border);
            var action=new org.apache.pdfbox.pdmodel.interactive.action.PDActionURI();action.setURI(matcher.group());link.setAction(action);page.getAnnotations().add(link);
        }
    }
    private static void text(PDPageContentStream s,PDFont font,String text,float x,float y,float size)throws IOException{
        s.beginText();s.setFont(font,size);s.newLineAtOffset(x,y);s.showText(text);s.endText();
    }
}
