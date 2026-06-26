package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.dto.validation.ValidationResultDTO;
import com.kcserver.finanz.AbrechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/abrechnung")
@RequiredArgsConstructor
public class AbrechnungController {

    private final AbrechnungService service;

    @GetMapping
    public ApiResponse<AbrechnungDetailDTO> get(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                service.getOrCreate(veranstaltungId)
        );
    }

    @PostMapping("/abschliessen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void abschliessen(
            @PathVariable Long veranstaltungId
    ) {
        service.abschliessen(veranstaltungId);
    }

    @PostMapping("/teilnehmer-berechnen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void berechneTeilnehmer(
            @PathVariable Long veranstaltungId
    ) {
        service.berechneTeilnehmerEinnahmen(veranstaltungId);
    }

    @GetMapping("/validierung")
    public ApiResponse<ValidationResultDTO> validateAbrechnung(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                service.validate(veranstaltungId)
        );
    }
}