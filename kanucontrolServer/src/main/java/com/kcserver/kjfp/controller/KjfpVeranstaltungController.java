package com.kcserver.kjfp.controller;


import com.kcserver.api.response.ApiResponse;
import com.kcserver.kjfp.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.kjfp.service.abrechnung.AbrechnungBelegService;
import com.kcserver.core.service.TeilnehmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.kcserver.core.dto.teilnehmer.TeilnehmerKurzDTO;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.kcserver.kjfp.service.finanz.FinanzGruppeService;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/veranstaltungen")
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
public class KjfpVeranstaltungController {
    private final TeilnehmerService teilnehmerService;
    private final FinanzGruppeService finanzGruppeService;
    private final AbrechnungBelegService belegService;

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

    @GetMapping("/{veranstaltungId}/finanzgruppen/{finanzGruppeId}/belege")
    public ApiResponse<List<AbrechnungBelegDTO>> getBelegeByFinanzGruppe(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId
    ) {
        return ApiResponse.of(
                belegService.findByFinanzGruppe(
                        veranstaltungId,
                        finanzGruppeId
                )
        );
    }
}
