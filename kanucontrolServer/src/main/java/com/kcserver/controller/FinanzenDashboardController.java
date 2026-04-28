// controller/FinanzenDashboardController.java

package com.kcserver.controller;

import com.kcserver.dto.finanzen.FinanzenDashboardDTO;
import com.kcserver.service.FinanzenDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/finanzen")
public class FinanzenDashboardController {

    private final FinanzenDashboardService service;

    @GetMapping("/dashboard")
    public FinanzenDashboardDTO getDashboard(
            @PathVariable Long veranstaltungId
    ) {
        return service.getDashboard(veranstaltungId);
    }
}