package com.kcserver.controller;

import com.kcserver.tenancy.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantController {

    @GetMapping("/api/active-schema")
    public String getActiveSchema() {
        String tenantId = TenantContext.getTenant();
        return tenantId != null ? tenantId : "No tenant schema active";
    }
}