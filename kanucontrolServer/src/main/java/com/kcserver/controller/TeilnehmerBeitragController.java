package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.teilnehmer.TeilnehmerBeitraegeResponseDTO;

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

        return ApiResponse.of(
                teilnehmerService.findAllByVeranstaltungForBeitraege(
                        veranstaltungId
                )
        );
    }
}