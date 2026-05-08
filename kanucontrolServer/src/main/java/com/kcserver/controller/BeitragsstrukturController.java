package com.kcserver.controller;

import com.kcserver.dto.beitrag.BeitragsregelCreateDTO;
import com.kcserver.dto.beitrag.BeitragsstrukturDTO;
import com.kcserver.dto.beitrag.BeitragsstrukturUpdateDTO;
import com.kcserver.service.BeitragsstrukturService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beitragsstrukturen")
public class BeitragsstrukturController {

    private final BeitragsstrukturService service;

    @GetMapping("/templates")
    public List<BeitragsstrukturDTO> getTemplates() {
        return service.getTemplates();
    }

    @GetMapping
    public List<BeitragsstrukturDTO> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }

    @DeleteMapping("/regeln/{regelId}")
    public void deleteRegel(
            @PathVariable Long regelId
    ) {
        service.deleteRegel(regelId);
    }

    @PutMapping("/{id}/regeln")
    public BeitragsstrukturDTO updateRegeln(
            @PathVariable Long id,
            @RequestBody List<BeitragsregelCreateDTO> dto
    ) {
        return service.updateRegeln(id, dto);
    }

    @PutMapping("/{id}")
    public BeitragsstrukturDTO update(
            @PathVariable Long id,
            @RequestBody BeitragsstrukturUpdateDTO dto
    ) {
        return service.update(id, dto);
    }

    @PutMapping("/regeln/{regelId}")
    public BeitragsstrukturDTO updateRegel(
            @PathVariable Long regelId,
            @RequestBody BeitragsregelCreateDTO dto
    ) {
        return service.updateRegel(regelId, dto);
    }

    @PostMapping("/{id}/copy")
    public BeitragsstrukturDTO copy(
            @PathVariable Long id,
            @RequestParam String name
    ) {

        return service.copy(id, name);
    }

    @PostMapping("/copy/{templateId}")
    public BeitragsstrukturDTO copyFromTemplate(
            @PathVariable Long templateId,
            @RequestParam String name
    ) {
        return service.copyFromTemplate(templateId, name);
    }
    @PostMapping("/{id}/regeln")
    public BeitragsstrukturDTO addRegel(
            @PathVariable Long id,
            @RequestBody BeitragsregelCreateDTO dto
    ) {
        return service.addRegel(id, dto);
    }
    @PostMapping("/create")
    public BeitragsstrukturDTO create(
            @RequestParam String name
    ) {
        return service.createFromDefaultTemplate(name);
    }
    @PostMapping
    public BeitragsstrukturDTO createTemplate(
            @RequestBody BeitragsstrukturDTO dto
    ) {
        return service.createTemplate(dto);
    }
}