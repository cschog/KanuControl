package com.kcserver.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return "Groups: " + jwt.getClaimAsStringList("groups");
    }
}
