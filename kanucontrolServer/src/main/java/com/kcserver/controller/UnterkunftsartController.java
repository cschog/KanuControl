package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.unterkunft.UnterkunftsartCreateUpdateDTO;
import com.kcserver.dto.unterkunft.UnterkunftsartDTO;
import com.kcserver.service.UnterkunftsartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unterkunftsarten")
@RequiredArgsConstructor
public class UnterkunftsartController {

    private final UnterkunftsartService service;

    @GetMapping
    public ApiResponse<List<UnterkunftsartDTO>> getAll() {
        return ApiResponse.of(service.getAll());
    }

    @GetMapping("/active")
    public ApiResponse<List<UnterkunftsartDTO>> getActive() {
        return ApiResponse.of(service.getActive());
    }

    @GetMapping("/{id}")
    public ApiResponse<UnterkunftsartDTO> getById(
            @PathVariable Long id) {

        return ApiResponse.of(service.getById(id));
    }

    @PostMapping
    public ApiResponse<UnterkunftsartDTO> create(
            @Valid @RequestBody UnterkunftsartCreateUpdateDTO dto) {

        return ApiResponse.of(service.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<UnterkunftsartDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UnterkunftsartCreateUpdateDTO dto) {

        return ApiResponse.of(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ApiResponse.of(null);
    }
}