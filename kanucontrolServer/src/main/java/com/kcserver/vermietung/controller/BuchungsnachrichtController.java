package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.service.BuchungsnachrichtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/buchungen/{buchung}/nachrichten")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class BuchungsnachrichtController {
 private final BuchungsnachrichtService service;
 private <T> ResponseEntity<ApiResponse<T>> ok(T value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new ApiResponse<>(value,List.of()));}
 @PostMapping public ResponseEntity<ApiResponse<BuchungsnachrichtService.Ansicht>> erstellen(@PathVariable Long buchung,@RequestBody @jakarta.validation.Valid BuchungsnachrichtService.Start request){return ok(service.erstellen(buchung,request));}
 @PostMapping("/vorschau") public ResponseEntity<ApiResponse<BuchungsnachrichtService.Vorschau>> vorschau(@PathVariable Long buchung,@RequestBody @jakarta.validation.Valid BuchungsnachrichtService.Start request){return ok(service.vorschau(buchung,request));}
 @GetMapping public ResponseEntity<ApiResponse<List<BuchungsnachrichtService.Kurz>>> liste(@PathVariable Long buchung){return ok(service.liste(buchung));}
 @GetMapping("/{id}") public ResponseEntity<ApiResponse<BuchungsnachrichtService.Ansicht>> lesen(@PathVariable Long buchung,@PathVariable String id){return ok(service.lesen(buchung,id));}
 @PutMapping("/{id}") public ResponseEntity<ApiResponse<BuchungsnachrichtService.Ansicht>> aendern(@PathVariable Long buchung,@PathVariable String id,@RequestBody BuchungsnachrichtService.Aenderung request){return ok(service.aendern(buchung,id,request));}
 @PostMapping(value="/{id}/pdf",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public ResponseEntity<ApiResponse<BuchungsnachrichtService.Ansicht>> pdf(@PathVariable Long buchung,@PathVariable String id,@RequestParam Long version,@RequestParam MultipartFile datei)throws java.io.IOException{
  if(datei.getSize()>10_000_000)throw com.kcserver.vermietung.service.VorlagenModell.fehler("PDF darf höchstens 10 MB groß sein.");return ok(service.hochladen(buchung,id,version,datei.getBytes(),datei.getOriginalFilename()));
 }
 @PostMapping("/{id}/ausfuehren") public ResponseEntity<ApiResponse<BuchungsnachrichtService.Ansicht>> ausfuehren(@PathVariable Long buchung,@PathVariable String id,@RequestBody BuchungsnachrichtService.Freigabe request){return ok(service.ausfuehren(buchung,id,request));}
}
