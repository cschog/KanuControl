package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.verein.VereinDTO;
import com.kcserver.dto.verein.VereinRefDTO;
import com.kcserver.service.VereinService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/verein")
@Tag(name = "Verein", description = "Verwaltung von Vereinen")
@SecurityRequirement(name = "bearerAuth")
public class VereinController {

    private final VereinService vereinService;

    public VereinController(VereinService vereinService) {
        this.vereinService = vereinService;
    }

    @GetMapping
    public ApiResponse<List<VereinDTO>> getAll() {

        return new ApiResponse<>(
                vereinService.getAll(),
                List.of()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<VereinDTO> getById(
            @PathVariable Long id) {

        return new ApiResponse<>(
                vereinService.getById(id),
                List.of()
        );
    }

    @GetMapping("/search")
    public ApiResponse<Page<VereinDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String abk,
            Pageable pageable
    ) {
        return new ApiResponse<>(
                vereinService.search(name, abk, pageable),
                List.of()
        );
    }
    @GetMapping("/search/all")
    public ApiResponse<List<VereinDTO>> searchAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String abk
    ) {
        return new ApiResponse<>(
                vereinService.searchAll(name, abk),
                List.of()

        );
    }


    @GetMapping("/search/ref")
    public ApiResponse<List<VereinRefDTO>> searchRef(
            @RequestParam(required = false) String search
    ) {
        return new ApiResponse<>(
                vereinService.searchRefList(search),
                List.of()

        );
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VereinDTO> create(
            @RequestBody @Valid VereinDTO dto) {

        return new ApiResponse<>(
                vereinService.create(dto),
                List.of()
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<VereinDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid VereinDTO dto) {

        return new ApiResponse<>(
                vereinService.update(id, dto),
                List.of()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vereinService.delete(id);
    }
}