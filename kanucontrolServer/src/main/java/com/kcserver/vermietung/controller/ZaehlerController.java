package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.ZaehlerDTO;
import com.kcserver.vermietung.service.ZaehlerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vermietung/objekte/{mietobjektId}/zaehler")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(
        name = "Zähler",
        description = "Verwaltung der Zähler eines Mietobjekts"
)
@SecurityRequirement(name = "bearerAuth")
public class ZaehlerController {

    private final ZaehlerService zaehlerService;

    public ZaehlerController(ZaehlerService zaehlerService) {
        this.zaehlerService = zaehlerService;
    }

    @GetMapping
    public ApiResponse<List<ZaehlerDTO>> getAll(
            @PathVariable Long mietobjektId
    ) {
        return new ApiResponse<>(
                zaehlerService.getAll(mietobjektId),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ZaehlerDTO> getById(
            @PathVariable Long mietobjektId,
            @PathVariable Long id
    ) {
        return new ApiResponse<>(
                zaehlerService.getById(mietobjektId, id),
                List.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ZaehlerDTO> create(
            @PathVariable Long mietobjektId,
            @Valid @RequestBody ZaehlerDTO dto
    ) {
        return new ApiResponse<>(
                zaehlerService.create(mietobjektId, dto),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<ZaehlerDTO> update(
            @PathVariable Long mietobjektId,
            @PathVariable Long id,
            @Valid @RequestBody ZaehlerDTO dto
    ) {
        return new ApiResponse<>(
                zaehlerService.update(mietobjektId, id, dto),
                List.of()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long mietobjektId,
            @PathVariable Long id
    ) {
        zaehlerService.delete(mietobjektId, id);
    }
}