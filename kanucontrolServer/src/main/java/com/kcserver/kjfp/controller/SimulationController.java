package com.kcserver.kjfp.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.kjfp.dto.simulation.PlanungsSimulation;
import com.kcserver.kjfp.dto.simulation.SimulationErgebnis;
import com.kcserver.kjfp.service.simulation.SimulationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
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