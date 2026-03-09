package com.kcserver.controller;

import com.kcserver.dto.finanz.FinanzGruppeCreateDTO;
import com.kcserver.dto.finanz.FinanzGruppeDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.mapper.FinanzGruppeMapper;
import com.kcserver.finanz.FinanzGruppeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/finanzgruppen")
@RequiredArgsConstructor
public class FinanzGruppeController {

    private final FinanzGruppeService service;
    private final FinanzGruppeMapper mapper;

    /* =========================
       GET
       ========================= */

    @GetMapping
    public List<FinanzGruppeDTO> findAll(
            @PathVariable Long veranstaltungId) {

        return service.findAll(veranstaltungId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    /* =========================
       CREATE
       ========================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinanzGruppeDTO create(
            @PathVariable Long veranstaltungId,
            @Valid @RequestBody FinanzGruppeCreateDTO dto) {

        FinanzGruppe g =
                service.create(veranstaltungId, dto.kuerzel());

        return mapper.toDTO(g);
    }

    /* =========================
       UPDATE
       ========================= */

    @PutMapping("/{gruppeId}")
    public FinanzGruppeDTO update(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId,
            @Valid @RequestBody FinanzGruppeCreateDTO dto) {

        FinanzGruppe g =
                service.update(veranstaltungId, gruppeId, dto.kuerzel());

        return mapper.toDTO(g);
    }

    /* =========================
       DELETE
       ========================= */

    @DeleteMapping("/{gruppeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId) {

        service.delete(veranstaltungId, gruppeId);
    }
}