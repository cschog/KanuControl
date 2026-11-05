package com.kcserver.kjfp.controller.teilnehmer;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.core.dto.teilnehmer.TeilnehmerAddBulkDTO;
import org.springframework.http.HttpStatus;
import com.kcserver.core.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.kjfp.dto.teilnehmer.TeilnehmerKjfpZuordnungDTO;
import com.kcserver.kjfp.service.teilnehmer.KjfpTeilnehmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/teilnehmer")
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
public class KjfpTeilnehmerController {
    private final KjfpTeilnehmerService teilnehmerService;

    @PutMapping("/{teilnehmerId}/kjfp-zuordnung")
    public ApiResponse<TeilnehmerDetailDTO> setKjfpZuordnung(
            @PathVariable Long veranstaltungId,
            @PathVariable Long teilnehmerId,
            @RequestBody TeilnehmerKjfpZuordnungDTO dto
    ) {
        return ApiResponse.of(teilnehmerService.setKjfpZuordnung(veranstaltungId, teilnehmerId, dto.isZugeordnet()));
    }

    @PostMapping("/{personId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TeilnehmerDetailDTO> addTeilnehmer(
            @PathVariable Long veranstaltungId,
            @PathVariable Long personId
    ) {
        return ApiResponse.of(
                teilnehmerService.addTeilnehmer(
                        veranstaltungId,
                        personId
                )
        );
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTeilnehmerBulk(
            @PathVariable Long veranstaltungId,
            @RequestBody TeilnehmerAddBulkDTO dto
    ) {
        teilnehmerService.addTeilnehmerBulk(
                veranstaltungId,
                dto.getPersonIds()
        );
    }
}
