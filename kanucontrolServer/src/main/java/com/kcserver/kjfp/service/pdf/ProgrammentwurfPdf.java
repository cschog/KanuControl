package com.kcserver.kjfp.service.pdf;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.kjfp.service.veranstaltung.Programmentwurf;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import java.io.IOException;
import java.util.*;

/** Fließtext auf eigenen A4-Seiten; die vorhandene Anmeldung wird nicht verändert. */
public final class ProgrammentwurfPdf implements AutoCloseable {
    private static final float LEFT = 50, RIGHT = 545, BOTTOM = 65, SIZE = 11, LEADING = 16;
    private final PDDocument doc;
    private final PDFont[] fonts = new PDFont[4];
    private final Veranstaltung event;
    private PDPageContentStream stream;
    private float y;
    private record Glyph(String text, int style) {}
    private ProgrammentwurfPdf(PDDocument doc, Veranstaltung event) throws IOException {
        this.doc = doc; this.event = event;
        String[] names = {"DejaVuSans", "DejaVuSans-Bold", "DejaVuSans-Oblique", "DejaVuSans-BoldOblique"};
        for (int i=0;i<4;i++) {
            try (var in = ProgrammentwurfPdf.class.getResourceAsStream("/fonts/"+names[i]+".ttf")) {
                fonts[i] = PDType0Font.load(doc, Objects.requireNonNull(in), true);
            }
        }
    }
    public static void append(PDDocument doc, Veranstaltung event) throws IOException {
        String html = Programmentwurf.inhalt(event.getProgrammentwurfInhalt());
        if (html == null) return;
        try (var renderer = new ProgrammentwurfPdf(doc,event)) {
            renderer.page();
            renderer.paragraph(event.getProgrammentwurfTitel() == null || event.getProgrammentwurfTitel().isBlank()
                    ? "Programmentwurf" : event.getProgrammentwurfTitel(), 1, 15);
            renderer.y -= 9;
            renderer.blocks(Jsoup.parseBodyFragment(html).body(), "");
        }
    }
    private void page() throws IOException {
        if (stream != null) stream.close();
        PDPage page = new PDPage(PDRectangle.A4); doc.addPage(page);
        stream = new PDPageContentStream(doc,page);
        y = 790;
        paragraph(event.getName(),1,12);
        paragraph(String.valueOf(event.getBeginnDatum())+" bis "+event.getEndeDatum(),0,9);
        y -= 4;
        stream.setStrokingColor(new java.awt.Color(0, 71, 130));
        stream.moveTo(LEFT,y); stream.lineTo(RIGHT,y); stream.stroke();
        y -= 18;
        draw("KanuControl  |  Programmentwurf  |  Seite "+doc.getNumberOfPages(),0,8,LEFT,35);
    }
    private void paragraph(String text,int style,float size) throws IOException {
        List<Glyph> glyphs = new ArrayList<>(); add(text,style,glyphs); lines(glyphs,"",size);
    }
    private void blocks(Element parent,String prefix) throws IOException {
        List<Glyph> pending = new ArrayList<>();
        for (Node node : parent.childNodes()) {
            if (node instanceof Element e && Set.of("p","ul","ol","li").contains(e.tagName())) {
                if (!pending.isEmpty()) { lines(pending,prefix,SIZE); pending.clear(); }
                if (e.tagName().equals("ul") || e.tagName().equals("ol")) {
                    int number=1;
                    for (Element item:e.children()) {
                        blocks(item,e.tagName().equals("ol") ? number+++". " : "• ");
                    }
                } else if (e.tagName().equals("li")) blocks(e,"• ");
                else { blocks(e,prefix); y-=7; }
            } else inline(node,0,pending);
        }
        if (!pending.isEmpty()) lines(pending,prefix,SIZE);
    }
    private void inline(Node node,int style,List<Glyph> target) {
        if (node instanceof TextNode t) add(t.getWholeText(),style,target);
        else if (node instanceof Element e) {
            if (e.tagName().equals("br")) target.add(new Glyph("\n",style));
            else for (Node child:e.childNodes()) inline(child,style | (e.tagName().equals("strong")?1:e.tagName().equals("em")?2:0),target);
        }
    }
    private void add(String text,int style,List<Glyph> target) {
        text.codePoints().forEach(cp -> {
            if (cp == '\n' || cp == '\r') target.add(new Glyph("\n",style));
            else if (!Character.isISOControl(cp) && Character.getType(cp) != Character.FORMAT) {
                String ch = new String(Character.toChars(cp)).replace('\u00a0',' ');
                // Unicode-Schrift; nicht verfügbare Zeichen sichtbar ersetzen statt PDF-Erzeugung abzubrechen.
                try { fonts[style].encode(ch); } catch (Exception ex) { ch = "□"; }
                target.add(new Glyph(ch,style));
            }
        });
    }
    private float width(Glyph g,float size) throws IOException { return fonts[g.style].getStringWidth(g.text)/1000*size; }
    private void lines(List<Glyph> glyphs,String prefix,float size) throws IOException {
        float indent=prefix.isEmpty()?0:25, available=RIGHT-LEFT-indent;
        int start=0; boolean first=true;
        while (start<glyphs.size()) {
            int end=start,lastSpace=-1; float w=0;
            while (end<glyphs.size() && !glyphs.get(end).text.equals("\n")) {
                float next=width(glyphs.get(end),size);
                if (w+next>available && end>start) break;
                w+=next; if (glyphs.get(end).text.equals(" ")) lastSpace=end; end++;
            }
            if (end<glyphs.size() && !glyphs.get(end).text.equals("\n") && lastSpace>start) end=lastSpace+1;
            if (y<BOTTOM+LEADING) page();
            if (first && !prefix.isEmpty()) draw(prefix,0,size,LEFT,y);
            float x=LEFT+indent;
            for (int i=start;i<end;) {
                int style=glyphs.get(i).style;
                StringBuilder run=new StringBuilder(); float runWidth=0;
                while (i<end && glyphs.get(i).style==style) { Glyph g=glyphs.get(i++); run.append(g.text); runWidth+=width(g,size); }
                draw(run.toString(),style,size,x,y); x+=runWidth;
            }
            y-=Math.max(LEADING,size+4); first=false;
            start=end;
            if (start<glyphs.size() && glyphs.get(start).text.equals("\n")) start++;
        }
    }
    private void draw(String value,int style,float size,float x,float baseline) throws IOException {
        stream.beginText(); stream.setFont(fonts[style],size); stream.newLineAtOffset(x,baseline); stream.showText(value); stream.endText();
    }
    @Override public void close() throws IOException { if (stream!=null) stream.close(); }
}
