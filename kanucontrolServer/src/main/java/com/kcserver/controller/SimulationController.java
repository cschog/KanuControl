package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.simulation.PlanungsSimulation;
import com.kcserver.dto.simulation.SimulationErgebnis;
import com.kcserver.service.simulation.SimulationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationFacade facade;

    @GetMapping("/{veranstaltungId}")
    public ApiResponse<PlanungsSimulation> getSimulation(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                facade.getSimulation(veranstaltungId)
        );
    }

    @PostMapping

    public ApiResponse<SimulationErgebnis> simuliere(
            @RequestBody PlanungsSimulation simulation
    ) {

        return ApiResponse.of(
                facade.simuliere(simulation));
    }

    @PutMapping("/{veranstaltungId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveSimulation(
            @PathVariable Long veranstaltungId,
            @RequestBody PlanungsSimulation simulation
    ) {
        facade.saveSimulation(
                veranstaltungId,
                simulation
        );
    }
}