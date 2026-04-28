package com.kcserver.controller;

import com.kcserver.dto.kik.KikZuschlagCreateUpdateDTO;
import com.kcserver.dto.kik.KikZuschlagDTO;
import com.kcserver.service.KikZuschlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kikZuschlag")
@RequiredArgsConstructor
public class KikZuschlagController {

    private final KikZuschlagService service;

    @GetMapping
    public List<KikZuschlagDTO> getAll() {
        return service.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KikZuschlagDTO create(
            @RequestBody KikZuschlagCreateUpdateDTO dto
    ) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public KikZuschlagDTO update(
            @PathVariable Long id,
            @RequestBody KikZuschlagCreateUpdateDTO dto
    ) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}