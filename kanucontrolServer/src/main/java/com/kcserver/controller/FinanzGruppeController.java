package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.finanzen.*;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.service.finanz.FinanzausgleichService;
import com.kcserver.service.finanz.FinanzGruppeQueryService;
import com.kcserver.service.finanz.FinanzGruppeService;
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
    private final FinanzausgleichService finanzausgleichService;

    /* =========================
       OVERVIEW (KürzelPage)
       ========================= */

    @GetMapping
    public ApiResponse<List<FinanzGruppeOverviewDTO>> findAll(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                queryService.getOverview(veranstaltungId)
        );
    }

    /* =========================
       DETAIL
       ========================= */

    @GetMapping("/{gruppeId:\\d+}")
    public ApiResponse<FinanzGruppeDetailDTO> findOne(
            @PathVariable Long gruppeId
    ) {
        return ApiResponse.of(
                queryService.getDetail(gruppeId)
        );
    }

    /* =========================
       FINANZAUSGLEICH
       ========================= */

    @GetMapping("/{gruppeId:\\d+}/finanzausgleich")
    public ApiResponse<FinanzausgleichDTO> getFinanzausgleich(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId
    ) {
        return ApiResponse.of(
                finanzausgleichService.getFinanzausgleich(
                        veranstaltungId,
                        gruppeId
                )
        );
    }

    /* =========================
   FINANZAUSGLEICH PRÜFUNG
   ========================= */

    @GetMapping("/finanzausgleich/pruefung")
    public ApiResponse<FinanzausgleichPruefungDTO> pruefeTeilnehmerBeitraege(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                finanzausgleichService.pruefeTeilnehmerBeitraege(
                        veranstaltungId
                )
        );
    }

    /* =========================
       CREATE
       ========================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FinanzGruppeDetailDTO> create(
            @PathVariable Long veranstaltungId,
            @Valid @RequestBody FinanzGruppeCreateDTO dto
    ) {
        FinanzGruppe g = commandService.create(
                veranstaltungId,
                dto.kuerzel()
        );

        return ApiResponse.of(
                queryService.getDetail(g.getId())
        );
    }

    /* =========================
       UPDATE
       ========================= */

    @PutMapping("/{gruppeId:\\d+}")
    public ApiResponse<FinanzGruppeDetailDTO> update(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId,
            @Valid @RequestBody FinanzGruppeCreateDTO dto
    ) {
        commandService.update(
                veranstaltungId,
                gruppeId,
                dto.kuerzel()
        );

        return ApiResponse.of(
                queryService.getDetail(gruppeId)
        );
    }

    /* =========================
       DELETE
       ========================= */

    @DeleteMapping("/{gruppeId:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long veranstaltungId,
            @PathVariable Long gruppeId
    ) {
        commandService.delete(
                veranstaltungId,
                gruppeId
        );
    }

    /* =========================
       REMOVE TEILNEHMER
       ========================= */

    @DeleteMapping("/{gruppeId:\\d+}/teilnehmer/{personId:\\d+}")
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

    @PutMapping("/{gruppeId:\\d+}/teilnehmer")
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