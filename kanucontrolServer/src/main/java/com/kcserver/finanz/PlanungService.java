package com.kcserver.finanz;

import com.kcserver.dto.finanzen.FinanzSummaryDTO;
import com.kcserver.dto.planung.PlanungDetailDTO;
import com.kcserver.entity.Planung;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.mapper.PlanungMapper;
import com.kcserver.repository.PlanungRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
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
    private final VeranstaltungRepository veranstaltungRepository;
    private final PlanungMapper mapper;
    private final FinanzService finanzService;
    private final TeilnehmerRepository teilnehmerRepository;

    /* =========================================================
       GET OR CREATE
       ========================================================= */

    public PlanungDetailDTO getOrCreate(Long veranstaltungId) {

        Planung p = planungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseGet(() -> createPlanung(veranstaltungId));

        PlanungDetailDTO dto = mapper.toDTO(p);

        FinanzSummaryDTO summary =
                finanzService.buildSummary(
                        p.getPositionen(),
                        teilnehmerRepository.countByVeranstaltungId(veranstaltungId)
                );

        dto.setFinanz(summary);

        return dto;
    }

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

        if (p.isEingereicht()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Planung bereits eingereicht"
            );
        }

        finanzService.validateAusgeglichen(p.getPositionen());

        p.setEingereicht(true);
    }

    @Transactional
    public void wiederOeffnen(Long veranstaltungId) {
        Planung planung = planungRepository.findByVeranstaltungId(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Planung not found"
                ));

        planung.setEingereicht(false);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private Planung createPlanung(Long veranstaltungId) {

        Veranstaltung v = veranstaltungRepository
                .findById(veranstaltungId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Veranstaltung nicht gefunden"
                ));

        Planung p = new Planung();
        p.setVeranstaltung(v);

        return planungRepository.save(p);
    }
}