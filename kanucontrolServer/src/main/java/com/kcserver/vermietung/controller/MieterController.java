package com.kcserver.vermietung.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.MieterDTO;
import com.kcserver.vermietung.dto.MieterSaveDTO;
import com.kcserver.core.dto.person.PersonRefDTO;
import com.kcserver.vermietung.service.MieterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vermietung/mieter")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
@Tag(name = "Mieter", description = "Mieterverwaltung")
@SecurityRequirement(name = "bearerAuth")
public class MieterController {
    private final MieterService service;

    public MieterController(MieterService service) { this.service = service; }

    @GetMapping public ApiResponse<List<MieterDTO>> getAll() { return ApiResponse.of(service.getAll()); }
    @GetMapping("/{id}") public ApiResponse<MieterDTO> get(@PathVariable Long id) { return ApiResponse.of(service.get(id)); }
    @GetMapping("/person/{personId}") public ApiResponse<PersonRefDTO> getPersonRef(@PathVariable Long personId) { return ApiResponse.of(service.getPersonRef(personId)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ApiResponse<MieterDTO> create(@RequestBody @Valid MieterSaveDTO dto) { return ApiResponse.of(service.create(dto)); }
    @PutMapping("/{id}") public ApiResponse<MieterDTO> update(@PathVariable Long id, @RequestBody @Valid MieterSaveDTO dto) { return ApiResponse.of(service.update(id, dto)); }
    @PutMapping("/{id}/archivieren") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) { service.deactivate(id); }
    // Bereits geladene ältere Clients verwenden DELETE /{id} zum Archivieren.
    // Diesen Aufruf nicht nachträglich zu einer endgültigen Löschung umdeuten.
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateLegacy(@PathVariable Long id) { service.deactivate(id); }
    @DeleteMapping("/{id}/endgueltig") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
