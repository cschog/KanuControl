package com.kcserver.controller;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungCreateRequest;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungDetailResponse;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungListResponse;
import com.kcserver.dto.reisekosten.ReisekostenabrechnungUpdateRequest;
import com.kcserver.service.reisekosten.ReisekostenabrechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reisekosten")
@RequiredArgsConstructor
public class ReisekostenabrechnungController {

    private final ReisekostenabrechnungService service;

    @PostMapping
    public Long create(
            @RequestBody ReisekostenabrechnungCreateRequest request
    ) {

        return service.create(request);
    }

    @GetMapping("/{id}")
    public ReisekostenabrechnungDetailResponse get(
            @PathVariable Long id
    ) {

        return service.get(id);
    }

    @GetMapping("/veranstaltung/{veranstaltungId}")
    public List<ReisekostenabrechnungListResponse> listByVeranstaltung(
            @PathVariable Long veranstaltungId
    ){

        return service.listByVeranstaltung(veranstaltungId);

    }

    @GetMapping(
            "/veranstaltung/{veranstaltungId}/personen"
    )
    public List<PersonRefDTO> getVerfuegbareReisekostenPersonen(
            @PathVariable Long veranstaltungId,
            @RequestParam(required = false) String search
    ) {
        return service.getVerfuegbareReisekostenPersonen(
                veranstaltungId,
                search
        );
    }

    @GetMapping(
            "/veranstaltung/{veranstaltungId}/mitfahrer"
    )
    public List<PersonRefDTO> getVerfuegbareMitfahrer(
            @PathVariable Long veranstaltungId
    ) {

        return service.getVerfuegbareMitfahrer(
                veranstaltungId
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody ReisekostenabrechnungUpdateRequest request
    ) {

        service.update(id, request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}