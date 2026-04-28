package com.kcserver.controller;

import com.kcserver.config.FoerderConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @GetMapping("/foerderdeckel")
    public BigDecimal getFoerderdeckel() {
        return FoerderConfig.FOERDERDECKEL;
    }
}
