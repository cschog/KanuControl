package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.service.TeilnehmerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teilnehmer")
public class TeilnehmerController {

    private final TeilnehmerService teilnehmerService;

    public TeilnehmerController(TeilnehmerService teilnehmerService) {
        this.teilnehmerService = teilnehmerService;
    }

    /* =========================================================
       READ (Paging)
       ========================================================= */

    @GetMapping
    public Page<TeilnehmerListDTO> getTeilnehmerDerAktivenVeranstaltung(
            @PageableDefault(size = 20, sort = "person.name") Pageable pageable
    ) {
        return teilnehmerService.getTeilnehmerDerAktivenVeranstaltung(pageable);
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping("/{personId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TeilnehmerDetailDTO addTeilnehmer(
            @PathVariable Long personId
    ) {
        return teilnehmerService.addTeilnehmerZurAktivenVeranstaltung(personId);
    }

    /* =========================================================
       DELETE (single)
       ========================================================= */

    @DeleteMapping("/{personId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeilnehmer(
            @PathVariable Long personId
    ) {
        teilnehmerService.removeTeilnehmerVonAktiverVeranstaltung(personId);
    }

    /* =========================================================
       DELETE (bulk)
       ========================================================= */

    @DeleteMapping("/bulk")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeilnehmerBulk(
            @Valid @RequestBody TeilnehmerBulkDeleteDTO dto
    ) {
        teilnehmerService.removeTeilnehmerBulkVonAktiverVeranstaltung(
                dto.getPersonIds()
        );
    }

    /* =========================================================
       LEITER
       ========================================================= */

    @PutMapping("/{personId}/leiter")
    @ResponseStatus(HttpStatus.OK)
    public TeilnehmerDetailDTO setLeiter(
            @PathVariable Long personId
    ) {
        return teilnehmerService.setLeiterDerAktivenVeranstaltung(personId);
    }
}