package com.kcserver.vermietung.controller;
import com.kcserver.vermietung.service.VermietungAbrechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
import java.util.List;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/buchungen/{id}/abrechnungen")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class VermietungAbrechnungController {
 private final VermietungAbrechnungService service;
 @GetMapping public List<VermietungAbrechnungService.Dokument> list(@PathVariable Long id){return service.list(id);}
 @PostMapping public VermietungAbrechnungService.Dokument create(@PathVariable Long id,@RequestBody VermietungAbrechnungService.Ausstellung request){return service.create(id,request);}
 @GetMapping("/{dokumentId}/pdf") public ResponseEntity<byte[]> pdf(@PathVariable Long id,@PathVariable Long dokumentId){return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=Abrechnung-"+id+"-"+dokumentId+".pdf").body(service.pdf(id,dokumentId));}
}
