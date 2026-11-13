package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.service.BuchungsdokumentService;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/buchungen/{buchung}/dokumente")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class BuchungsdokumentController {

    private final BuchungsdokumentService service;

    private <T> ResponseEntity<ApiResponse<T>> ok(T value){
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new ApiResponse<>(value,List.of()));
    }

    public record Vorschau(Buchungsstatus art){}
    @GetMapping public ResponseEntity<ApiResponse<BuchungsdokumentService.Uebersicht>> liste(@PathVariable Long buchung){
        return ok(service.liste(buchung));
    }

    @PostMapping("/vorschau") public ResponseEntity<ApiResponse<BuchungsdokumentService.Fassung>> vorschau(@PathVariable Long buchung,@RequestBody Vorschau request){
        return ok(service.vorschau(buchung,request.art()));
    }

    @GetMapping("/{id}") public ResponseEntity<ApiResponse<BuchungsdokumentService.Fassung>> lesen(@PathVariable Long buchung,@PathVariable String id){
        return ok(service.lesen(buchung,id));
    }

    @PostMapping("/{id}/speichern") public ResponseEntity<ApiResponse<BuchungsdokumentService.Fassung>> speichern(@PathVariable Long buchung,@PathVariable String id,@RequestBody BuchungsdokumentService.Speichern request){
        return ok(service.speichern(buchung,id,request.ersetztId()));
    }

    @PostMapping(value="/import",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BuchungsdokumentService.Fassung>> importieren(@PathVariable Long buchung,@RequestParam Buchungsstatus art,@RequestParam(required=false) String ersetztId,@RequestParam MultipartFile datei)throws java.io.IOException{

        if(datei.getSize()>BuchungsdokumentService.UPLOAD_LIMIT)throw com.kcserver.vermietung.service.VorlagenModell.fehler("PDF darf höchstens 10 MB groß sein.");

        return ok(service.importieren(buchung,art,ersetztId,datei.getBytes(),datei.getOriginalFilename()));

    }

    @PostMapping("/{id}/mail") public ResponseEntity<ApiResponse<BuchungsdokumentService.Versandvorbereitung>> vorbereiten(@PathVariable Long buchung,@PathVariable String id){
        return ok(service.vorbereiten(buchung,id));
    }

    @PostMapping("/{id}/senden") public ResponseEntity<ApiResponse<BuchungsdokumentService.Versand>> senden(@PathVariable Long buchung,@PathVariable String id,@RequestBody BuchungsdokumentService.Senden request){
        return ok(service.senden(buchung,id,request));
    }

}
