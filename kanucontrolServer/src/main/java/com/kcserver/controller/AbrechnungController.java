package com.kcserver.controller;

import com.kcserver.dto.abrechnung.AbrechnungDetailDTO;
import com.kcserver.dto.validation.ValidationResultDTO;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.finanz.AbrechnungService;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.service.VeranstaltungValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/abrechnung")
@RequiredArgsConstructor
public class AbrechnungController {

    private final AbrechnungService service;

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final VeranstaltungValidator validator;

    @GetMapping
    public AbrechnungDetailDTO get(
            @PathVariable Long veranstaltungId
    ) {
        return service.getOrCreate(veranstaltungId);
    }

    @PostMapping("/abschliessen")
    public void abschliessen(
            @PathVariable Long veranstaltungId
    ) {
        service.abschliessen(veranstaltungId);
    }

    @PostMapping("/teilnehmer-berechnen")
    public void berechneTeilnehmer(
            @PathVariable Long veranstaltungId
    ) {
        service.berechneTeilnehmerEinnahmen(veranstaltungId);
    }

    @GetMapping("/validierung")
    public ValidationResultDTO validateAbrechnung(
            @PathVariable Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findByIdWithRelations(veranstaltungId)
                        .orElseThrow();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository
                        .findAllWithPerson(veranstaltungId);

        return validator.getAbrechnungValidation(
                veranstaltung,
                teilnehmer
        );
    }
}