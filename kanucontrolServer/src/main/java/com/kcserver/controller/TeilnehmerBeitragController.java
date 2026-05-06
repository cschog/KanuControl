package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerBeitraegeResponseDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerBezahltDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.service.VeranstaltungService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/beitraege")
public class TeilnehmerBeitragController {

    private final TeilnehmerService teilnehmerService;

    private final VeranstaltungService veranstaltungService;


    @PatchMapping("/{teilnehmerId}")
    public TeilnehmerListDTO updateBezahlt(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId,
            @RequestBody TeilnehmerBezahltDTO dto
    ) {

        return teilnehmerService.updateBezahlt(
                teilnehmerId,
                dto.getBezahlt()
        );
    }

    @GetMapping
    public TeilnehmerBeitraegeResponseDTO getBeitraege(
            @PathVariable Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungService
                        .findEntityById(
                                veranstaltungId
                        );

        TeilnehmerBeitraegeResponseDTO dto =
                new TeilnehmerBeitraegeResponseDTO();

        dto.setIndividuelleGebuehren(
                veranstaltung.isIndividuelleGebuehren()
        );

        dto.setTeilnehmer(
                teilnehmerService
                        .findAllByVeranstaltungForBeitraege(
                                veranstaltungId
                        )
        );

        return dto;
    }

}