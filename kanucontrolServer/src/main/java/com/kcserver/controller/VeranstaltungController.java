package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungCreateDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungListDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungUpdateDTO;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.service.VeranstaltungService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veranstaltung")
public class VeranstaltungController {

    private final VeranstaltungService veranstaltungService;
    private final TeilnehmerService teilnehmerService;   // ⭐ HINZUFÜGEN

    public VeranstaltungController(
            VeranstaltungService veranstaltungService,
            TeilnehmerService teilnehmerService           // ⭐ HINZUFÜGEN
    ) {
        this.veranstaltungService = veranstaltungService;
        this.teilnehmerService = teilnehmerService;       // ⭐ HINZUFÜGEN
    }


    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeranstaltungDetailDTO create(
            @Valid @RequestBody VeranstaltungCreateDTO dto
    ) {
        return veranstaltungService.create(dto);
    }

    /* =========================================================
       READ (LIST)
       ========================================================= */

    @GetMapping
    public Page<VeranstaltungListDTO> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean aktiv,
            Pageable pageable
    ) {
        return veranstaltungService.getAll(name, aktiv, pageable);
    }

    /* =========================================================
       READ (DETAIL)
       ========================================================= */

    @GetMapping("/active")
    public VeranstaltungDetailDTO getActive() {
        return veranstaltungService.getActive();
    }

    @GetMapping("/{id}")
    public VeranstaltungDetailDTO getById(@PathVariable Long id) {
        return veranstaltungService.getById(id);
    }

    /* =========================================================
       UPDATE
       ========================================================= */
    @PutMapping("/{id}")
    public VeranstaltungDetailDTO update(
            @PathVariable Long id,
            @RequestBody @Valid VeranstaltungUpdateDTO dto
    ) {
        return veranstaltungService.update(id, dto);
    }

     /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        veranstaltungService.delete(id);
    }

    @DeleteMapping("/{id}/teilnehmer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeilnehmerBulk(
            @PathVariable Long id,
            @RequestBody TeilnehmerBulkDeleteDTO dto
    ) {
        teilnehmerService.removeTeilnehmerBulk(id, dto.getPersonIds());
    }

    /* =========================================================
       ACTIVATE
       ========================================================= */

    @PutMapping("/{id}/aktiv")
    public VeranstaltungDetailDTO setActive(
            @PathVariable Long id
    ) {
        return veranstaltungService.setActive(id);
    }
}