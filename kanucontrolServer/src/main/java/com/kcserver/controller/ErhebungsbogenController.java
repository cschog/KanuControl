package com.kcserver.controller;

import com.kcserver.dto.ErhebungsbogenDTO;
import com.kcserver.mapper.ErhebungsbogenMapper;
import com.kcserver.service.ErhebungsbogenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/abrechnung")
@RequiredArgsConstructor
public class ErhebungsbogenController {

    private final ErhebungsbogenService erhebungsbogenService;
    private final ErhebungsbogenMapper erhebungsbogenMapper;

    /* =========================================================
       READ / CREATE
       ========================================================= */

    @GetMapping
    public ResponseEntity<ErhebungsbogenDTO> getOrCreate(
            @PathVariable Long veranstaltungId
    ) {
        return ResponseEntity.ok(
                erhebungsbogenMapper.toDTO(
                        erhebungsbogenService.getOrCreate(veranstaltungId)
                )
        );
    }

    /* =========================================================
       BERECHNEN
       ========================================================= */

    @PostMapping("/berechnen")
    public ResponseEntity<ErhebungsbogenDTO> berechnen(
            @PathVariable Long veranstaltungId
    ) {
        return ResponseEntity.ok(
                erhebungsbogenMapper.toDTO(
                        erhebungsbogenService.berechneStatistik(veranstaltungId)
                )
        );
    }

    /* =========================================================
       ABSCHLIESSEN
       ========================================================= */

    @PostMapping("/abschliessen")
    public ResponseEntity<Void> abschliessen(
            @PathVariable Long veranstaltungId
    ) {
        erhebungsbogenService.abschliessen(veranstaltungId);
        return ResponseEntity.noContent().build();
    }
}