package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.HomepageIntegrationDTO.*;
import com.kcserver.vermietung.service.HomepageIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/integrationen/homepage/objekte/{id}")
@PreAuthorize("hasRole('HOMEPAGE_INTEGRATION')")
public class HomepageIntegrationController {
    private final HomepageIntegrationService service;
    @GetMapping("/katalog")
    public ApiResponse<Katalog> katalog(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.of(service.katalog(id, jwt.getClaimAsString("azp")));
    }
    @PostMapping("/verfuegbarkeit")
    public ApiResponse<Verfuegbarkeit> verfuegbarkeit(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Auswahl dto) {
        return ApiResponse.of(service.verfuegbarkeit(id, jwt.getClaimAsString("azp"), dto));
    }
    @PostMapping("/kalender")
    public ApiResponse<Kalender> kalender(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody KalenderAuswahl dto) {
        return ApiResponse.of(service.kalender(id, jwt.getClaimAsString("azp"), dto));
    }
    @PostMapping("/anfragen")
    public ApiResponse<Bestaetigung> anfrage(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody Anfrage dto) {
        return ApiResponse.of(service.anfrage(id, jwt.getClaimAsString("azp"), dto));
    }
}
