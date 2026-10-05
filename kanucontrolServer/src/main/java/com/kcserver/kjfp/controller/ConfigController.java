package com.kcserver.kjfp.controller;

import com.kcserver.kjfp.config.FoerderConfig;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/config")
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
public class ConfigController {

    @GetMapping("/foerderdeckel")
    public BigDecimal getFoerderdeckel() {
        return FoerderConfig.FOERDERDECKEL;
    }
}
