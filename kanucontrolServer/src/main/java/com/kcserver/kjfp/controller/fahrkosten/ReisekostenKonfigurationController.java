package com.kcserver.kjfp.controller.fahrkosten;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenKonfigurationResponse;
import com.kcserver.kjfp.dto.reisekosten.ReisekostenKonfigurationSaveRequest;
import com.kcserver.kjfp.service.reisekosten.ReisekostenKonfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin/reisekosten-konfiguration")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ReisekostenKonfigurationController {

    private final ReisekostenKonfigurationService service;

    @GetMapping
    public ApiResponse<List<ReisekostenKonfigurationResponse>> list() {
        return ApiResponse.of(
                service.list()
        );
    }

    @GetMapping("/aktuell")
    public ApiResponse<ReisekostenKonfigurationResponse> aktuell() {
        return ApiResponse.of(
                service.getAktuell()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(
            @RequestBody @Valid ReisekostenKonfigurationSaveRequest request
    ) {
        return ApiResponse.of(
                service.create(request)
        );
    }
}