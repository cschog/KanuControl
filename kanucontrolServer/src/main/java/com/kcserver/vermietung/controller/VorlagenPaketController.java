package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.io.*;
import java.time.*;
import java.util.*;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/vorlagen/pakete")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class VorlagenPaketController {
    private final VorlagenPaketService service;
    private <T> ResponseEntity<ApiResponse<T>> response(T value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new ApiResponse<>(value,List.of()));}
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> paketFehler(org.springframework.web.server.ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).cacheControl(CacheControl.noStore()).body(Map.of("message",e.getReason()==null?"Import konnte nicht verarbeitet werden.":e.getReason()));}
    @ExceptionHandler({org.springframework.dao.CannotAcquireLockException.class,org.springframework.orm.ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<Map<String,String>> konflikt(RuntimeException e){return ResponseEntity.status(HttpStatus.CONFLICT).cacheControl(CacheControl.noStore()).body(Map.of("message","Konfiguration wird gleichzeitig geändert. Bitte Plan und Vorschau erneut prüfen."));}
    @GetMapping("/export") public ResponseEntity<byte[]> exportieren(){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType("application/zip")).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"KanuControl-Vorlagen-"+LocalDate.now(ZoneOffset.UTC)+".zip\"").body(service.exportieren());}
    @PostMapping(value="/importe",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<VorlagenPaketService.Ansicht>> hochladen(@RequestParam MultipartFile datei)throws IOException {
        if(datei.getSize()>VorlagenPaket.ZIP_MAX)throw new org.springframework.web.server.ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"ZIP darf höchstens 25 MB groß sein.");
        byte[] bytes;try(var in=datei.getInputStream()){bytes=in.readNBytes(VorlagenPaket.ZIP_MAX+1);}if(bytes.length>VorlagenPaket.ZIP_MAX)throw new org.springframework.web.server.ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"ZIP darf höchstens 25 MB groß sein.");
        return response(service.hochladen(bytes));
    }
    @GetMapping("/importe/{id}") public ResponseEntity<ApiResponse<VorlagenPaketService.Ansicht>> lesen(@PathVariable String id){return response(service.lesen(id));}
    @PutMapping("/importe/{id}/plan") public ResponseEntity<ApiResponse<VorlagenPaketService.Ansicht>> planen(@PathVariable String id,@RequestBody VorlagenPaketService.Plan plan){return response(service.planen(id,plan));}
    @PostMapping("/importe/{id}/vorschau") public ResponseEntity<ApiResponse<VorlagenPaketService.Vorschau>> vorschau(@PathVariable String id){return response(service.vorschau(id));}
    @PostMapping("/importe/{id}/uebernehmen") public ResponseEntity<ApiResponse<VorlagenPaketService.Ansicht>> uebernehmen(@PathVariable String id){return response(service.uebernehmen(id));}
    @DeleteMapping("/importe/{id}") public ResponseEntity<ApiResponse<Boolean>> abbrechen(@PathVariable String id){service.abbrechen(id);return response(true);}
}
