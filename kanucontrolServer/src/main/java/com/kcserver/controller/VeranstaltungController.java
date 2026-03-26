package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerBulkDeleteDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerUpdateDTO;
import com.kcserver.dto.veranstaltung.*;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.service.VeranstaltungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import com.kcserver.dto.teilnehmer.TeilnehmerKurzDTO;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.kcserver.finanz.FinanzGruppeService;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Pageable;

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
       LIST mit paging
       ========================================================= */

    @GetMapping
    public Page<VeranstaltungListDTO> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean aktiv,
            @RequestParam(required = false) Long vereinId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate beginnDatum,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endeDatum,
            @RequestParam(required = false) VeranstaltungTyp typ,
            @PageableDefault(size = 1000) Pageable pageable
    ) {

        VeranstaltungFilterDTO filter = new VeranstaltungFilterDTO();
        filter.setName(name);
        filter.setAktiv(aktiv);
        filter.setVereinId(vereinId);
        filter.setBeginnDatum(beginnDatum);
        filter.setEndeDatum(endeDatum);
        filter.setTyp(typ);

        return veranstaltungService.search(filter, pageable);
    }

    // ohne paging

    @GetMapping("/all")
    public List<VeranstaltungListDTO> searchAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean aktiv,
            @RequestParam(required = false) Long vereinId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate beginnDatum,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endeDatum,
            @RequestParam(required = false) VeranstaltungTyp typ
    ) {

        VeranstaltungFilterDTO filter = new VeranstaltungFilterDTO();
        filter.setName(name);
        filter.setAktiv(aktiv);
        filter.setVereinId(vereinId);
        filter.setBeginnDatum(beginnDatum);
        filter.setEndeDatum(endeDatum);
        filter.setTyp(typ);

        return veranstaltungService.searchAll(filter);
    }

    /* =========================================================
       DETAIL
       ========================================================= */

    @GetMapping("/aktiv")
    public VeranstaltungDetailDTO getActive() {
        return veranstaltungService.getActiveOptional()
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id:\\d+}")
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