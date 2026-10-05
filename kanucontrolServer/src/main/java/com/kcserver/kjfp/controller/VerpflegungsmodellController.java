package com.kcserver.kjfp.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellCreateUpdateDTO;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellDTO;
import com.kcserver.kjfp.dto.verpflegung.VerpflegungsmodellRefDTO;
import com.kcserver.kjfp.service.VerpflegungsmodellService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verpflegungsmodelle")
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
@RequiredArgsConstructor
public class VerpflegungsmodellController {

    private final VerpflegungsmodellService service;

    @GetMapping
    public ApiResponse<List<VerpflegungsmodellDTO>> getAll() {
        return ApiResponse.of(service.getAll());
    }

    @GetMapping("/active")
    public ApiResponse<List<VerpflegungsmodellDTO>> getActive() {
        return ApiResponse.of(service.getActive());
    }

    @GetMapping("/{id}")
    public ApiResponse<VerpflegungsmodellDTO> getById(
            @PathVariable Long id) {

        return ApiResponse.of(service.getById(id));
    }

    @GetMapping("/refs")
    public ApiResponse<List<VerpflegungsmodellRefDTO>> getRefs() {
        return ApiResponse.of(service.getRefs());
    }

    @PostMapping
    public ApiResponse<VerpflegungsmodellDTO> create(
            @Valid @RequestBody VerpflegungsmodellCreateUpdateDTO dto) {

        return ApiResponse.of(service.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<VerpflegungsmodellDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody VerpflegungsmodellCreateUpdateDTO dto) {

        return ApiResponse.of(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ApiResponse.of(null);
    }
}