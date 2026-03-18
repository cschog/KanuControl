package com.kcserver.controller;

import com.kcserver.dto.planung.PlanungDetailDTO;
import com.kcserver.finanz.PlanungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/planung")
@RequiredArgsConstructor
public class PlanungController {

    private final PlanungService planungService;

    /* =========================================================
       GET PLANUNG
       ========================================================= */

    @GetMapping
    public PlanungDetailDTO get(@PathVariable Long veranstaltungId) {
        return planungService.getOrCreate(veranstaltungId);
    }

    /* =========================================================
       EINREICHEN
       ========================================================= */

    @PostMapping("/einreichen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void einreichen(@PathVariable Long veranstaltungId) {
        planungService.einreichen(veranstaltungId);
    }

    @PostMapping("/wieder-oeffnen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void wiederOeffnen(@PathVariable Long veranstaltungId) {
        planungService.wiederOeffnen(veranstaltungId);
    }
}