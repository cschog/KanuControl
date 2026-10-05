package com.kcserver.kjfp.controller.abrechnung;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.kjfp.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.kjfp.dto.validation.ValidationResultDTO;
import com.kcserver.kjfp.service.abrechnung.AbrechnungService;
import com.kcserver.kjfp.service.abrechnung.AbrechnungSynchronisationsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/abrechnung")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
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
        synchronisationsService.synchronisieren(
                veranstaltungId
        );
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