package com.kcserver.controller;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class WhoAmIController {

    @GetMapping("/whoami")
    public Map<String, Object> whoAmI(JwtAuthenticationToken authentication) {

        return Map.of(
                "username", authentication.getToken().getClaimAsString("preferred_username"),
                "groups", authentication.getToken().getClaimAsStringList("groups"),
                "issuer", authentication.getToken().getIssuer().toString()
        );
    }
}