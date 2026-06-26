package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.dto.planung.PlanungPositionDTO;
import com.kcserver.finanz.PlanungPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/planung/positionen")
@RequiredArgsConstructor
public class PlanungPositionController {

    private final PlanungPositionService service;

    /* =========================================================
       ADD
       ========================================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PlanungPositionDTO> add(
            @PathVariable Long veranstaltungId,
            @RequestBody PlanungPositionCreateDTO dto
    ) {
        return ApiResponse.of(
                service.addPosition(veranstaltungId, dto)
        );
    }

    /* =========================================================
       UPDATE
       ========================================================= */

    @PutMapping("/{positionId}")
    public ApiResponse<PlanungPositionDTO> update(
            @PathVariable Long veranstaltungId,
            @PathVariable Long positionId,
            @RequestBody PlanungPositionCreateDTO dto
    ) {
        return ApiResponse.of(
                service.updatePosition(
                        veranstaltungId,
                        positionId,
                        dto
                )
        );
    }

    /* =========================================================
       DELETE
       ========================================================= */

    @DeleteMapping("/{positionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long veranstaltungId,
            @PathVariable Long positionId
    ) {
        service.deletePosition(
                veranstaltungId,
                positionId
        );
    }
}