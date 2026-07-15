package com.kcserver.service.simulation;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.entity.Planung;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.PlanungsStatus;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.mapper.PlanungSimulationMapper;
import com.kcserver.repository.PlanungRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.service.PlanungAutomatikService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final PlanungRepository planungRepository;
    private final VeranstaltungRepository veranstaltungRepository;

    private final SimulationFactory simulationFactory;
    private final PlanungSimulationMapper mapper;

    private final PlanungAutomatikService planungAutomatikService;

    public PlanungsSimulation getSimulation(
            Long veranstaltungId
    ) {

        return planungRepository
                .findByVeranstaltungId(veranstaltungId)
                .map(mapper::toSimulation)
                .orElseGet(() -> {

                    Veranstaltung veranstaltung =
                            veranstaltungRepository.findById(veranstaltungId)
                                    .orElseThrow(() ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND,
                                                    ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                            ));

                    return simulationFactory.fromVeranstaltung(
                            veranstaltung
                    );
                });
    }

    @Transactional
    public void saveSimulation(
            Long veranstaltungId,
            PlanungsSimulation simulation
    ) {

        Planung planung = planungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseGet(() -> createPlanung(veranstaltungId));

        mapper.updatePlanung(
                planung,
                simulation
        );
        mapper.berechneAntragsdaten(planung);

        planungAutomatikService
                .aktualisiereAutomatischePositionen(planung);

        planungRepository.save(planung);
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private Planung createPlanung(
            Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository.findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                ));

        Planung planung = new Planung();
        planung.setVeranstaltung(veranstaltung);

        planung.setStatus(
                PlanungsStatus.IN_BEARBEITUNG
        );

        return planung;
    }
}