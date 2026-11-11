package com.kcserver.vermietung.service;

import org.apache.commons.compress.archivers.zip.*;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.text.Normalizer;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

/** Bounded in-memory ZIP reader. No file-system extraction or external references. */
public final class VorlagenPaket {
    private VorlagenPaket() {}
    public static final int ZIP_MAX=25_000_000, TOTAL_MAX=50_000_000, ENTRIES_MAX=500, TEXT_MAX=1_000_000;
    public record Quelle(String mandantBezeichnung) {}
    public record Papier(@com.fasterxml.jackson.annotation.JsonProperty(required=true) String pdf,@com.fasterxml.jackson.annotation.JsonProperty(required=true) String grussformel,@com.fasterxml.jackson.annotation.JsonProperty(required=true) String bild) {}
    public record Objekt(String schluessel,String bezeichnung) {}
    public record Eintrag(String mietobjekt,Vorgang vorgang,String datei) {}
    public record Manifest(String format,int formatVersion,String exportiertAm,Quelle quelle,Papier briefpapier,List<Objekt> mietobjekte,List<Eintrag> vorlagen) {}
    public record Inhalt(Manifest manifest,byte[] pdf,String gruss,byte[] bild,Map<String,Vorlage> vorlagen) {}
    public static final String ANLEITUNG="""
        # KanuControl-Vorlagen – Paketformat 1

        Dieses Paket enthält ausschließlich gespeicherte Konfiguration, keine Buchungen oder Versanddaten.
        manifest.json bleibt im ZIP-Wurzelverzeichnis. Fehlendes Briefpapier (null) bedeutet beim Import beibehalten.
        Vorlagen als UTF-8 bearbeiten. CRLF/CR wird zu LF normalisiert; BOM am Dateianfang ist erlaubt.
        Genau ein JSON-Codeblock kanucontrol-template am Dateianfang; keine unbekannten/doppelten JSON-Felder.
        Block-/Variantenreihenfolge steht in den Arrays. IDs ordnen die Textbereiche zu.
        Jeder deklarierte Bereich und mailtext muss genau einmal vorkommen. Marker nur auf eigenen Zeilen.
        Je ein Zeilenumbruch nach Startmarker und vor Endmarker ist Protokoll; weitere Leerzeilen gehören zum Text.
        Außerhalb der Bereiche sind ausschließlich vollständig leere Zeilen erlaubt. Keine zusätzlichen Kommentare.
        Reservierte Zeilen beginnen mit <!-- kc: oder lauten ```kanucontrol-template; sie sind kein normaler Text.
        Grußformel ist direkter Text in mandant/grussformel.md. Keine Prüfsummen manuell pflegen.
        Keine Dateien umbenennen/ergänzen, ohne das Manifest passend zu ändern. Keine verschachtelten Archive.
        ZIP in der Vorlagenverwaltung importieren, Quellobjekte ausdrücklich zuordnen und neu/ersetzen/überspringen wählen.
        Briefpapier, Grußformel und Bild sind eine gemeinsame Einheit für den gesamten Zielmandanten.
        Bei Übernahme dieser Einheit entfernt bild:null das bisherige Bild. Andere fehlende Inhalte werden nie gelöscht.
        Vorschauen mit synthetischen Daten prüfen, dann Geprüfte Auswahl übernehmen. Kein Versand beim Import.
        Beispielvorschauen sind Layoutkontrollen; Laufzeitprüfung mit tatsächlichen Buchungsdaten bleibt erforderlich.
        Leere Listen und briefpapier:null beschreiben eine leere Konfiguration.
        """;
    public static byte[] schreiben(Manifest manifest,Map<String,byte[]> files) {
        try(var out=new ByteArrayOutputStream();var zip=new java.util.zip.ZipOutputStream(out,StandardCharsets.UTF_8)) {
            var all=new LinkedHashMap<String,byte[]>();all.put("manifest.json",VorlagenMarkdown.json(manifest));all.put("ANLEITUNG.md",ANLEITUNG.getBytes(StandardCharsets.UTF_8));all.putAll(files);
            int total=0;if(all.size()>ENTRIES_MAX)throw gross();
            for(var e:all.entrySet()){total+=e.getValue().length;if(total>TOTAL_MAX)throw gross();zip.putNextEntry(new java.util.zip.ZipEntry(e.getKey()));zip.write(e.getValue());zip.closeEntry();}
            zip.finish();if(out.size()>ZIP_MAX)throw gross();return out.toByteArray();
        }catch(IOException e){throw fehler("ZIP konnte nicht erstellt werden.");}
    }
    static ResponseStatusException gross(){return new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Paket überschreitet die Grenzen (25 MB ZIP, 50 MB entpackt, 500 Einträge, 1 MB je Textdatei).");}
    private static void pfad(String path) {
        if(path==null || path.isEmpty() || path.startsWith("/") || path.contains("\\") || path.contains(":" ) || path.contains("\0") || path.length()>240)
            throw fehler("Unsicherer Paketpfad: "+path);
        for(String s:path.split("/",-1))if(s.equals("..") || s.equals(".") || s.isEmpty())throw fehler("Unsicherer Paketpfad: "+path);
    }
    public static Inhalt lesen(byte[] bytes) {
        if(bytes.length>ZIP_MAX)throw gross();var files=new LinkedHashMap<String,byte[]>();var names=new HashSet<String>();int total=0,count=0;
        try(var channel=new SeekableInMemoryByteChannel(bytes);var zip=ZipFile.builder().setSeekableByteChannel(channel).get()) {
            var entries=zip.getEntries();
            while(entries.hasMoreElements()) {
                var e=entries.nextElement();if(++count>ENTRIES_MAX)throw gross();String name=e.getName();
                String path=e.isDirectory() && name.endsWith("/")?name.substring(0,name.length()-1):name;pfad(path);
                if(!names.add(Normalizer.normalize(path,Normalizer.Form.NFC).toLowerCase(Locale.ROOT)))throw fehler("Doppelter/kollidierender Paketpfad: "+path);
                if(e.isUnixSymlink() || (e.getUnixMode() & 0170000)!=0 && (e.getUnixMode() & 0170000)!=(e.isDirectory()?0040000:0100000) || e.getGeneralPurposeBit().usesEncryption() || !zip.canReadEntryData(e))throw fehler("Nicht unterstützter ZIP-Eintrag: "+name);
                int max=path.endsWith(".pdf")?5_000_000:path.endsWith(".png")||path.endsWith(".jpg")?1_000_000:TEXT_MAX;
                try(var in=zip.getInputStream(e);var out=new ByteArrayOutputStream()) {
                    var crc=new java.util.zip.CRC32();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){total+=n;if(total>TOTAL_MAX || out.size()+n>max)throw gross();crc.update(buf,0,n);out.write(buf,0,n);}
                    if(e.getCrc()!=crc.getValue() || e.getSize()!=out.size())throw fehler("ZIP-Eintrag ist beschädigt: "+name);
                    if(e.isDirectory()){if(out.size()!=0)throw fehler("Verzeichnis enthält Daten: "+name);continue;}
                    if(path.equals(".DS_Store") || path.endsWith("/.DS_Store") || path.startsWith("__MACOSX/"))continue;
                    files.put(path,out.toByteArray());
                }
            }
        }catch(IOException | IllegalArgumentException e){throw fehler("ZIP ist beschädigt oder nicht lesbar.");}
        if(!files.containsKey("manifest.json"))throw fehler("manifest.json fehlt im ZIP-Wurzelverzeichnis. Bitte keinen äußeren Ordner mitpacken.");
        Manifest m=VorlagenMarkdown.json(files.get("manifest.json"),Manifest.class,"manifest.json");
        if(!"kanucontrol-vorlagen".equals(m.format()) || m.formatVersion()!=1)throw fehler("manifest.json: Nicht unterstütztes Paketformat oder Formatversion.");
        if(m.quelle()==null || m.quelle().mandantBezeichnung()==null || m.mietobjekte()==null || m.vorlagen()==null || m.exportiertAm()==null)throw fehler("manifest.json: Pflichtfelder fehlen.");
        try{Instant.parse(m.exportiertAm());}catch(Exception e){throw fehler("manifest.json: exportiertAm benötigt einen UTC-Zeitstempel.");}
        var declared=new HashSet<>(Set.of("manifest.json","ANLEITUNG.md"));
        var objects=new HashSet<String>();for(var o:m.mietobjekte())if(o==null || o.schluessel()==null || !o.schluessel().matches("objekt-[a-zA-Z0-9_-]{1,40}") || o.bezeichnung()==null || !objects.add(o.schluessel()))throw fehler("manifest.json: Ungültiger/doppelter Mietobjektschlüssel.");
        var templates=new LinkedHashMap<String,Vorlage>();var used=new HashSet<String>();
        for(var v:m.vorlagen()) {
            if(v==null || !objects.contains(v.mietobjekt()) || v.vorgang()==null)throw fehler("manifest.json: Unbekanntes Mietobjekt oder Vorgang.");
            String key=v.mietobjekt()+"/"+v.vorgang();
            used.add(v.mietobjekt());
            if(templates.containsKey(key))throw fehler("manifest.json: Doppelter Vorlagenschlüssel: "+key);
            if(!Objects.equals(v.datei(),"vorlagen/"+key+".md"))throw fehler("manifest.json: Ungültiger Vorlagenpfad für "+key);
            templates.put(key,VorlagenMarkdown.lesen(referenz(files,declared,v.datei(),".md"),v.datei()));
        }
        if(!used.equals(objects))throw fehler("manifest.json: Mietobjekt ohne enthaltene Vorlage.");
        byte[] pdf=null,bild=null;String gruss=null;
        if(m.briefpapier()!=null){var p=m.briefpapier();
            pdf=referenz(files,declared,p.pdf(),".pdf");gruss=VorlagenMarkdown.utf8(referenz(files,declared,p.grussformel(),".md"),p.grussformel());
            if(p.bild()!=null){if(!p.bild().endsWith(".png") && !p.bild().endsWith(".jpg"))throw fehler("manifest.json: Bild benötigt .png oder .jpg.");bild=referenz(files,declared,p.bild(),p.bild().endsWith(".png")?".png":".jpg");}
            try{BriefpapierPdf.pruefen(pdf);BriefpapierPdf.pruefeBild(bild);pruefeText(gruss,true);if(gruss.isBlank() && bild==null)throw fehler("Grußformel oder Abschlussbild erforderlich.");}
            catch(ResponseStatusException e){throw fehler("mandant/: "+e.getReason());}
        }
        if(files.containsKey("ANLEITUNG.md"))VorlagenMarkdown.utf8(files.get("ANLEITUNG.md"),"ANLEITUNG.md");
        for(String f:files.keySet())if(!declared.contains(f))throw fehler("Nicht deklarierte Nutzdatei: "+f);
        return new Inhalt(m,pdf,gruss,bild,Collections.unmodifiableMap(templates));
    }
    private static byte[] referenz(Map<String,byte[]> files,Set<String> used,String path,String suffix) {
        pfad(path);if(!path.endsWith(suffix) || !used.add(path))throw fehler("manifest.json: Ungültige oder mehrfach referenzierte Datei: "+path);
        byte[] data=files.get(path);if(data==null)throw fehler("Manifestreferenz fehlt im Paket: "+path);return data;
    }
}
