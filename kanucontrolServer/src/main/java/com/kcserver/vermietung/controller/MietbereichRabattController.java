package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.MietbereichRabattDTO;
import com.kcserver.vermietung.dto.MietbereichRabattSaveDTO;
import com.kcserver.vermietung.service.MietbereichRabattService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/vermietung/objekte/{mietobjektId}/bereiche/{mietbereichId}/rabatte"
)
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(
        name = "Mietbereich-Rabatte",
        description = "Verwaltung der Standardrabatte eines Mietbereichs"
)
@SecurityRequirement(name = "bearerAuth")
public class MietbereichRabattController {

    private final MietbereichRabattService mietbereichRabattService;

    public MietbereichRabattController(
            MietbereichRabattService mietbereichRabattService
    ) {
        this.mietbereichRabattService = mietbereichRabattService;
    }

    @GetMapping
    public ApiResponse<List<MietbereichRabattDTO>> getAll(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId
    ) {
        return new ApiResponse<>(
                mietbereichRabattService.getAll(mietobjektId, mietbereichId),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<MietbereichRabattDTO> getById(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId,
            @PathVariable Long id
    ) {
        return new ApiResponse<>(
                mietbereichRabattService.getById(
                        mietobjektId,
                        mietbereichId,
                        id
                ),
                List.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MietbereichRabattDTO> create(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId,
            @Valid @RequestBody MietbereichRabattSaveDTO dto
    ) {
        return new ApiResponse<>(
                mietbereichRabattService.create(
                        mietobjektId,
                        mietbereichId,
                        dto
                ),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<MietbereichRabattDTO> update(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId,
            @PathVariable Long id,
            @Valid @RequestBody MietbereichRabattSaveDTO dto
    ) {
        return new ApiResponse<>(
                mietbereichRabattService.update(
                        mietobjektId,
                        mietbereichId,
                        id,
                        dto
                ),
                List.of()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId,
            @PathVariable Long id
    ) {
        mietbereichRabattService.delete(
                mietobjektId,
                mietbereichId,
                id
        );
    }
}