package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Foerdersatz;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.mapper.FoerdersatzMapper;
import com.kcserver.service.FoerdersatzService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/foerdersaetze")
@RequiredArgsConstructor
public class FoerdersatzController {

    private final FoerdersatzService service;
    private final FoerdersatzMapper mapper;

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FoerdersatzDTO> create(
            @Valid @RequestBody FoerdersatzCreateUpdateDTO dto
    ) {
        return ApiResponse.of(
                service.create(dto)
        );
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @PutMapping("/{id}")
    public ApiResponse<FoerdersatzDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody FoerdersatzCreateUpdateDTO dto
    ) {
        return ApiResponse.of(
                service.update(id, dto)
        );
    }

    /* =========================================================
       GET BY ID
       ========================================================= */

    @GetMapping("/{id}")
    public ApiResponse<FoerdersatzDTO> get(
            @PathVariable Long id
    ) {
        return ApiResponse.of(
                service.getById(id)
        );
    }

    /* =========================================================
       LIST ALL
       ========================================================= */

    @GetMapping
    public ApiResponse<List<FoerdersatzDTO>> list() {
        return ApiResponse.of(
                service.getAll()
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
        service.delete(id);
    }

    /* =========================================================
       GÜLTIGER FÖRDERSATZ FÜR TYP + DATUM
       ========================================================= */

    @GetMapping("/gueltig")
    public ApiResponse<FoerdersatzDTO> gueltigAm(
            @RequestParam VeranstaltungTyp typ,
            @RequestParam LocalDate datum
    ) {

        Foerdersatz entity =
                service.findEntityGueltigFuerTypAm(
                        typ,
                        datum
                );

        return ApiResponse.of(
                mapper.toDTO(entity)
        );
    }
}