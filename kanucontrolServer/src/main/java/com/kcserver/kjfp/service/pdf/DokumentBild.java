package com.kcserver.kjfp.service.pdf;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.*;

/** EXIF-Orientierung wird einmal angewendet; keine Erkennung der Textrichtung. */
public final class DokumentBild {
    private DokumentBild() {}
    public static BufferedImage lesen(byte[] bytes) throws IOException {
        try (var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers=ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Bildformat nicht lesbar.");
            var reader=readers.next();
            try {
                reader.setInput(input);
                long pixels=(long)reader.getWidth(0)*reader.getHeight(0);
                if (pixels<=0 || pixels>40_000_000) throw new IOException("Bild ist zu groß (höchstens 40 Megapixel).");
                return orientieren(reader.read(0),exif(bytes));
            } finally {reader.dispose();}
        }
    }
    public static BufferedImage drehen(BufferedImage image,int rotation) {
        return orientieren(image,switch(rotation) {case 0 -> 1;case 90 -> 6;case 180 -> 3;case 270 -> 8;default -> throw new IllegalArgumentException("Drehung muss 0, 90, 180 oder 270 Grad sein.");});
    }
    static BufferedImage orientieren(BufferedImage src,int orientation) {
        if (orientation<=1 || orientation>8) return src;
        int w=src.getWidth(),h=src.getHeight(); boolean swap=orientation>=5;
        var dst=new BufferedImage(swap?h:w,swap?w:h,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<h;y++) for(int x=0;x<w;x++) {
            int dx=x,dy=y;
            switch(orientation) {
                case 2 -> dx=w-1-x;
                case 3 -> {dx=w-1-x;dy=h-1-y;}
                case 4 -> dy=h-1-y;
                case 5 -> {dx=y;dy=x;}
                case 6 -> {dx=h-1-y;dy=x;}
                case 7 -> {dx=h-1-y;dy=w-1-x;}
                case 8 -> {dx=y;dy=w-1-x;}
            }
            dst.setRGB(dx,dy,src.getRGB(x,y));
        }
        return dst;
    }
    // TIFF-IFD0 in JPEG APP1, PNG eXIf oder TIFF; beschädigte Metadaten werden ignoriert.
    static int exif(byte[] b) {
        try {
            if(b.length>=4 && (b[0]=='I'&&b[1]=='I'||b[0]=='M'&&b[1]=='M'))return tiff(b,0,b.length);
            if(b.length>4 && (b[0]&255)==255 && (b[1]&255)==216) {
                int p=2;
                while(p+4<=b.length && (b[p]&255)==255) {
                    int marker=b[p+1]&255;if(marker==218||marker==217)break;
                    int length=((b[p+2]&255)<<8)|(b[p+3]&255);
                    if(length<2 || p+2L+length>b.length)break;
                    if(marker==225 && length>=8 && b[p+4]=='E' && b[p+5]=='x' && b[p+6]=='i' && b[p+7]=='f' && b[p+8]==0 && b[p+9]==0)
                        return tiff(b,p+10,length-8);
                    p+=2+length;
                }
            }
            if(b.length>=8 && b[0]==(byte)137 && b[1]=='P' && b[2]=='N' && b[3]=='G') {
                int p=8;
                while(p+12<=b.length) {
                    int length=ByteBuffer.wrap(b,p,4).getInt();
                    if(length<0 || p+12L+length>b.length)break;
                    if(b[p+4]=='e'&&b[p+5]=='X'&&b[p+6]=='I'&&b[p+7]=='f')return tiff(b,p+8,length);
                    p+=length+12;
                }
            }
        } catch(RuntimeException ignored) { /* EXIF ist optional. */ }
        return 1;
    }
    private static int tiff(byte[] bytes,int start,int length) {
        if(length<8)return 1;
        var b=ByteBuffer.wrap(bytes,start,length).slice();
        if(b.get(0)=='I'&&b.get(1)=='I')b.order(ByteOrder.LITTLE_ENDIAN);
        else if(b.get(0)=='M'&&b.get(1)=='M')b.order(ByteOrder.BIG_ENDIAN);else return 1;
        if(b.getShort(2)!=42)return 1;
        long offset=Integer.toUnsignedLong(b.getInt(4));if(offset<8||offset+2>length)return 1;
        int count=Short.toUnsignedInt(b.getShort((int)offset));
        for(int i=0;i<count;i++) {
            long pos=offset+2+12L*i;if(pos+12>length)return 1;int p=(int)pos;
            if(Short.toUnsignedInt(b.getShort(p))==274 && b.getShort(p+2)==3 && b.getInt(p+4)==1) {
                int value=Short.toUnsignedInt(b.getShort(p+8));return value>=1&&value<=8?value:1;
            }
        }
        return 1;
    }
}
