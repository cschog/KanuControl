package com.kcserver.vermietung.controller;

import com.kcserver.vermietung.dto.MietpreisDTO;
import com.kcserver.vermietung.service.MietpreisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vermietung/mietpreise")
@RequiredArgsConstructor
@Tag(name = "Vermietung – Mietpreise")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'VERMIETUNG')")
public class MietpreisController {

    private final MietpreisService mietpreisService;

    @GetMapping("/mietbereich/{mietbereichId}")
    @Operation(summary = "Mietpreise eines Mietbereichs abrufen")
    public List<MietpreisDTO> findeNachMietbereich(
            @PathVariable Long mietbereichId
    ) {
        return mietpreisService.findeNachMietbereich(mietbereichId);
    }

    @PostMapping
    @Operation(summary = "Neuen Mietpreis anlegen")
    public MietpreisDTO anlegen(
            @Valid @RequestBody MietpreisDTO dto
    ) {
        return mietpreisService.anlegen(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mietpreis aktualisieren")
    public MietpreisDTO aktualisieren(
            @PathVariable Long id,
            @Valid @RequestBody MietpreisDTO dto
    ) {
        return mietpreisService.aktualisieren(id, dto);
    }
}