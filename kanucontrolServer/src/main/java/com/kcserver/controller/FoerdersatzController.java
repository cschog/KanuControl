package com.kcserver.controller;

import com.kcserver.dto.foerder.FoerdersatzCreateUpdateDTO;
import com.kcserver.dto.foerder.FoerdersatzDTO;
import com.kcserver.service.FoerdersatzService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/foerdersaetze")
public class FoerdersatzController {

    private final FoerdersatzService service;

    public FoerdersatzController(FoerdersatzService service) {
        this.service = service;
    }

    @PostMapping
    public FoerdersatzDTO create(
            @RequestBody FoerdersatzCreateUpdateDTO dto
    ) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public FoerdersatzDTO update(
            @PathVariable Long id,
            @RequestBody FoerdersatzCreateUpdateDTO dto
    ) {
        return service.update(id, dto);
    }

    @GetMapping("/{id}")
    public FoerdersatzDTO get(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<FoerdersatzDTO> list() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/gueltig")
    public FoerdersatzDTO gueltigAm(
            @RequestParam LocalDate datum
    ) {
        return service.findGueltigAm(datum);
    }
}