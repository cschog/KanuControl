package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.dto.validation.ValidationResultDTO;
import com.kcserver.finanz.AbrechnungService;
import com.kcserver.service.abrechnung.AbrechnungSynchronisationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/abrechnung")
@RequiredArgsConstructor
public class AbrechnungController {

    private final AbrechnungService service;
    private final AbrechnungSynchronisationsService synchronisationsService;

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

    @PostMapping("/synchronisieren")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void synchronisieren(
            @PathVariable Long veranstaltungId
    ) {
        synchronisationsService.synchronisieren(veranstaltungId);
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