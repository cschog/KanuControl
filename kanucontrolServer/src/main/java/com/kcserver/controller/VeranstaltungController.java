package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
import com.kcserver.dto.veranstaltung.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.service.VeranstaltungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.kcserver.finanz.FinanzGruppeService;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/veranstaltungen")
public class VeranstaltungController {

    private final VeranstaltungService veranstaltungService;
    private final TeilnehmerService teilnehmerService;
    private final FinanzGruppeService finanzGruppeService;




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
       LIST
       ========================================================= */

    @GetMapping
    public Page<VeranstaltungListDTO> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean aktiv,
            @RequestParam(required = false) Long vereinId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate beginnVon,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate beginnBis,
            @RequestParam(required = false) VeranstaltungTyp typ,
            Pageable pageable
    ) {
        return veranstaltungService.getAll(
                name, aktiv, vereinId, beginnVon, beginnBis, typ, pageable
        );
    }

    /* =========================================================
       DETAIL
       ========================================================= */

    @GetMapping("/active")
    public VeranstaltungDetailDTO getActive() {
        return veranstaltungService.getActiveOptional()
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));
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
            @Valid @RequestBody VeranstaltungUpdateDTO dto
    ) {
        return veranstaltungService.update(id, dto);
    }

    @PutMapping("/{id}/aktiv")
    public VeranstaltungDetailDTO setActive(@PathVariable Long id) {
        return veranstaltungService.setActive(id);
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        veranstaltungService.delete(id);
    }

    /* =========================================================
       TEILNEHMER BULK DELETE
       ========================================================= */

    @DeleteMapping("/{id}/teilnehmer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeilnehmerBulk(
            @PathVariable Long id,
            @RequestBody TeilnehmerBulkDeleteDTO dto
    ) {
        teilnehmerService.removeTeilnehmerBulk(id, dto.getPersonIds());
    }

    @PutMapping("/{veranstaltungId}/teilnehmer/{teilnehmerId}/kuerzel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignKuerzel(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId,
            @RequestParam String kuerzel
    ) {

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                kuerzel
        );
    }
    @GetMapping("/{veranstaltungId}/teilnehmer/ohne-kuerzel")
    public List<TeilnehmerKurzDTO> findOhneKuerzel(
            @PathVariable Long veranstaltungId) {

        return teilnehmerService.findOhneKuerzel(veranstaltungId);
    }
}