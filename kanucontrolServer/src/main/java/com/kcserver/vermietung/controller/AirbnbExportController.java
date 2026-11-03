package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.AirbnbExportDTO;
import com.kcserver.vermietung.service.AirbnbExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/objekte/{id}/airbnb-export")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
public class AirbnbExportController {
    private final AirbnbExportService service;
    @GetMapping public ApiResponse<AirbnbExportDTO> get(@PathVariable Long id) { return ApiResponse.of(service.get(id)); }
    @PutMapping public ApiResponse<AirbnbExportDTO> save(@PathVariable Long id, @Valid @RequestBody AirbnbExportDTO input) {
        return ApiResponse.of(service.save(id,input,false));
    }
    @PostMapping("/link") public ApiResponse<AirbnbExportDTO> link(@PathVariable Long id, @Valid @RequestBody AirbnbExportDTO input) {
        return ApiResponse.of(service.save(id,input,true));
    }
}
