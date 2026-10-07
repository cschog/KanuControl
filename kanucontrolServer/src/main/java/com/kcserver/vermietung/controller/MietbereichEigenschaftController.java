package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.MietbereichEigenschaftDTO;
import com.kcserver.vermietung.service.MietbereichEigenschaftService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/vermietung/objekte/{mietobjektId}/bereiche/{mietbereichId}/eigenschaften"
)
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(
        name = "Mietbereich-Eigenschaften",
        description = "Verwaltung der Eigenschaften von Mietbereichen"
)
@SecurityRequirement(name = "bearerAuth")
public class MietbereichEigenschaftController {

    private final MietbereichEigenschaftService
            mietbereichEigenschaftService;

    public MietbereichEigenschaftController(
            MietbereichEigenschaftService mietbereichEigenschaftService
    ) {
        this.mietbereichEigenschaftService =
                mietbereichEigenschaftService;
    }

    @GetMapping
    public ApiResponse<List<MietbereichEigenschaftDTO>> getAll(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId
    ) {

        return new ApiResponse<>(
                mietbereichEigenschaftService.getAll(
                        mietobjektId,
                        mietbereichId
                ),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<MietbereichEigenschaftDTO> getById(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId,
            @PathVariable Long id
    ) {

        return new ApiResponse<>(
                mietbereichEigenschaftService.getById(
                        mietobjektId,
                        mietbereichId,
                        id
                ),
                List.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MietbereichEigenschaftDTO> create(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId,
            @RequestBody @Valid MietbereichEigenschaftDTO dto
    ) {

        dto.setMietbereichId(mietbereichId);

        return new ApiResponse<>(
                mietbereichEigenschaftService.create(
                        mietobjektId,
                        mietbereichId,
                        dto
                ),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<MietbereichEigenschaftDTO> update(
            @PathVariable Long mietobjektId,
            @PathVariable Long mietbereichId,
            @PathVariable Long id,
            @RequestBody @Valid MietbereichEigenschaftDTO dto
    ) {

        dto.setMietbereichId(mietbereichId);

        return new ApiResponse<>(
                mietbereichEigenschaftService.update(
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

        mietbereichEigenschaftService.delete(
                mietobjektId,
                mietbereichId,
                id
        );
    }
}