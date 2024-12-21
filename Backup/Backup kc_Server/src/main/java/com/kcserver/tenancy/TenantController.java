package com.kcserver.tenancy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TenantController {

    @GetMapping("/currentTenant")
    public ResponseEntity<String> getCurrentTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok("Current tenant: " + tenantId);
    }
}
