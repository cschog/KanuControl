package com.kcserver.controller;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.service.MitgliedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mitglied")
public class MitgliedController {

    private final MitgliedService mitgliedService;

    public MitgliedController(MitgliedService mitgliedService) {
        this.mitgliedService = mitgliedService;
    }

    @PostMapping
    public ResponseEntity<MitgliedDTO> createMitglied(
            @RequestBody @Valid MitgliedDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mitgliedService.createMitglied(dto));
    }
}