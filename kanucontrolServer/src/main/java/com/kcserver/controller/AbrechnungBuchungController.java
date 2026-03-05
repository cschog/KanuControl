package com.kcserver.controller;

import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungDTO;
import com.kcserver.finanz.AbrechnungBuchungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/abrechnung/buchungen")
@RequiredArgsConstructor
public class AbrechnungBuchungController {

    private final AbrechnungBuchungService service;

    /* =========================================================
       ADD
       ========================================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbrechnungBuchungDTO add(
            @PathVariable Long veranstaltungId,
            @RequestBody AbrechnungBuchungCreateDTO dto
    ) {
        return service.addBuchung(veranstaltungId, dto);
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @PutMapping("/{buchungId}")
    public AbrechnungBuchungDTO update(
            @PathVariable Long veranstaltungId,
            @PathVariable Long buchungId,
            @RequestBody AbrechnungBuchungCreateDTO dto
    ) {
        return service.updateBuchung(veranstaltungId, buchungId, dto);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{buchungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long veranstaltungId,
            @PathVariable Long buchungId
    ) {
        service.deleteBuchung(veranstaltungId, buchungId);
    }
}