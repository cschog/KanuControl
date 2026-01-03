package com.kcserver.controller;

import com.kcserver.dto.VeranstaltungDTO;
import com.kcserver.service.VeranstaltungService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veranstaltung")
public class VeranstaltungController {

    private final VeranstaltungService veranstaltungService;

    public VeranstaltungController(VeranstaltungService veranstaltungService) {
        this.veranstaltungService = veranstaltungService;
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeranstaltungDTO create(@Valid @RequestBody VeranstaltungDTO dto) {
        return veranstaltungService.create(dto);
    }

    /* =========================================================
       READ
       ========================================================= */

    @GetMapping
    public List<VeranstaltungDTO> getAll() {
        return veranstaltungService.getAll();
    }

    @GetMapping("/active")
    public VeranstaltungDTO getActive() {
        return veranstaltungService.getActive();
    }

    @GetMapping("/{id}")
    public VeranstaltungDTO getById(@PathVariable Long id) {
        return veranstaltungService.getById(id);
    }

    /* =========================================================
       STATE
       ========================================================= */

    @PostMapping("/beenden")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void beenden() {
        veranstaltungService.beenden();
    }

    @PostMapping("/{id}/aktivieren")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aktivieren(@PathVariable Long id) {
        veranstaltungService.aktivieren(id);
    }
}