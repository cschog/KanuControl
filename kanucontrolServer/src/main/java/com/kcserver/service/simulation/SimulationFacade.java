package com.kcserver.service.simulation;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.dto.simulation.SimulationErgebnis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimulationFacade {

    private final SimulationService simulationService;
    private final SimulationEngine engine;

    @Transactional(readOnly = true)
    public PlanungsSimulation getSimulation(
            Long veranstaltungId
    ) {
        return simulationService.getSimulation(
                veranstaltungId
        );
    }

    @Transactional
    public void saveSimulation(
            Long veranstaltungId,
            PlanungsSimulation simulation
    ) {
        simulationService.saveSimulation(
                veranstaltungId,
                simulation
        );
    }

    public SimulationErgebnis simuliere(
            PlanungsSimulation simulation
    ) {
        return engine.simuliere(simulation);
    }
}