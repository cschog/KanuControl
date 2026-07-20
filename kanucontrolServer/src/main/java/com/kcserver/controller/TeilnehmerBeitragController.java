package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.teilnehmer.TeilnehmerBeitraegeResponseDTO;

import com.kcserver.dto.teilnehmer.TeilnehmerBezahltDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.service.TeilnehmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/beitraege")
public class TeilnehmerBeitragController {

    private final TeilnehmerService teilnehmerService;

    @GetMapping
    public ApiResponse<TeilnehmerBeitraegeResponseDTO> getBeitraege(
            @PathVariable Long veranstaltungId
    ) {

        TeilnehmerBeitraegeResponseDTO dto =
                new TeilnehmerBeitraegeResponseDTO();

        dto.setTeilnehmer(
                teilnehmerService.findAllByVeranstaltungForBeitraege(
                        veranstaltungId
                )
        );

        return ApiResponse.of(dto);
    }
    @PatchMapping("/{teilnehmerId}")
    public ApiResponse<TeilnehmerListDTO> updateBezahlt(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId,
            @RequestBody TeilnehmerBezahltDTO dto
    ) {
        return ApiResponse.of(
                teilnehmerService.updateBezahlt(
                        veranstaltungId,
                        teilnehmerId,
                        dto.getBezahlt()
                )
        );
    }

    @PatchMapping
    public ApiResponse<Void> updateAlleBezahlt(
            @PathVariable Long veranstaltungId,
            @RequestBody TeilnehmerBezahltDTO dto
    ) {

        teilnehmerService.updateAlleBezahlt(
                veranstaltungId,
                dto.getBezahlt()
        );

        return ApiResponse.of(null);
    }
}