package com.kcserver.vermietung.service;

import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

final class SimulationsPdf {

    private SimulationsPdf(){}
    static byte[] markieren(byte[] bytes){

        try(var doc=Loader.loadPDF(bytes);
        var in=PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");
        var out=new ByteArrayOutputStream()){

            var font=PDType0Font.load(doc,java.util.Objects.requireNonNull(in));

            for(var page:doc.getPages())try(var s=new PDPageContentStream(doc,page,PDPageContentStream.AppendMode.APPEND,true,true)){

                s.setNonStrokingColor(java.awt.Color.WHITE);
                s.addRect(20,15,page.getMediaBox().getWidth()-40,18);
                s.fill();

                s.setNonStrokingColor(java.awt.Color.RED);
                s.beginText();
                s.setFont(font,10);
                s.newLineAtOffset(24,20);
                s.showText("ENTWURF / SIMULATION – keine endgültige Rechnung");
                s.endText();

            }

            doc.save(out);
            return out.toByteArray();

        }
        catch(java.io.IOException ex){
            throw VorlagenModell.fehler("Rechnungssimulation konnte nicht gekennzeichnet werden.");
        }

    }

}
