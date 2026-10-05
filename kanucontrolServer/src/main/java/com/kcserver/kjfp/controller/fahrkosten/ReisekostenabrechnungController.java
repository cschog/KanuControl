package com.kcserver.kjfp.controller.fahrkosten;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.core.dto.person.PersonRefDTO;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenabrechnungCreateRequest;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenabrechnungDetailResponse;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenabrechnungListResponse;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenabrechnungUpdateRequest;
import com.kcserver.kjfp.service.reisekosten.ReisekostenabrechnungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reisekosten")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
public class ReisekostenabrechnungController {

    private final ReisekostenabrechnungService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(
            @RequestBody @Valid ReisekostenabrechnungCreateRequest request
    ) {
        return ApiResponse.of(
                service.create(request)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ReisekostenabrechnungDetailResponse> get(
            @PathVariable Long id
    ) {
        return ApiResponse.of(
                service.get(id)
        );
    }

    @GetMapping("/veranstaltung/{veranstaltungId}")
    public ApiResponse<List<ReisekostenabrechnungListResponse>> listByVeranstaltung(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                service.listByVeranstaltung(veranstaltungId)
        );
    }

    @GetMapping("/veranstaltung/{veranstaltungId}/finanzgruppe/{finanzGruppeId}")
    public ApiResponse<List<ReisekostenabrechnungListResponse>> listByFinanzGruppe(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId
    ) {
        return ApiResponse.of(
                service.listByFinanzGruppe(
                        veranstaltungId,
                        finanzGruppeId
                )
        );
    }

    @GetMapping("/veranstaltung/{veranstaltungId}/personen")
    public ApiResponse<List<PersonRefDTO>> getVerfuegbareReisekostenPersonen(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.of(
                service.getVerfuegbareReisekostenPersonen(
                        veranstaltungId,
                        search
                )
        );
    }

    @GetMapping("/veranstaltung/{veranstaltungId}/mitfahrer")
    public ApiResponse<List<PersonRefDTO>> getVerfuegbareMitfahrer(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                service.getVerfuegbareMitfahrer(
                        veranstaltungId
                )
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @RequestBody @Valid ReisekostenabrechnungUpdateRequest request
    ) {
        service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }
}