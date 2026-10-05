package com.kcserver.controller.vermietung;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.vermietung.MietbereichDTO;
import com.kcserver.service.vermietung.MietbereichService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/vermietung/objekte/{mietobjektId}/bereiche")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(
        name = "Mietbereich",
        description = "Verwaltung von Mietbereichen"
)
@SecurityRequirement(name = "bearerAuth")
public class MietbereichController {

    private final MietbereichService mietbereichService;

    public MietbereichController(
            MietbereichService mietbereichService
    ) {
        this.mietbereichService = mietbereichService;
    }

    @GetMapping
    public ApiResponse<List<MietbereichDTO>> getAll(
            @PathVariable Long mietobjektId
    ) {

        return new ApiResponse<>(
                mietbereichService.getAll(mietobjektId),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<MietbereichDTO> getById(
            @PathVariable Long mietobjektId,
            @PathVariable Long id
    ) {

        return new ApiResponse<>(
                mietbereichService.getById(id),
                List.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MietbereichDTO> create(
            @PathVariable Long mietobjektId,
            @RequestBody @Valid MietbereichDTO dto
    ) {

        dto.setMietobjektId(mietobjektId);

        return new ApiResponse<>(
                mietbereichService.create(dto),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<MietbereichDTO> update(
            @PathVariable Long mietobjektId,
            @PathVariable Long id,
            @RequestBody @Valid MietbereichDTO dto
    ) {

        dto.setMietobjektId(mietobjektId);

        return new ApiResponse<>(
                mietbereichService.update(id, dto),
                List.of()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long mietobjektId,
            @PathVariable Long id
    ) {

        mietbereichService.delete(id);
    }
}