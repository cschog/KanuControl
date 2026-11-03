package com.kcserver.vermietung.controller;
import com.kcserver.api.response.ApiResponse;
import com.kcserver.vermietung.dto.SperrzeitDTO;
import com.kcserver.vermietung.service.SperrzeitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/vermietung/objekte/{objektId}/sperrzeiten")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
public class SperrzeitController {
    private final SperrzeitService service;
    @GetMapping public ApiResponse<List<SperrzeitDTO>> list(@PathVariable Long objektId) {
        return ApiResponse.of(service.list(objektId));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SperrzeitDTO> create(@PathVariable Long objektId, @Valid @RequestBody SperrzeitDTO dto) {
        return ApiResponse.of(service.save(objektId, null, dto));
    }
    @PutMapping("/{id}")
    public ApiResponse<SperrzeitDTO> update(@PathVariable Long objektId, @PathVariable Long id, @Valid @RequestBody SperrzeitDTO dto) {
        return ApiResponse.of(service.save(objektId, id, dto));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long objektId, @PathVariable Long id, @RequestParam Long version) {
        service.delete(objektId, id, version);
    }
}
