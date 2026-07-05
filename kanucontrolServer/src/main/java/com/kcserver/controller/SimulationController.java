package com.kcserver.controller;

import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.dto.simulation.SimulationErgebnis;
import com.kcserver.service.simulation.SimulationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationFacade facade;

    @GetMapping("/{veranstaltungId}")
    public PlanungsSimulation getSimulation(
            @PathVariable Long veranstaltungId
    ) {

        return facade.getSimulation(
                veranstaltungId
        );
    }

    @PostMapping
    public SimulationErgebnis simuliere(
            @RequestBody PlanungsSimulation simulation
    ) {

        return facade.simuliere(simulation);
    }
}