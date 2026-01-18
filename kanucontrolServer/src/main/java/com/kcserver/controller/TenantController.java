package com.kcserver.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TenantController {

    @GetMapping("/api/active-schema")
    public String activeSchema(Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();

        List<String> groups = jwt.getClaimAsStringList("groups");

        return groups.stream()
                .findFirst()
                .map(g -> g.startsWith("/") ? g.substring(1) : g)
                .orElse("NO_TENANT");
    }
}