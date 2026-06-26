package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.mitglied.MitgliedDTO;
import com.kcserver.dto.mitglied.MitgliedDetailDTO;
import com.kcserver.entity.Mitglied;
import org.springframework.data.domain.Pageable;
import com.kcserver.mapper.MitgliedMapper;
import com.kcserver.service.MitgliedService;
import com.kcserver.validation.OnCreate;
import com.kcserver.validation.OnUpdate;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MitgliedDetailDTO> create(
            @RequestBody @Validated(OnCreate.class) MitgliedDTO dto
    ) {
        Mitglied mitglied = mitgliedService.createMitgliedEntity(dto);

        return ApiResponse.of(
                mitgliedMapper.toDetailDTO(mitglied)
        );
    }

    /* =========================================================
       READ
       ========================================================= */

    @GetMapping("/{id}")
    public ApiResponse<MitgliedDetailDTO> getById(
            @PathVariable Long id
    ) {
        Mitglied mitglied = mitgliedService.getEntityByIdWithVerein(id);

        return ApiResponse.of(
                mitgliedMapper.toDetailDTO(mitglied)
        );
    }

    @GetMapping("/person/{personId}")
    public ApiResponse<List<MitgliedDTO>> getByPerson(
            @PathVariable Long personId,
            Pageable pageable
    ) {
        return ApiResponse.of(
                mitgliedService.getByPerson(personId, pageable)
        );
    }

    @GetMapping("/verein/{vereinId}")
    public ApiResponse<List<MitgliedDTO>> getByVerein(
            @PathVariable Long vereinId
    ) {
        return ApiResponse.of(
                mitgliedService.getByVerein(vereinId)
        );
    }

    @GetMapping("/person/{personId}/hauptverein")
    public ApiResponse<MitgliedDTO> getHauptvereinByPerson(
            @PathVariable Long personId
    ) {
        return ApiResponse.of(
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
    public ApiResponse<MitgliedDetailDTO> update(
            @PathVariable Long id,
            @RequestBody @Validated(OnUpdate.class) MitgliedDTO dto
    ) {
        Mitglied mitglied =
                mitgliedService.updateMitgliedEntity(id, dto);

        return ApiResponse.of(
                mitgliedMapper.toDetailDTO(mitglied)
        );
    }

    @PutMapping("/{id}/hauptverein")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setHauptverein(@PathVariable Long id) {
        mitgliedService.setHauptverein(id);
    }
}