// src/main/java/com/kcserver/controller/CorsTestController.java
package com.kcserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CorsTestController {

    @GetMapping("/test-cors")
    public String testCors() {
        return "CORS works";
    }
}