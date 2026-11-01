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
@RequestMapping("/api/vermietung/zaehler")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(
        name = "Zähler",
        description = "Zentrale Verwaltung aller Zähler"
)
@SecurityRequirement(name = "bearerAuth")
public class ZaehlerController {

    private final ZaehlerService zaehlerService;

    public ZaehlerController(ZaehlerService zaehlerService) {
        this.zaehlerService = zaehlerService;
    }

    @GetMapping
    public ApiResponse<List<ZaehlerDTO>> getAll() {
        return new ApiResponse<>(
                zaehlerService.getAll(),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ZaehlerDTO> getById(
            @PathVariable Long id
    ) {
        return new ApiResponse<>(
                zaehlerService.getById(id),
                List.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ZaehlerDTO> create(
            @Valid @RequestBody ZaehlerDTO dto
    ) {
        return new ApiResponse<>(
                zaehlerService.create(dto),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<ZaehlerDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ZaehlerDTO dto
    ) {
        return new ApiResponse<>(
                zaehlerService.update(id, dto),
                List.of()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        zaehlerService.delete(id);
    }
}