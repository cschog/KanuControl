package com.kcserver.service.simulation;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.dto.simulation.SimulationErgebnis;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.service.veranstaltung.VeranstaltungService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationFacade {

    private final VeranstaltungService veranstaltungService;
    private final SimulationFactory factory;
    private final SimulationEngine engine;


    public SimulationErgebnis simuliere(
            PlanungsSimulation simulation
    ) {

        return engine.simuliere(
                simulation
        );
    }

    @Transactional(readOnly = true)
    public PlanungsSimulation getSimulation(
            Long veranstaltungId
    ) {

        Veranstaltung veranstaltung =
                veranstaltungService.findEntityById(
                        veranstaltungId
                );

        return factory.createDefault(
                veranstaltung
        );
    }
}