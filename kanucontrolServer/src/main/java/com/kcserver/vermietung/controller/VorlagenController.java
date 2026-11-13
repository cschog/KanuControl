package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import com.kcserver.vermietung.service.VorlagenModell.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/vorlagen")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class VorlagenController {
    private final VorlagenService service;
    private final BuchungRepository buchungen;
    private final ReservierungsbestaetigungPdf pdf;
    private final VorlagenVersandRepository archiv;
    private final AbrechnungVorlagenService abrechnung;
    private <T> ApiResponse<T> response(T value){return new ApiResponse<>(value,List.of());}
    @GetMapping("/katalog") public ApiResponse<Map<String,Object>> katalog(){VorlagenService.tenant();return response(Map.of("felder",new TreeSet<>(VorlagenModell.FELDER),"bedingungen",Bedingung.values(),"vorgaenge",Vorgang.values(),"pflichtfelder",new TreeSet<>(BriefpapierPdf.PFLICHT)));}
    @GetMapping("/briefpapier") public ApiResponse<Briefpapier> briefpapier(){return response(service.briefpapier());}
    @PutMapping(value="/briefpapier",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Briefpapier> speichern(@RequestParam(required=false) Long version,@RequestParam String gruss,
        @RequestParam(required=false) MultipartFile pdf,@RequestParam(required=false) MultipartFile bild,@RequestParam(defaultValue="false") boolean bildEntfernen,@RequestParam String pruefung)throws java.io.IOException{
        if(pdf!=null && pdf.getSize()>5_000_000 || bild!=null && bild.getSize()>1_000_000)throw VorlagenModell.fehler("Datei überschreitet die erlaubte Größe.");
        return response(service.speichern(version,gruss,pdf==null?null:pdf.getBytes(),bild==null?null:bild.getBytes(),bildEntfernen,pruefung));
    }
    @PostMapping(value="/briefpapier/vorschau",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VorlagenService.Probe>> probe(@RequestParam(required=false) Long version,@RequestParam String gruss,
        @RequestParam(required=false) MultipartFile pdf,@RequestParam(required=false) MultipartFile bild,@RequestParam(defaultValue="false") boolean bildEntfernen)throws java.io.IOException {
        if(pdf!=null && pdf.getSize()>5_000_000 || bild!=null && bild.getSize()>1_000_000)throw VorlagenModell.fehler("Datei überschreitet die erlaubte Größe.");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response(service.probe(version,gruss,pdf==null?null:pdf.getBytes(),bild==null?null:bild.getBytes(),bildEntfernen)));
    }
    @GetMapping("/{objekt}/{vorgang}") public ApiResponse<Vorlage> vorlage(@PathVariable Long objekt,@PathVariable Vorgang vorgang){return response(service.vorlage(objekt,vorgang));}
    @PutMapping("/{objekt}/{vorgang}") public ApiResponse<Vorlage> speichern(@PathVariable Long objekt,@PathVariable Vorgang vorgang,@RequestBody Vorlage v){return response(service.speichern(objekt,vorgang,v));}
    public record BeispielEntwurf(Vorlage vorlage,boolean ogGebucht,boolean kurzfristig){}
    @PostMapping(value="/{objekt}/{vorgang}/beispiel-vorschau",consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String,Object>>> beispielVorschau(@PathVariable Long objekt,@PathVariable Vorgang vorgang,@RequestBody BeispielEntwurf e){
        var r=service.beispielVorschau(objekt,vorgang,e.vorlage(),e.ogGebucht(),e.kurzfristig());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response(Map.of("pdf",r.pdf(),"betreff",r.betreff(),"text",r.mailtext())));
    }
    @PostMapping(value="/{objekt}/{vorgang}/beispiel-vorschau",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String,Object>>> beispielVorschauMitBriefpapier(@PathVariable Long objekt,@PathVariable Vorgang vorgang,
            @RequestPart("vorlage") Vorlage vorlage,@RequestParam(defaultValue="false") boolean ogGebucht,@RequestParam(defaultValue="false") boolean kurzfristig,
            @RequestParam(required=false) Long version,@RequestParam String gruss,@RequestParam(required=false) MultipartFile pdf,
            @RequestParam(required=false) MultipartFile bild,@RequestParam(defaultValue="false") boolean bildEntfernen)throws java.io.IOException {
        if(pdf!=null && pdf.getSize()>5_000_000 || bild!=null && bild.getSize()>1_000_000)throw VorlagenModell.fehler("Datei überschreitet die erlaubte Größe.");
        var r=service.beispielVorschauMitBriefpapier(objekt,vorgang,vorlage,ogGebucht,kurzfristig,version,gruss,
                pdf==null?null:pdf.getBytes(),bild==null?null:bild.getBytes(),bildEntfernen);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response(Map.of("pdf",r.pdf(),"betreff",r.betreff(),"text",r.mailtext())));
    }
    @PostMapping(value="/{objekt}/{vorgang}/beispiel-szenario",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String,Object>>> szenario(@PathVariable Long objekt,@PathVariable Vorgang vorgang,
        @RequestPart("vorlage") Vorlage vorlage,@RequestParam String szenario,@RequestParam(defaultValue="false") boolean ogGebucht,
        @RequestParam(defaultValue="false") boolean kurzfristig,@RequestParam(required=false) Long version,@RequestParam String gruss,
        @RequestParam(required=false) MultipartFile pdf,@RequestParam(required=false) MultipartFile bild,@RequestParam(defaultValue="false") boolean bildEntfernen)throws java.io.IOException{
        if(pdf!=null&&pdf.getSize()>5_000_000 || bild!=null&&bild.getSize()>1_000_000)throw VorlagenModell.fehler("Datei überschreitet die erlaubte Größe.");
        var r=service.beispielVorschauSzenario(objekt,vorgang,vorlage,ogGebucht,kurzfristig,szenario,true,version,gruss,pdf==null?null:pdf.getBytes(),bild==null?null:bild.getBytes(),bildEntfernen);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response(Map.of("pdf",r.pdf(),"betreff",r.betreff(),"text",r.mailtext())));
    }
    public record Entwurf(Long buchungId,Vorlage vorlage){}
    @PostMapping("/{objekt}/RESERVIERUNG/vorschau") @Transactional(readOnly=true)
    public ResponseEntity<ApiResponse<Map<String,Object>>> vorschau(@PathVariable Long objekt,@RequestBody Entwurf e){
        VorlagenService.tenant();var b=buchungen.findById(e.buchungId()).orElseThrow();
        if(!Objects.equals(b.getMietobjekt().getId(),objekt))throw VorlagenModell.fehler("Beispielbuchung gehört nicht zum Mietobjekt.");
        var r=pdf.entwurf(b,e.vorlage());return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response(Map.of("pdf",r.pdf(),"betreff",r.betreff(),"text",r.mailtext())));
    }
    @PostMapping("/{objekt}/ABGERECHNET/vorschau") @Transactional(readOnly=true)
    public ResponseEntity<ApiResponse<Map<String,Object>>> abrechnungVorschau(@PathVariable Long objekt,@RequestBody Entwurf e){
        VorlagenService.tenant();var b=buchungen.findById(e.buchungId()).orElseThrow();
        if(!Objects.equals(b.getMietobjekt().getId(),objekt))throw VorlagenModell.fehler("Beispielbuchung gehört nicht zum Mietobjekt.");
        if(b.getStatus()!=com.kcserver.vermietung.enumtype.Buchungsstatus.ABGESCHLOSSEN || b.getAbrechnungRevision()!=b.getAbschlussRevision())throw VorlagenModell.fehler("Buchung zuerst im aktuellen Stand abschließen.");
        byte[] bytes=abrechnung.erstellen(b,1,e.vorlage());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response(Map.of("pdf",bytes,"betreff","Abrechnung "+b.getBuchungsnummer(),"text","Vorschau der Abrechnung. Kein Versand.")));
    }
    @GetMapping("/archiv/{buchung}") @Transactional(readOnly=true)
    public ResponseEntity<ApiResponse<List<Map<String,Object>>>> archiv(@PathVariable Long buchung){
        VorlagenService.tenant();return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response(archiv.findByBuchungIdOrderByErstelltAmDesc(buchung).stream().map(a->{Map<String,Object> m=new LinkedHashMap<>();m.put("id",a.getId());m.put("erstelltAm",a.getErstelltAm());m.put("ergebnis",a.getErgebnis());m.put("smtpAm",a.getSmtpAm());m.put("vorgang",a.getVorgang());return m;}).toList()));
    }
    @GetMapping("/archiv/nachricht/{id}") @Transactional(readOnly=true)
    public ResponseEntity<ApiResponse<Map<String,Object>>> archivNachricht(@PathVariable String id){
        VorlagenService.tenant();var a=archiv.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
        var result=new LinkedHashMap<String,Object>();result.put("vorgang",a.getVorgang());result.put("ergebnis",a.getErgebnis());result.put("fehler",a.getFehler());
        try{var node=new com.fasterxml.jackson.databind.ObjectMapper().readTree(a.getMetadaten());
            if(node.isObject())for(String key:List.of("betreff","mailtext","routing","dateiname","externAm","pdfHash","quelle","ziel","storno","benutzer","mieter","pruefsumme","simulation","herkunft","dokumentart"))if(node.has(key))result.put(key,node.get(key));
        }catch(com.fasterxml.jackson.core.JsonProcessingException ignored){/* Older archive metadata was not structured JSON. */}
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response(result));
    }
    @GetMapping("/archiv/datei/{id}") @Transactional(readOnly=true)
    public ResponseEntity<byte[]> archivPdf(@PathVariable String id){VorlagenService.tenant();var a=archiv.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\"Versand.pdf\"").body(a.getPdf());}
}
