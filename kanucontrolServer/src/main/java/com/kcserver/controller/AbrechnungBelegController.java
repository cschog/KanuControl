package com.kcserver.controller;

import com.kcserver.dto.abrechnung.*;
import com.kcserver.finanz.AbrechnungBelegService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/abrechnung/belege")
@RequiredArgsConstructor
public class AbrechnungBelegController {

    private final AbrechnungBelegService service;

    /* =========================================================
       BELEG ANLEGEN
       ========================================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbrechnungBelegDTO createBeleg(
            @PathVariable Long veranstaltungId,
            @RequestBody AbrechnungBelegCreateDTO dto
    ) {
        return service.createBeleg(veranstaltungId, dto);
    }

    /* =========================================================
       POSITION HINZUFÜGEN (Splitbuchung)
       ========================================================= */

    @PostMapping("/{belegId}/positionen")
    @ResponseStatus(HttpStatus.CREATED)
    public AbrechnungBuchungDTO addPosition(
            @PathVariable Long veranstaltungId,
            @PathVariable Long belegId,
            @RequestBody AbrechnungBuchungCreateDTO dto
    ) {
        return service.addPosition(veranstaltungId, belegId, dto);
    }

    /* =========================================================
       POSITION UPDATE
       ========================================================= */

    @PutMapping("/positionen/{positionId}")
    public AbrechnungBuchungDTO updatePosition(
            @PathVariable Long veranstaltungId,
            @PathVariable Long positionId,
            @RequestBody AbrechnungBuchungCreateDTO dto
    ) {
        return service.updatePosition(veranstaltungId, positionId, dto);
    }

    /* =========================================================
       POSITION DELETE
       ========================================================= */

    @DeleteMapping("/positionen/{positionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePosition(
            @PathVariable Long veranstaltungId,
            @PathVariable Long positionId
    ) {
        service.deletePosition(veranstaltungId, positionId);
    }

    /* =========================================================
       BELEG DELETE
       ========================================================= */

    @DeleteMapping("/{belegId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBeleg(
            @PathVariable Long veranstaltungId,
            @PathVariable Long belegId
    ) {
        service.deleteBeleg(veranstaltungId, belegId);
    }
}