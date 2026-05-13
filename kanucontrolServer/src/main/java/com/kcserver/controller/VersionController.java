package com.kcserver.controller;

import com.kcserver.dto.system.VersionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    @Value("${app.version}")
    private String version;

    @GetMapping("/api/version")
    public VersionDTO version() {
        return new VersionDTO(version);
    }
}