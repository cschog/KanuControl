package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.abrechnung.*;
import com.kcserver.finanz.AbrechnungBelegService;
import jakarta.validation.Valid;
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

    @PostMapping("/mit-buchung")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AbrechnungBelegDTO> createBelegMitBuchung(
            @PathVariable Long veranstaltungId,
            @RequestBody @Valid AbrechnungBelegMitBuchungCreateDTO dto
    ) {
        return ApiResponse.of(
                service.createBelegMitBuchung(
                        veranstaltungId,
                        dto.getBeleg(),
                        dto.getBuchung()
                )
        );
    }

    /* =========================================================
       BELEG UPDATE
       ========================================================= */

    @PutMapping("/{belegId}")
    public ApiResponse<AbrechnungBelegDTO> updateBeleg(
            @PathVariable Long veranstaltungId,
            @PathVariable Long belegId,
            @RequestBody @Valid AbrechnungBelegCreateDTO dto
    ) {
        return ApiResponse.of(
                service.updateBeleg(
                        veranstaltungId,
                        belegId,
                        dto
                )
        );
    }

    /* =========================================================
       POSITION HINZUFÜGEN (Splitbuchung)
       ========================================================= */

    @PostMapping("/{belegId}/positionen")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AbrechnungBuchungDTO> addPosition(
            @PathVariable Long veranstaltungId,
            @PathVariable Long belegId,
            @RequestBody @Valid AbrechnungBuchungCreateDTO dto
    ) {
        return ApiResponse.of(
                service.addPosition(
                        veranstaltungId,
                        belegId,
                        dto
                )
        );
    }

    /* =========================================================
       POSITION UPDATE
       ========================================================= */

    @PutMapping("/positionen/{positionId}")
    public ApiResponse<AbrechnungBuchungDTO> updatePosition(
            @PathVariable Long veranstaltungId,
            @PathVariable Long positionId,
            @RequestBody @Valid AbrechnungBuchungCreateDTO dto
    ) {
        return ApiResponse.of(
                service.updatePosition(
                        veranstaltungId,
                        positionId,
                        dto
                )
        );
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

    /* =========================================================
       KÜRZEL ÄNDERN
       ========================================================= */

    @PutMapping("/{belegId}/kuerzel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeKuerzel(
            @PathVariable Long veranstaltungId,
            @PathVariable Long belegId,
            @RequestParam String kuerzel
    ) {
        service.changeKuerzel(
                veranstaltungId,
                belegId,
                kuerzel
        );
    }
}