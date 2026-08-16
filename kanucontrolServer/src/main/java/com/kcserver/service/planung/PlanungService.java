package com.kcserver.service.planung;

import com.kcserver.dto.planung.PlanungDetailDTO;
import com.kcserver.entity.Planung;
import com.kcserver.enumtype.PlanungsStatus;
import com.kcserver.service.finanz.FinanzService;
import com.kcserver.mapper.PlanungMapper;
import com.kcserver.repository.PlanungRepository;
import com.kcserver.repository.TeilnehmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanungService {

    private final PlanungRepository planungRepository;
    private final PlanungMapper mapper;
    private final FinanzService finanzService;
    private final TeilnehmerRepository teilnehmerRepository;
    private final PlanungAutomatikService planungsSimulationService;

    /* =========================================================
       EINREICHEN
       ========================================================= */

    public void einreichen(Long veranstaltungId) {

        Planung p = planungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Planung nicht gefunden"
                ));

        if (p.istEingereicht()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Planung bereits eingereicht"
            );
        }

        finanzService.validatePlanung(p.getPositionen());

        p.setStatus(PlanungsStatus.EINGEREICHT);
    }

    @Transactional
    public void wiederOeffnen(Long veranstaltungId) {
        Planung planung = planungRepository.findByVeranstaltungId(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Planung nicht gefunden."
                ));

        planung.setStatus(PlanungsStatus.IN_BEARBEITUNG);
    }

 /* =========================================================
   PLANUNG LADEN
   ========================================================= */

    public PlanungDetailDTO get(Long veranstaltungId) {

        Planung p = planungRepository
                .findByVeranstaltungIdWithPositionen(veranstaltungId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Für diese Veranstaltung wurde noch keine Planung gespeichert."
                        ));

        if (!p.istEingereicht()) {
            planungsSimulationService.aktualisiereAutomatischePositionen(p);
        }

        PlanungDetailDTO dto = mapper.toDTO(p);

        dto.setFinanz(
                finanzService.buildSummary(
                        p.getPositionen(),
                        teilnehmerRepository.countByVeranstaltungId(veranstaltungId)
                )
        );

        return dto;
    }
}