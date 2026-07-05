package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.beitrag.BeitragsregelCreateDTO;
import com.kcserver.dto.beitrag.BeitragsstrukturDTO;
import com.kcserver.dto.beitrag.BeitragsstrukturUpdateDTO;
import com.kcserver.service.beitrag.BeitragsstrukturService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beitragsstrukturen")
public class BeitragsstrukturController {

    private final BeitragsstrukturService service;

    @GetMapping("/templates")
    public ApiResponse<List<BeitragsstrukturDTO>> getTemplates() {
        return ApiResponse.of(service.getTemplates());
    }

    @GetMapping
    public ApiResponse<List<BeitragsstrukturDTO>> getAll() {
        return ApiResponse.of(service.getAll());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }

    @DeleteMapping("/regeln/{regelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRegel(
            @PathVariable Long regelId
    ) {
        service.deleteRegel(regelId);
    }

    @PutMapping("/{id}/regeln")
    public ApiResponse<BeitragsstrukturDTO> updateRegeln(
            @PathVariable Long id,
            @RequestBody @Valid List<BeitragsregelCreateDTO> dto
    ) {
        return ApiResponse.of(
                service.updateRegeln(id, dto)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<BeitragsstrukturDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid BeitragsstrukturUpdateDTO dto
    ) {
        return ApiResponse.of(
                service.update(id, dto)
        );
    }

    @PutMapping("/regeln/{regelId}")
    public ApiResponse<BeitragsstrukturDTO> updateRegel(
            @PathVariable Long regelId,
            @RequestBody @Valid BeitragsregelCreateDTO dto
    ) {
        return ApiResponse.of(
                service.updateRegel(regelId, dto)
        );
    }

    @PostMapping("/{id}/copy")
    public ApiResponse<BeitragsstrukturDTO> copy(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        return ApiResponse.of(
                service.copy(id, name)
        );
    }

    @PostMapping("/copy/{templateId}")
    public ApiResponse<BeitragsstrukturDTO> copyFromTemplate(
            @PathVariable Long templateId,
            @RequestParam String name
    ) {
        return ApiResponse.of(
                service.copyFromTemplate(templateId, name)
        );
    }

    @PostMapping("/{id}/regeln")
    public ApiResponse<BeitragsstrukturDTO> addRegel(
            @PathVariable Long id,
            @RequestBody @Valid BeitragsregelCreateDTO dto
    ) {
        return ApiResponse.of(
                service.addRegel(id, dto)
        );
    }

    @PostMapping("/create")
    public ApiResponse<BeitragsstrukturDTO> create(
            @RequestParam String name
    ) {
        return ApiResponse.of(
                service.createFromDefaultTemplate(name)
        );
    }

    @PostMapping
    public ApiResponse<BeitragsstrukturDTO> createTemplate(
            @RequestBody @Valid BeitragsstrukturDTO dto
    ) {
        return ApiResponse.of(
                service.createTemplate(dto)
        );
    }
}