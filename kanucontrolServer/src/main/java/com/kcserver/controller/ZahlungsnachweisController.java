package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisDetailDTO;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisListDTO;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisUpdateDTO;
import com.kcserver.service.abrechnung.ZahlungsnachweisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/zahlungsnachweise")
public class ZahlungsnachweisController {

    private final ZahlungsnachweisService zahlungsnachweisService;

    @GetMapping
    public ApiResponse<List<ZahlungsnachweisListDTO>> getAll(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                zahlungsnachweisService.findByVeranstaltung(
                        veranstaltungId
                )
        );
    }

    @GetMapping("/{zahlungsnachweisId}")
    public ApiResponse<ZahlungsnachweisDetailDTO> get(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId
    ) {
        return ApiResponse.of(
                zahlungsnachweisService.get(
                        veranstaltungId,
                        zahlungsnachweisId
                )
        );
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<ZahlungsnachweisDetailDTO> create(
            @PathVariable Long veranstaltungId,
            @RequestBody ZahlungsnachweisUpdateDTO dto
    ) {

        return ApiResponse.of(
                zahlungsnachweisService.create(
                        veranstaltungId,
                        dto
                )
        );
    }

    @PutMapping("/{zahlungsnachweisId}")
    public ApiResponse<ZahlungsnachweisDetailDTO> update(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId,
            @Valid @RequestBody ZahlungsnachweisUpdateDTO dto
    ) {

        return ApiResponse.of(
                zahlungsnachweisService.update(
                        veranstaltungId,
                        zahlungsnachweisId,
                        dto
                )
        );
    }

    @DeleteMapping("/{zahlungsnachweisId}")
    public ApiResponse<Void> delete(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId
    ) {

        zahlungsnachweisService.delete(
                veranstaltungId,
                zahlungsnachweisId
        );

        return ApiResponse.of(null);
    }
}