package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.service.BuchungsnummerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/buchungen")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class BuchungsnummerController {
    private final BuchungsnummerService service;
    public record Aenderung(@NotBlank String buchungsnummer,@NotBlank String bisherigeBuchungsnummer) {}
    @PatchMapping("/{id}/buchungsnummer")
    public ApiResponse<BuchungsnummerService.Nummer> aendern(@PathVariable Long id,@RequestBody @Valid Aenderung request) {
        return ApiResponse.of(service.aendern(id,request.buchungsnummer(),request.bisherigeBuchungsnummer()));
    }
}
