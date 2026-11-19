package com.kcserver.vermietung.controller;
import com.kcserver.vermietung.service.JahresabrechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.*;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/buchungen/{id}/jahresabrechnungen")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class JahresabrechnungController {
 private final JahresabrechnungService service;
 @GetMapping public JahresabrechnungService.Uebersicht list(@PathVariable Long id){return service.list(id);}
 @GetMapping("/{jahr}/vorschau") public JahresabrechnungService.Vorschau preview(@PathVariable Long id,@PathVariable int jahr){return service.vorschau(id,jahr);}
 @PostMapping("/{jahr}") public JahresabrechnungService.Dokument create(@PathVariable Long id,@PathVariable int jahr,@RequestBody JahresabrechnungService.Auftrag request){return service.abschliessen(id,jahr,request);}
 @GetMapping("/{jahr}/pdf") public ResponseEntity<byte[]> pdf(@PathVariable Long id,@PathVariable int jahr){return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=Jahresabrechnung-"+id+"-"+jahr+".pdf").body(service.pdf(id,jahr));}
}
