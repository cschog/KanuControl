package com.kcserver.controller;

import com.kcserver.dto.finanz.FinanzGruppeCreateDTO;
import com.kcserver.dto.finanz.FinanzGruppeDTO;
import com.kcserver.dto.finanz.FinanzGruppeDetailDTO;
import com.kcserver.dto.finanz.FinanzGruppeOverviewDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.finanz.FinanzGruppeQueryService;
import com.kcserver.mapper.FinanzGruppeDetailMapper;
import com.kcserver.finanz.FinanzGruppeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/finanzgruppen")
@RequiredArgsConstructor
public class FinanzGruppeController {

    private final FinanzGruppeService commandService;
    private final FinanzGruppeQueryService queryService;

    /* =========================
       OVERVIEW (KürzelPage)
       ========================= */

    @GetMapping
    public List<FinanzGruppeOverviewDTO> findAll(
            @PathVariable Long veranstaltungId) {

        return queryService.getOverview(veranstaltungId);
    }

    /* =========================
       DETAIL
       ========================= */

    @GetMapping("/{gruppeId}")
    public FinanzGruppeDetailDTO findOne(
            @PathVariable Long gruppeId) {

        return queryService.getDetail(gruppeId);
    }

    /* =========================
       CREATE
       ========================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinanzGruppeDetailDTO create(
            @PathVariable Long veranstaltungId,
            @Valid @RequestBody FinanzGruppeCreateDTO dto) {

        FinanzGruppe g =
                commandService.create(veranstaltungId, dto.kuerzel());

        return queryService.getDetail(g.getId());
    }

    /* =========================
       UPDATE
       ========================= */

    @PutMapping("/{gruppeId}")
    public FinanzGruppeDetailDTO update(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId,
            @Valid @RequestBody FinanzGruppeCreateDTO dto) {

        commandService.update(veranstaltungId, gruppeId, dto.kuerzel());

        return queryService.getDetail(gruppeId);
    }

    /* =========================
       DELETE
       ========================= */

    @DeleteMapping("/{gruppeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId) {

        commandService.delete(veranstaltungId, gruppeId);
    }

    @DeleteMapping("/{gruppeId}/teilnehmer/{personId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId,
            @PathVariable Long personId
    ) {
        commandService.removeTeilnehmerFromGruppe(
                veranstaltungId,
                gruppeId,
                personId
        );
    }

    /* =========================
       ASSIGN TEILNEHMER
       ========================= */

    @PutMapping("/{gruppeId}/teilnehmer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignTeilnehmerBulk(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId,
            @RequestBody List<Long> personIds
    ) {
        commandService.assignTeilnehmerBulkByPersonIds(
                veranstaltungId,
                gruppeId,
                personIds
        );
    }
}