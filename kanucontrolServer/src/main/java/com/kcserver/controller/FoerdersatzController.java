package com.kcserver.controller;

import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.entity.Foerdersatz;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.mapper.FoerdersatzMapper;
import com.kcserver.service.FoerdersatzService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/foerdersaetze")
public class FoerdersatzController {

    private final FoerdersatzService service;
    private final FoerdersatzMapper mapper;

    public FoerdersatzController(
            FoerdersatzService service,
            FoerdersatzMapper mapper
    ) {
        this.service = service;
        this.mapper = mapper;
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping
    public FoerdersatzDTO create(
            @Valid
            @RequestBody
            FoerdersatzCreateUpdateDTO dto
    ) {
        return service.create(dto);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @PutMapping("/{id}")
    public FoerdersatzDTO update(
            @PathVariable Long id,

            @Valid
            @RequestBody
            FoerdersatzCreateUpdateDTO dto
    ) {
        return service.update(id, dto);
    }

    /* =========================================================
       GET BY ID
       ========================================================= */

    @GetMapping("/{id}")
    public FoerdersatzDTO get(
            @PathVariable Long id
    ) {
        return service.getById(id);
    }

    /* =========================================================
       LIST ALL
       ========================================================= */

    @GetMapping
    public List<FoerdersatzDTO> list() {
        return service.getAll();
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }

    /* =========================================================
       GÜLTIGER FÖRDERSATZ FÜR TYP + DATUM
       ========================================================= */

    @GetMapping("/gueltig")
    public FoerdersatzDTO gueltigAm(
            @RequestParam VeranstaltungTyp typ,
            @RequestParam LocalDate datum
    ) {

        Foerdersatz entity =
                service.findEntityGueltigFuerTypAm(
                        typ,
                        datum
                );

        return mapper.toDTO(entity);
    }
}