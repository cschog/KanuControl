package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.ZaehlerstandDTO;
import com.kcserver.vermietung.service.ZaehlerstandService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vermietung/zaehler/{zaehlerId}/staende")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(
        name = "Zählerstände",
        description = "Verwaltung der Ablesungen eines Zählers"
)
@SecurityRequirement(name = "bearerAuth")
public class ZaehlerstandController {

    private final ZaehlerstandService zaehlerstandService;

    public ZaehlerstandController(
            ZaehlerstandService zaehlerstandService
    ) {
        this.zaehlerstandService = zaehlerstandService;
    }

    @GetMapping
    public ApiResponse<List<ZaehlerstandDTO>> getAll(
            @PathVariable Long zaehlerId
    ) {
        return new ApiResponse<>(
                zaehlerstandService.getAll(zaehlerId),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ZaehlerstandDTO> getById(
            @PathVariable Long zaehlerId,
            @PathVariable Long id
    ) {
        return new ApiResponse<>(
                zaehlerstandService.getById(zaehlerId, id),
                List.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ZaehlerstandDTO> create(
            @PathVariable Long zaehlerId,
            @Valid @RequestBody ZaehlerstandDTO dto
    ) {
        return new ApiResponse<>(
                zaehlerstandService.create(zaehlerId, dto),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<ZaehlerstandDTO> update(
            @PathVariable Long zaehlerId,
            @PathVariable Long id,
            @Valid @RequestBody ZaehlerstandDTO dto
    ) {
        return new ApiResponse<>(
                zaehlerstandService.update(zaehlerId, id, dto),
                List.of()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long zaehlerId,
            @PathVariable Long id
    ) {
        zaehlerstandService.delete(zaehlerId, id);
    }
}