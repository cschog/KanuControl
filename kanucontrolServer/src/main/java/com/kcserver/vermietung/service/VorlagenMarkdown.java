package com.kcserver.vermietung.service;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

/** Versionierter Textcontainer. Ausschließlich Leerzeilen außerhalb seiner Bereiche. */
public final class VorlagenMarkdown {
    private VorlagenMarkdown() {}
    static final ObjectMapper JSON = new ObjectMapper(JsonFactory.builder()
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(30).maxStringLength(1_000_000).maxNumberLength(30).build()).build())
        .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
        .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    public record Meta(int formatVersion, String anrede, String betreff, List<Abschnitt> bloecke) {}
    public record Abschnitt(String id, String titel, Bedingung bedingung, List<Zusatz> varianten) {}
    public record Zusatz(String id, Bedingung bedingung) {}
    static RuntimeException fehler(String datei, int zeile, String text) {
        return VorlagenModell.fehler(datei + (zeile > 0 ? ": Zeile " + zeile : "") + ": " + text);
    }
    static String utf8(byte[] bytes, String file) {
        try {
            String s=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
            return normalisieren(s.startsWith("\uFEFF") ? s.substring(1) : s);
        } catch (CharacterCodingException e) { throw fehler(file,0,"Ungültiges UTF-8."); }
    }
    static <T> T json(byte[] bytes, Class<T> type, String file) {
        try { return JSON.readValue(utf8(bytes,file),type); }
        catch (JsonProcessingException e) { throw fehler(file,e.getLocation()==null?0:(int)e.getLocation().getLineNr(),"Ungültiges JSON: "+e.getOriginalMessage()); }
    }
    static byte[] json(Object value) {
        try { return JSON.writerWithDefaultPrettyPrinter().writeValueAsBytes(value); }
        catch (JsonProcessingException e) { throw new IllegalStateException(e); }
    }
    /** Guard against a model extension silently losing fields through this codec. */
    static void vertrag() {
        if(!felder(Vorlage.class).equals(Set.of("version","anrede","betreff","mailtext","bloecke")) ||
           !felder(Block.class).equals(Set.of("titel","text","bedingung","varianten")) ||
           !felder(Variante.class).equals(Set.of("bedingung","text")))
            throw VorlagenModell.fehler("Vorlagenmodell wurde erweitert. Paketformat muss zuerst angepasst werden.");
    }
    private static Set<String> felder(Class<?> c) { var r=new HashSet<String>(); for(var f:c.getRecordComponents())r.add(f.getName()); return r; }
    public static byte[] schreiben(Vorlage input) {
        vertrag(); var v=normalisieren(input);pruefe(v);var blocks=new ArrayList<Abschnitt>();
        for(int i=0;i<v.bloecke().size();i++) {
            var b=v.bloecke().get(i);var xs=new ArrayList<Zusatz>();
            if(b.varianten()!=null)for(int j=0;j<b.varianten().size();j++)xs.add(new Zusatz("v"+(j+1),b.varianten().get(j).bedingung()));
            blocks.add(new Abschnitt("b"+(i+1),b.titel(),b.bedingung(),b.varianten()==null?null:xs));
        }
        var out=new StringBuilder("```kanucontrol-template\n").append(new String(json(new Meta(1,v.anrede(),v.betreff(),blocks)),StandardCharsets.UTF_8)).append("\n```\n\n");
        bereich(out,"mailtext",v.mailtext());
        for(int i=0;i<blocks.size();i++) {
            var b=v.bloecke().get(i);String id=blocks.get(i).id();bereich(out,"block "+id,b.text());
            if(b.varianten()!=null)for(int j=0;j<b.varianten().size();j++)bereich(out,"variante "+id+" v"+(j+1),b.varianten().get(j).text());
        }
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }
    private static void bereich(StringBuilder b,String key,String text) {
        for(String line:text.split("\n",-1))if(reserviert(line))throw VorlagenModell.fehler("Text enthält eine reservierte Strukturmarkerzeile: "+line);
        b.append("<!-- kc:").append(key).append(" -->\n").append(text).append("\n<!-- kc:end -->\n\n");
    }
    private static boolean reserviert(String s) { return s.startsWith("<!-- kc:") || s.equals("```kanucontrol-template"); }
    public static Vorlage lesen(byte[] bytes,String file) {
        vertrag();String[] lines=utf8(bytes,file).split("\n",-1);
        if(lines.length==0 || !lines[0].equals("```kanucontrol-template"))throw fehler(file,1,"Datei muss mit dem Metadaten-Codeblock beginnen.");
        int pos=1;var metadata=new StringBuilder();
        while(pos<lines.length && !lines[pos].equals("```"))metadata.append(lines[pos++]).append('\n');
        if(pos==lines.length)throw fehler(file,1,"Metadaten-Codeblock ist nicht geschlossen.");pos++;
        Meta m=json(metadata.toString().getBytes(StandardCharsets.UTF_8),Meta.class,file);
        if(m.formatVersion()!=1)throw fehler(file,1,"Nicht unterstützte Formatversion.");
        if(m.anrede()==null || m.betreff()==null || m.bloecke()==null || m.bloecke().size()>5)throw fehler(file,1,"Metadaten unvollständig oder zu viele Blöcke.");
        var expected=new LinkedHashSet<String>();expected.add("mailtext");var ids=new HashSet<String>();
        for(var b:m.bloecke()) {
            if(b==null || !id(b.id()) || !ids.add(b.id()))throw fehler(file,1,"Ungültige oder doppelte Block-ID.");
            expected.add("block "+b.id());var variants=new HashSet<String>();
            if(b.varianten()!=null)for(var x:b.varianten()) {
                if(x==null || !id(x.id()) || !variants.add(x.id()))throw fehler(file,1,"Ungültige oder doppelte Varianten-ID.");
                expected.add("variante "+b.id()+" "+x.id());
            }
        }
        var texts=new HashMap<String,String>();
        while(pos<lines.length) {
            if(lines[pos].isEmpty()){pos++;continue;}
            String start=lines[pos];int line=++pos;
            if(!start.startsWith("<!-- kc:") || !start.endsWith(" -->"))throw fehler(file,line,"Außerhalb der Bereiche sind nur Leerzeilen erlaubt.");
            String key=start.substring(8,start.length()-4);
            if(!expected.contains(key) || texts.containsKey(key))throw fehler(file,line,"Unbekannter oder doppelter Bereich: "+key);
            var text=new StringBuilder();boolean first=true;
            while(pos<lines.length && !lines[pos].equals("<!-- kc:end -->")) {
                if(reserviert(lines[pos]))throw fehler(file,pos+1,"Verschachtelter oder unerwarteter Strukturmarker.");
                if(!first)text.append('\n');first=false;text.append(lines[pos++]);
            }
            if(pos==lines.length || first)throw fehler(file,line,"Bereich nicht korrekt geschlossen (Textzeile erforderlich, auch bei leerem Text).");
            texts.put(key,text.toString());pos++;
        }
        if(!texts.keySet().equals(expected)) {expected.removeAll(texts.keySet());throw fehler(file,0,"Fehlende Bereiche: "+expected);}
        var blocks=new ArrayList<Block>();
        for(var b:m.bloecke()) {
            List<Variante> xs=b.varianten()==null?null:b.varianten().stream().map(x->new Variante(x.bedingung(),texts.get("variante "+b.id()+" "+x.id()))).toList();
            blocks.add(new Block(b.titel(),texts.get("block "+b.id()),b.bedingung(),xs));
        }
        var v=new Vorlage(null,m.betreff(),texts.get("mailtext"),blocks,m.anrede());
        try {pruefe(v);}catch(org.springframework.web.server.ResponseStatusException e){throw fehler(file,0,e.getReason());}return v;
    }
    private static boolean id(String id) {return id!=null && id.matches("[a-zA-Z][a-zA-Z0-9_-]{0,39}");}
}
