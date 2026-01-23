package com.kcserver.controller;

import com.kcserver.tenancy.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantController {

    @GetMapping("/api/active-schema")
    public String activeSchema() {

        String tenant = TenantContext.getCurrentTenant();

        return tenant != null ? tenant : "NO_TENANT";
    }
}