package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.VerbrauchskostenDTO;
import com.kcserver.vermietung.service.VerbrauchskostenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vermietung/objekte/{mietobjektId}/verbrauchskosten")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(
        name = "Verbrauchskosten",
        description = "Verwaltung der Verbrauchskosten eines Mietobjekts"
)
@SecurityRequirement(name = "bearerAuth")
public class VerbrauchskostenController {

    private final VerbrauchskostenService verbrauchskostenService;

    public VerbrauchskostenController(
            VerbrauchskostenService verbrauchskostenService
    ) {
        this.verbrauchskostenService = verbrauchskostenService;
    }

    @GetMapping
    public ApiResponse<List<VerbrauchskostenDTO>> getAll(
            @PathVariable Long mietobjektId
    ) {
        return new ApiResponse<>(
                verbrauchskostenService.getAll(mietobjektId),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<VerbrauchskostenDTO> getById(
            @PathVariable Long mietobjektId,
            @PathVariable Long id
    ) {
        return new ApiResponse<>(
                verbrauchskostenService.getById(mietobjektId, id),
                List.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VerbrauchskostenDTO> create(
            @PathVariable Long mietobjektId,
            @Valid @RequestBody VerbrauchskostenDTO dto
    ) {
        dto.setMietobjektId(mietobjektId);

        return new ApiResponse<>(
                verbrauchskostenService.create(dto),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<VerbrauchskostenDTO> update(
            @PathVariable Long mietobjektId,
            @PathVariable Long id,
            @Valid @RequestBody VerbrauchskostenDTO dto
    ) {
        dto.setMietobjektId(mietobjektId);

        return new ApiResponse<>(
                verbrauchskostenService.update(mietobjektId, id, dto),
                List.of()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long mietobjektId,
            @PathVariable Long id
    ) {
        verbrauchskostenService.delete(mietobjektId, id);
    }
}