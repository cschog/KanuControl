package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.MietobjektDTO;
import com.kcserver.vermietung.service.MietobjektService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vermietung/objekte")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
public class MietobjektController {

    private final MietobjektService mietobjektService;

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MietobjektDTO> create(
            @Valid @RequestBody MietobjektDTO dto
    ) {
        return ApiResponse.of(
                mietobjektService.create(dto)
        );
    }

    /* =========================================================
       LISTE
       ========================================================= */

    @GetMapping
    public ApiResponse<List<MietobjektDTO>> getAll() {

        return ApiResponse.of(
                mietobjektService.getAll()
        );
    }

    /* =========================================================
       AKTIVES MIETOBJEKT
       ========================================================= */

    @GetMapping("/aktiv")
    public ApiResponse<MietobjektDTO> getActive() {

        return ApiResponse.of(
                mietobjektService.getActive()
        );
    }

    /* =========================================================
       DETAIL
       ========================================================= */

    @GetMapping("/{id:\\d+}")
    public ApiResponse<MietobjektDTO> getById(
            @PathVariable Long id
    ) {
        return ApiResponse.of(
                mietobjektService.getById(id)
        );
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @PutMapping("/{id}")
    public ApiResponse<MietobjektDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MietobjektDTO dto
    ) {
        return ApiResponse.of(
                mietobjektService.update(id, dto)
        );
    }

    /* =========================================================
       AKTIV SETZEN
       ========================================================= */

    @PutMapping("/{id}/aktiv")
    public ApiResponse<MietobjektDTO> setActive(
            @PathVariable Long id
    ) {
        return ApiResponse.of(
                mietobjektService.setActive(id)
        );
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        mietobjektService.delete(id);
    }
}