package com.kcserver.kjfp.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.kjfp.dto.kik.KikZuschlagCreateUpdateDTO;
import com.kcserver.kjfp.dto.kik.KikZuschlagDTO;
import com.kcserver.kjfp.service.KikZuschlagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kikZuschlag")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
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