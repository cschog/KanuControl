package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.service.AnfrageMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequiredArgsConstructor @RequestMapping("/api/vermietung/altanfragen") @PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class AnfrageMigrationController {
 private final AnfrageMigrationService service;
 @GetMapping public ApiResponse<java.util.List<AnfrageMigrationService.Altbuchung>> liste(){return ApiResponse.of(service.offen());}
 public record Entscheidung(boolean ablehnen) {}
 @PostMapping("/{id}") public ApiResponse<Boolean> migrieren(@PathVariable Long id,@RequestBody Entscheidung request){service.migrieren(id,request.ablehnen());return ApiResponse.of(true);}
}
