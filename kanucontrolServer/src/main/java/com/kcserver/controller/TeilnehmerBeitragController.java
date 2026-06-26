package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.teilnehmer.TeilnehmerBeitraegeResponseDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerBezahltDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.service.VeranstaltungService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/beitraege")
public class TeilnehmerBeitragController {

    private final TeilnehmerService teilnehmerService;
    private final VeranstaltungService veranstaltungService;

    @PatchMapping("/{teilnehmerId}")
    public ApiResponse<TeilnehmerListDTO> updateBezahlt(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId,
            @RequestBody TeilnehmerBezahltDTO dto
    ) {

        return ApiResponse.of(
                teilnehmerService.updateBezahlt(
                        teilnehmerId,
                        dto.getBezahlt()
                )
        );
    }

    @GetMapping
    public ApiResponse<TeilnehmerBeitraegeResponseDTO> getBeitraege(
            @PathVariable Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungService.findEntityById(veranstaltungId);

        TeilnehmerBeitraegeResponseDTO dto =
                new TeilnehmerBeitraegeResponseDTO();

        dto.setIndividuelleGebuehren(
                veranstaltung.isIndividuelleGebuehren()
        );

        dto.setTeilnehmer(
                teilnehmerService.findAllByVeranstaltungForBeitraege(
                        veranstaltungId
                )
        );

        return ApiResponse.of(dto);
    }
}