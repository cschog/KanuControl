package com.kcserver.controller;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.service.MitgliedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mitglied")
public class MitgliedController {

    private final MitgliedService mitgliedService;

    public MitgliedController(MitgliedService mitgliedService) {
        this.mitgliedService = mitgliedService;
    }

    @PostMapping
    public ResponseEntity<MitgliedDTO> createMitglied(@RequestBody MitgliedDTO dto) {
        MitgliedDTO created = mitgliedService.createMitglied(
                dto.getPersonId(),
                dto.getVereinId(),
                dto.getFunktion(),
                dto.getHauptVerein()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}