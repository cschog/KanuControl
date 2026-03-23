package com.kcserver.controller;

import com.kcserver.dto.verein.VereinDTO;
import com.kcserver.dto.verein.VereinRefDTO;
import com.kcserver.service.VereinService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    public List<VereinDTO> getAll() {
        return vereinService.getAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Verein nach ID laden",
            description = "Lädt einen Verein anhand seiner ID."
    )
    @ApiResponse(responseCode = "200", description = "Verein gefunden")
    @ApiResponse(responseCode = "404", description = "Verein nicht gefunden")
    public VereinDTO getById(@PathVariable Long id) {
        return vereinService.getById(id);
    }

    @GetMapping("/search")
    public Page<VereinDTO> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String abk,
            Pageable pageable
    ) {
        return vereinService.search(name, abk, pageable);
    }
    @GetMapping("/search/all")
    public List<VereinDTO> searchAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String abk
    ) {
        return vereinService.searchAll(name, abk);
    }


    @GetMapping("/search/ref")
    public List<VereinRefDTO> searchRef(
            @RequestParam(required = false) String search
    ) {
        return vereinService.searchRefList(search);
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