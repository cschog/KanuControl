package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.BuchungDTO;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.service.BuchungService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vermietung/buchungen")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(
        name = "Buchungen",
        description = "Verwaltung der Vermietungsbuchungen"
)
@SecurityRequirement(name = "bearerAuth")
public class BuchungController {

    private final BuchungService buchungService;

    public BuchungController(
            BuchungService buchungService
    ) {
        this.buchungService = buchungService;
    }

    @GetMapping
    public ApiResponse<List<BuchungDTO>> getAll() {

        return new ApiResponse<>(
                buchungService.getAll(),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<BuchungDTO> getById(
            @PathVariable Long id
    ) {

        return new ApiResponse<>(
                buchungService.getById(id),
                List.of()
        );
    }

    @GetMapping("/preis")
    public ApiResponse<java.math.BigDecimal> getPreis(
            @RequestParam Long mietbereichId,
            @RequestParam com.kcserver.vermietung.enumtype.Buchungsquelle buchungsquelle,
            @RequestParam java.time.LocalDate stichtag
    ) {
        return new ApiResponse<>(
                buchungService.getPreis(
                        mietbereichId,
                        buchungsquelle,
                        stichtag
                ),
                List.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BuchungDTO> create(
            @RequestBody @Valid BuchungDTO dto
    ) {

        return new ApiResponse<>(
                buchungService.create(dto),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<BuchungDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid BuchungDTO dto
    ) {

        return new ApiResponse<>(
                buchungService.update(id, dto),
                List.of()
        );
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<BuchungDTO> aendereStatus(
            @PathVariable Long id,
            @RequestBody @Valid BuchungsstatusRequest request
    ) {
        return new ApiResponse<>(
                buchungService.aendereStatus(id, request.status()),
                List.of()
        );
    }

    public record BuchungsstatusRequest(
            @jakarta.validation.constraints.NotNull
            Buchungsstatus status
    ) {
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {

        buchungService.delete(id);
    }
}