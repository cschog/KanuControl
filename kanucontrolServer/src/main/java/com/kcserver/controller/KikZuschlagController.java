package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.kik.KikZuschlagCreateUpdateDTO;
import com.kcserver.dto.kik.KikZuschlagDTO;
import com.kcserver.service.KikZuschlagService;
import jakarta.validation.Valid;
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
    public ApiResponse<List<KikZuschlagDTO>> getAll() {
        return ApiResponse.of(
                service.findAll()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KikZuschlagDTO> create(
            @RequestBody @Valid KikZuschlagCreateUpdateDTO dto
    ) {
        return ApiResponse.of(
                service.create(dto)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<KikZuschlagDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid KikZuschlagCreateUpdateDTO dto
    ) {
        return ApiResponse.of(
                service.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        service.delete(id);
    }
}