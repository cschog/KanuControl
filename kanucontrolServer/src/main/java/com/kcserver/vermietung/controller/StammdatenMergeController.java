package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.service.StammdatenMergeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequiredArgsConstructor @RequestMapping("/api/vermietung/zusammenfuehren") @PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class StammdatenMergeController {
 private final StammdatenMergeService service;
 private boolean person(String art){if(!art.equals("mieter")&&!art.equals("veranstalter"))throw new IllegalArgumentException("Ungültige Stammdatenart.");return art.equals("mieter");}
 @GetMapping("/{art}") public ApiResponse<StammdatenMergeService.Vorschau> vorschau(@PathVariable String art,@RequestParam Long quelle,@RequestParam Long ziel){return ApiResponse.of(service.vorschau(person(art),quelle,ziel));}
 @PostMapping("/{art}") public ApiResponse<StammdatenMergeService.Vorschau> anwenden(@PathVariable String art,@RequestBody StammdatenMergeService.Auftrag auftrag){return ApiResponse.of(service.zusammenfuehren(person(art),auftrag));}
}
