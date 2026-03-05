package com.kcserver.controller;

import com.kcserver.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.finanz.AbrechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/abrechnung")
@RequiredArgsConstructor
public class AbrechnungController {

    private final AbrechnungService service;

    @GetMapping
    public AbrechnungDetailDTO get(@PathVariable Long veranstaltungId) {
        return service.getOrCreate(veranstaltungId);
    }

    @PostMapping("/abschliessen")
    public void abschliessen(@PathVariable Long veranstaltungId) {
        service.abschliessen(veranstaltungId);
    }

    @PostMapping("/teilnehmer-berechnen")
    public void berechneTeilnehmer(@PathVariable Long veranstaltungId) {
        service.berechneTeilnehmerEinnahmen(veranstaltungId);
    }
}