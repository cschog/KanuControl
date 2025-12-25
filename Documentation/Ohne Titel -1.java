package com.kcserver.controller;

import com.kcserver.tenancy.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantController {

    @GetMapping("/api/active-schema")
    public String getActiveSchema() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : "No tenant schema active";
    }
}

package com.kcserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    @GetMapping("/api/active-schema")
    public String getActiveSchema() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return "Authentication is missing or invalid.";
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        logger.info("JWT: {}", jwt); // Log the JWT for debugging

        return "Authenticated JWT Claims: " + jwt.getClaims();
        // String tenantId = TenantContext.getTenantId();
        // return tenantId != null ? tenantId : "No tenant schema active";
    }
}