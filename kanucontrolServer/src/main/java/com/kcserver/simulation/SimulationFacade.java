package com.kcserver.simulation;

import com.kcserver.entity.Veranstaltung;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimulationFacade {

    private final PlanungsSimulationFactory factory;
    private final SimulationEngine simulationEngine;

    public SimulationErgebnis simuliere(
            Veranstaltung veranstaltung
    ) {

        return simulationEngine.simuliere(
                factory.fromVeranstaltung(veranstaltung)
        );
    }

    public SimulationErgebnis simuliere(
            PlanungsSimulation simulation
    ) {

        return simulationEngine.simuliere(simulation);
    }
}
