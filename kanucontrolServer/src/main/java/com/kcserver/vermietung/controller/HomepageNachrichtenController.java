package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.HomepageStatusMailDTO;
import com.kcserver.vermietung.service.HomepageStatusMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/integrationen/homepage/objekte/{id}/nachrichten")
@PreAuthorize("hasRole('HOMEPAGE_INTEGRATION')")
public class HomepageNachrichtenController {
    private final HomepageStatusMailService service;
    @GetMapping
    public ApiResponse<List<HomepageStatusMailDTO>> offen(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.of(service.offen(id, jwt.getClaimAsString("azp")));
    }
    @PostMapping("/{nachrichtId}/quittieren")
    public ApiResponse<Map<String, Boolean>> quittieren(@PathVariable Long id, @PathVariable UUID nachrichtId,
            @AuthenticationPrincipal Jwt jwt) {
        service.quittieren(id, jwt.getClaimAsString("azp"), nachrichtId.toString());
        return ApiResponse.of(Map.of("quittiert", true));
    }
}
