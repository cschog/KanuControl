package com.kcserver.controller;

import com.kcserver.dto.VeranstaltungDTO;
import com.kcserver.service.VeranstaltungService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veranstaltungen")
public class VeranstaltungController {

    private final VeranstaltungService veranstaltungService;

    public VeranstaltungController(VeranstaltungService veranstaltungService) {
        this.veranstaltungService = veranstaltungService;
    }

    /* =========================================================
       READ
       ========================================================= */

    @GetMapping
    public ResponseEntity<List<VeranstaltungDTO>> getAll() {
        return ResponseEntity.ok(veranstaltungService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<VeranstaltungDTO> getActive() {
        return ResponseEntity.ok(veranstaltungService.getActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeranstaltungDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(veranstaltungService.getById(id));
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping
    public ResponseEntity<VeranstaltungDTO> create(
            @Valid @RequestBody VeranstaltungDTO dto
    ) {
        VeranstaltungDTO created = veranstaltungService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* =========================================================
       STATE MANAGEMENT
       ========================================================= */

    @PostMapping("/beenden")
    public ResponseEntity<Void> beenden() {
        veranstaltungService.beenden();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/aktivieren")
    public ResponseEntity<Void> aktivieren(@PathVariable Long id) {
        veranstaltungService.aktivieren(id);
        return ResponseEntity.noContent().build();
    }
}