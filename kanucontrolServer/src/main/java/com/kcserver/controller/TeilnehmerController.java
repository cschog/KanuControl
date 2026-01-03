package com.kcserver.controller;

import com.kcserver.dto.TeilnehmerDTO;
import com.kcserver.service.TeilnehmerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teilnehmer")
public class TeilnehmerController {

    private final TeilnehmerService teilnehmerService;

    public TeilnehmerController(TeilnehmerService teilnehmerService) {
        this.teilnehmerService = teilnehmerService;
    }

    /* =========================================================
       READ
       ========================================================= */

    @GetMapping
    public List<TeilnehmerDTO> getTeilnehmerDerAktivenVeranstaltung() {
        return teilnehmerService.getTeilnehmerDerAktivenVeranstaltung();
    }

    /* =========================================================
       CREATE (UC-T1)
       ========================================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeilnehmerDTO addTeilnehmer(
            @RequestBody @Valid TeilnehmerDTO dto
    ) {
        return teilnehmerService.addTeilnehmerZurAktivenVeranstaltung(dto);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{personId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeilnehmer(
            @PathVariable Long personId
    ) {
        teilnehmerService.removeTeilnehmerVonAktiverVeranstaltung(personId);
    }

    /* =========================================================
       LEITER
       ========================================================= */

    @PutMapping("/{personId}/leiter")
    public TeilnehmerDTO setLeiter(
            @PathVariable Long personId
    ) {
        return teilnehmerService.setLeiterDerAktivenVeranstaltung(personId);
    }
}