package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerListDTO;
import com.kcserver.service.TeilnehmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/beitraege")
public class TeilnehmerBeitragController {

    private final TeilnehmerService teilnehmerService;

    @GetMapping
    public List<TeilnehmerListDTO> getBeitraege(
            @PathVariable Long veranstaltungId
    ) {

        return teilnehmerService
                .findAllByVeranstaltungForBeitraege(
                        veranstaltungId
                );
    }
}