package com.kcserver.controller.finanz;

import com.kcserver.dto.finanzen.FinanzausgleichZahlungDTO;
import com.kcserver.service.finanz.FinanzausgleichZahlungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}/finanzausgleich-zahlungen")
@RequiredArgsConstructor
public class FinanzausgleichZahlungController {

    private final FinanzausgleichZahlungService service;

    @GetMapping
    public List<FinanzausgleichZahlungDTO> findAll(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId
    ) {
        return service.findByFinanzGruppe(
                veranstaltungId,
                finanzGruppeId
        );
    }

    @GetMapping("/{zahlungId}")
    public FinanzausgleichZahlungDTO get(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long zahlungId
    ) {
        return service.get(
                veranstaltungId,
                zahlungId
        );
    }

    @GetMapping("/offen")
    public BigDecimal getOffen(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId
    ) {
        return service.getNochOffenerBetrag(
                veranstaltungId,
                finanzGruppeId
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinanzausgleichZahlungDTO create(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @RequestBody CreateFinanzausgleichZahlungRequest request
    ) {
        return service.create(
                veranstaltungId,
                finanzGruppeId,
                request.betrag(),
                request.datum(),
                request.bemerkung()
        );
    }

    @DeleteMapping("/{zahlungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long zahlungId
    ) {
        service.delete(
                veranstaltungId,
                zahlungId
        );
    }

    public record CreateFinanzausgleichZahlungRequest(
            BigDecimal betrag,
            LocalDate datum,
            String bemerkung
    ) {
    }
}