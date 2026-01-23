package com.kcserver.controller;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.service.MitgliedService;
import com.kcserver.validation.OnCreate;
import com.kcserver.validation.OnUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mitglied")
public class MitgliedController {

    private final MitgliedService mitgliedService;

    public MitgliedController(MitgliedService mitgliedService) {
        this.mitgliedService = mitgliedService;
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping
    public ResponseEntity<MitgliedDTO> create(
            @RequestBody @Validated(OnCreate.class) MitgliedDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mitgliedService.createMitglied(dto));
    }

    /* =========================================================
       READ
       ========================================================= */

    @GetMapping("/{id}")
    public MitgliedDTO getById(@PathVariable Long id) {
        return mitgliedService.getById(id);
    }

    @GetMapping("/person/{personId}")
    public List<MitgliedDTO> getByPerson(
            @PathVariable Long personId,
            org.springframework.data.domain.Pageable pageable
    ) {
        return mitgliedService.getByPerson(personId, pageable);
    }

    @GetMapping("/verein/{vereinId}")
    public List<MitgliedDTO> getByVerein(@PathVariable Long vereinId) {
        return mitgliedService.getByVerein(vereinId);
    }

    @GetMapping("/person/{personId}/hauptverein")
    public ResponseEntity<MitgliedDTO> getHauptvereinByPerson(
            @PathVariable Long personId
    ) {
        return ResponseEntity.ok(
                mitgliedService.getHauptvereinByPerson(personId)
        );
    }

     /* =========================
       DELETE
       ========================= */

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMitglied(@PathVariable Long id) {
        mitgliedService.delete(id);
    }

     /* =========================
       UPDATE
       ========================= */

    @PutMapping("/{id}")
    public MitgliedDTO update(
            @PathVariable Long id,
            @RequestBody @Validated(OnUpdate.class) MitgliedDTO dto) {

        return mitgliedService.updateMitglied(id, dto);
    }
}