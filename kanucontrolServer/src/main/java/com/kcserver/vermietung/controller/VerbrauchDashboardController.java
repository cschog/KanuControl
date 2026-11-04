package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.VerbrauchDashboardDTO;
import com.kcserver.vermietung.service.VerbrauchDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/vermietung/verbrauch-dashboard") @PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class VerbrauchDashboardController {
    private final VerbrauchDashboardService service;
    public VerbrauchDashboardController(VerbrauchDashboardService service){this.service=service;}
    @GetMapping("/{objektId}/{jahr}") public ApiResponse<VerbrauchDashboardDTO> get(@PathVariable Long objektId,@PathVariable int jahr) {
        return new ApiResponse<>(service.get(objektId,jahr),List.of());
    }
}
