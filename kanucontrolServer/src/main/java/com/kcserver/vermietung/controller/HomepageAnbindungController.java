package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.HomepageAnbindungDTO;
import com.kcserver.vermietung.service.HomepageAnbindungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vermietung/objekte/{mietobjektId}/homepage-anbindung")
public class HomepageAnbindungController {
    private final HomepageAnbindungService service;

    private final com.kcserver.vermietung.service.HausmeisterBerichtService berichte;

    @GetMapping("/hausmeister-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
    public ApiResponse<java.util.Map<String, Object>> berichtStatus(@PathVariable Long mietobjektId) {
        return ApiResponse.of(berichte.status());
    }

    @PostMapping("/hausmeister-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> test(@PathVariable Long mietobjektId) {
        berichte.testSenden(mietobjektId);
        return ApiResponse.of("Testbericht an den Mailserver übergeben.");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
    public ApiResponse<HomepageAnbindungDTO> get(@PathVariable Long mietobjektId) {
        return ApiResponse.of(service.get(mietobjektId));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HomepageAnbindungDTO> save(@PathVariable Long mietobjektId,
            @Valid @RequestBody HomepageAnbindungDTO dto) {
        return ApiResponse.of(service.save(mietobjektId, dto));
    }
}
