package com.kcserver.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    @PreAuthorize("hasRole('api-role')")
    @GetMapping("/secured")
    public String securedEndpoint() {
        return "This is a secured endpoint";
    }
}

