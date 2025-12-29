package com.kcserver.controller;

import com.kcserver.dto.VereinDTO;
import com.kcserver.service.VereinService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vereine")
public class VereinController {

    private final VereinService vereinService;

    public VereinController(VereinService vereinService) {
        this.vereinService = vereinService;
    }

    @GetMapping
    public List<VereinDTO> getAll() {
        return vereinService.getAll();
    }

    @GetMapping("/{id}")
    public VereinDTO getById(@PathVariable Long id) {
        return vereinService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VereinDTO create(@RequestBody @Valid VereinDTO dto) {
        return vereinService.create(dto);
    }

    @PutMapping("/{id}")
    public VereinDTO update(
            @PathVariable Long id,
            @RequestBody @Valid VereinDTO dto
    ) {
        return vereinService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vereinService.delete(id);
    }
}