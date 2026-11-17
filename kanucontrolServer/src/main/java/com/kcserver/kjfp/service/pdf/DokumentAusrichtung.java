package com.kcserver.kjfp.service.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.kjfp.entity.Dokument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@Component
public class DokumentAusrichtung {
    private static final ObjectMapper JSON=new ObjectMapper();
    public record Info(List<Integer> drehungen,long version) {}
    public record Aenderung(List<Integer> drehungen,Long version) {}
    public record Datei(byte[] bytes,String mimeType,String dateiname) {}
    public record Seite(byte[] bytes,float breite,float hoehe) {}
    public boolean istPdf(Dokument d){return "application/pdf".equalsIgnoreCase(d.getMimeType()) || d.getInhalt()!=null && d.getInhalt().length>=5 && d.getInhalt()[0]==37 && d.getInhalt()[1]==80 && d.getInhalt()[2]==68 && d.getInhalt()[3]==70 && d.getInhalt()[4]==45;}
    public Info info(Dokument d) {
        return new Info(drehungen(d,seitenzahl(d)),d.getAusrichtungVersion()==null?0:d.getAusrichtungVersion());
    }
    public int seitenzahl(Dokument d) {
        if(!istPdf(d))return 1;
        try(var pdf=Loader.loadPDF(d.getInhalt())) {if(pdf.getNumberOfPages()==0)throw invalid("Das PDF enthält keine Seiten.");return pdf.getNumberOfPages();}
        catch(IOException ex){throw invalid("Das PDF konnte nicht gelesen werden.");}
    }
    private List<Integer> drehungen(Dokument d,int count) {
        if(d.getSeitenDrehungen()==null)return new ArrayList<>(Collections.nCopies(count,0));
        try {var values=JSON.readValue(d.getSeitenDrehungen(),Integer[].class);pruefen(Arrays.asList(values),count);return Arrays.asList(values);}
        catch(IOException ex){throw invalid("Die gespeicherte Ausrichtung ist nicht lesbar.");}
    }
    public void setzen(Dokument d,Aenderung request) {
        if(request==null || request.version()==null || request.version()<0)throw invalid("Die Version der Ausrichtung fehlt.");
        if(request.version()!=(d.getAusrichtungVersion()==null?0:d.getAusrichtungVersion()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Das Dokument wurde inzwischen geändert. Bitte erneut öffnen.");
        pruefen(request.drehungen(),seitenzahl(d));
        try{d.setSeitenDrehungen(JSON.writeValueAsString(request.drehungen()));}catch(IOException ex){throw new IllegalStateException(ex);}
    }
    private void pruefen(List<Integer> values,int count) {
        if(values==null || values.size()!=count || values.stream().anyMatch(v->v==null || !Set.of(0,90,180,270).contains(v)))
            throw invalid("Bitte für jede Seite 0, 90, 180 oder 270 Grad angeben.");
    }
    public Datei ausgabe(Dokument d) {
        if(istPdf(d)) {
            try(var pdf=Loader.loadPDF(d.getInhalt());var out=new ByteArrayOutputStream()) {
                var angles=drehungen(d,pdf.getNumberOfPages());
                if(angles.stream().allMatch(v->v==0))return new Datei(d.getInhalt(),d.getMimeType(),d.getOriginalDateiname());
                for(int i=0;i<angles.size();i++)pdf.getPage(i).setRotation(Math.floorMod(pdf.getPage(i).getRotation()+angles.get(i),360));
                pdf.save(out);return new Datei(out.toByteArray(),"application/pdf",d.getOriginalDateiname());
            }catch(IOException ex){throw invalid("Das PDF konnte nicht aufbereitet werden.");}
        }
        try {
            var image=DokumentBild.drehen(DokumentBild.lesen(d.getInhalt()),drehungen(d,1).getFirst());
            return new Datei(png(image),"image/png",pngName(d.getOriginalDateiname()));
        }catch(IOException ex){throw invalid("Das Bild konnte nicht aufbereitet werden: "+ex.getMessage());}
    }
    public byte[] vorschau(Dokument d,int seite) {
        if(seite<0 || seite>=seitenzahl(d))throw invalid("Ungültige Seitennummer.");
        try {
            if(!istPdf(d))return png(DokumentBild.lesen(d.getInhalt()));
            try(var pdf=Loader.loadPDF(d.getInhalt())) {
                var page=pdf.getPage(seite);var box=page.getCropBox();float size=Math.max(box.getWidth(),box.getHeight());
                if(!Float.isFinite(size)||size<=0)throw invalid("Ungültiges Seitenformat.");
                // Basis ohne manuelle Drehung: die lokale Vorschau wendet ihren Entwurf an.
                return png(new PDFRenderer(pdf).renderImage(seite,Math.min(2f,1400f/size)));
            }
        }catch(IOException ex){throw invalid("Die Vorschau konnte nicht erstellt werden.");}
    }
    public List<Seite> seiten(Dokument d) {
        List<Seite> result=new ArrayList<>();
        if(!istPdf(d)) {
            try {var image=DokumentBild.drehen(DokumentBild.lesen(d.getInhalt()),drehungen(d,1).getFirst());result.add(new Seite(png(image),image.getWidth(),image.getHeight()));return result;}
            catch(IOException ex){throw invalid("Belegbild nicht lesbar: "+ex.getMessage());}
        }
        try(var pdf=Loader.loadPDF(d.getInhalt())) {
            var angles=drehungen(d,pdf.getNumberOfPages());
            for(int i=0;i<pdf.getNumberOfPages();i++) {
                try(var single=new PDDocument();var out=new ByteArrayOutputStream()) {
                    var source=pdf.getPage(i);var page=single.importPage(source);page.setResources(source.getResources());
                    int rotation=Math.floorMod(source.getRotation()+angles.get(i),360);page.setRotation(rotation);
                    var box=page.getCropBox();boolean swap=rotation==90||rotation==270;
                    single.save(out);result.add(new Seite(out.toByteArray(),swap?box.getHeight():box.getWidth(),swap?box.getWidth():box.getHeight()));
                }
            }
            return result;
        }catch(IOException ex){throw invalid("Beleg-PDF nicht lesbar.");}
    }
    private byte[] png(BufferedImage image)throws IOException {
        try(var out=new ByteArrayOutputStream()){ImageIO.write(image,"png",out);return out.toByteArray();}
    }
    public static String pngName(String name){return name.replaceFirst("(?i)\\.[^.]+$","")+".png";}
    private ResponseStatusException invalid(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
}
