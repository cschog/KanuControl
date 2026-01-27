package com.kcserver.controller;

import com.kcserver.dto.MitgliedDTO;
import com.kcserver.dto.MitgliedDetailDTO;
import com.kcserver.entity.Mitglied;
import com.kcserver.mapper.MitgliedMapper;
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

    private final MitgliedMapper mitgliedMapper;

    public MitgliedController(
            MitgliedService mitgliedService,
            MitgliedMapper mitgliedMapper
    ) {
        this.mitgliedService = mitgliedService;
        this.mitgliedMapper = mitgliedMapper;
    }

    /* =========================================================
       CREATE
       ========================================================= */

    @PostMapping
    public ResponseEntity<MitgliedDetailDTO> create(
            @RequestBody @Validated(OnCreate.class) MitgliedDTO dto
    ) {
        Mitglied mitglied = mitgliedService.createMitgliedEntity(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mitgliedMapper.toDetailDTO(mitglied));
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
    public MitgliedDetailDTO update(
            @PathVariable Long id,
            @RequestBody @Validated(OnUpdate.class) MitgliedDTO dto
    ) {
        Mitglied mitglied = mitgliedService.updateMitgliedEntity(id, dto);
        return mitgliedMapper.toDetailDTO(mitglied);
    }
}