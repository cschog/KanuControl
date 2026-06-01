package com.kcserver.controller;

import com.kcserver.dto.reisekosten.ReisekostenKonfigurationResponse;
import com.kcserver.dto.reisekosten.ReisekostenKonfigurationSaveRequest;
import com.kcserver.service.reisekosten.ReisekostenKonfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reisekosten-konfiguration")
@RequiredArgsConstructor
public class ReisekostenKonfigurationController {

    private final ReisekostenKonfigurationService service;

    @GetMapping
    public List<ReisekostenKonfigurationResponse> list() {
        return service.list();
    }

    @GetMapping("/aktuell")
    public ReisekostenKonfigurationResponse aktuell() {
        return service.getAktuell();
    }

    @PostMapping
    public Long create(
            @RequestBody ReisekostenKonfigurationSaveRequest request
    ) {
        return service.create(request);
    }
}
