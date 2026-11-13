package com.kcserver.kjfp.service.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class BelegAusrichtungPdfTest {
    @Test void landscapeImagesAndPdfsHaveSameRightHandReadingDirectionAndUprightImagesStayUpright() throws Exception {
        var image=new BufferedImage(300,200,BufferedImage.TYPE_INT_RGB);
        var graphics=image.createGraphics();
        graphics.setColor(Color.RED);graphics.fillRect(0,0,150,200);
        graphics.setColor(Color.BLUE);graphics.fillRect(150,0,150,200);graphics.dispose();
        byte[] imageBytes;
        try(var out=new ByteArrayOutputStream()){ImageIO.write(image,"png",out);imageBytes=out.toByteArray();}
        byte[] pdfBytes;
        try(var source=new PDDocument();var out=new ByteArrayOutputStream()) {
            var page=new PDPage(new PDRectangle(300,200));source.addPage(page);
            try(var stream=new PDPageContentStream(source,page)) {
                stream.setNonStrokingColor(Color.RED);stream.addRect(0,0,150,200);stream.fill();
                stream.setNonStrokingColor(Color.BLUE);stream.addRect(150,0,150,200);stream.fill();
            }
            source.save(out);pdfBytes=out.toByteArray();
        }
        var placements=List.of(
                new A4LayoutPlacement("pdf",1,40,100,200,300,0,200,90,false),
                new A4LayoutPlacement("image",2,40,100,200,300,0,200,90,false),
                new A4LayoutPlacement("pdf",3,40,100,300,200,0,200,0,false),
                new A4LayoutPlacement("image",4,40,100,300,200,0,200,0,false));
        var bytes=new PDFDocumentComposer(new PDFLayoutService()).composeWithoutFooter(
                Map.of("pdf",pdfBytes,"image",imageBytes),placements,Map.of());
        try(var result=Loader.loadPDF(bytes)) {
            var renderer=new PDFRenderer(result);
            for(int page=0;page<4;page++) {
                var rendered=renderer.renderImage(page);
                if(page<2) {
                    assertBlue(pixel(rendered,140,350)); // Originalrechte Seite liegt oben.
                    assertRed(pixel(rendered,140,150));
                } else {
                    assertRed(pixel(rendered,90,200));
                    assertBlue(pixel(rendered,290,200));
                }
            }
        }
        if(System.getProperty("orientation.preview")!=null) java.nio.file.Files.write(
                java.nio.file.Path.of(System.getProperty("orientation.preview")),bytes);
    }
    Color pixel(BufferedImage image,int x,int y){return new Color(image.getRGB(x,image.getHeight()-y));}
    void assertRed(Color color){assertThat(color.getRed()).isGreaterThan(200);assertThat(color.getBlue()).isLessThan(50);}
    void assertBlue(Color color){assertThat(color.getBlue()).isGreaterThan(200);assertThat(color.getRed()).isLessThan(50);}
}
