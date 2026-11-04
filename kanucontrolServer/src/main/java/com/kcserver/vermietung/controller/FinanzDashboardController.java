package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.service.FinanzDashboardService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/vermietung/finanz-dashboard")
@PreAuthorize("hasAnyRole('ADMIN','VERMIETUNG')")
public class FinanzDashboardController {
    private final FinanzDashboardService service;
    public FinanzDashboardController(FinanzDashboardService service) {this.service=service;}
    @GetMapping("/{jahr}") public ApiResponse<FinanzDashboardDTO> get(@PathVariable int jahr) {return new ApiResponse<>(service.get(jahr),List.of());}
    @PutMapping("/{jahr}/einstellungen") public ApiResponse<FinanzDashboardDTO> save(@PathVariable int jahr,@RequestBody @Valid FinanzDashboardEinstellungDTO dto) {
        return new ApiResponse<>(service.save(jahr,dto),List.of());
    }
}
