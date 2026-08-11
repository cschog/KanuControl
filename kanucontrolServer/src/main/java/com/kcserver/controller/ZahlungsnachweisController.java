package com.kcserver.controller;

import com.kcserver.api.response.ApiResponse;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisDetailDTO;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisDokumentDTO;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisListDTO;
import com.kcserver.dto.zahlungsnachweis.ZahlungsnachweisUpdateDTO;
import com.kcserver.entity.ZahlungsnachweisDokument;
import com.kcserver.service.abrechnung.ZahlungsnachweisDokumentService;
import com.kcserver.service.abrechnung.ZahlungsnachweisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veranstaltungen/{veranstaltungId}/zahlungsnachweise")
public class ZahlungsnachweisController {

    private final ZahlungsnachweisService zahlungsnachweisService;
    private final ZahlungsnachweisDokumentService zahlungsnachweisDokumentService;

    /* =========================================================
       ZAHLUNGSNACHWEISE
       ========================================================= */

    @GetMapping
    public ApiResponse<List<ZahlungsnachweisListDTO>> getAll(
            @PathVariable Long veranstaltungId
    ) {
        return ApiResponse.of(
                zahlungsnachweisService.findByVeranstaltung(
                        veranstaltungId
                )
        );
    }

    @GetMapping("/{zahlungsnachweisId}")
    public ApiResponse<ZahlungsnachweisDetailDTO> get(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId
    ) {
        return ApiResponse.of(
                zahlungsnachweisService.get(
                        veranstaltungId,
                        zahlungsnachweisId
                )
        );
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<ZahlungsnachweisDetailDTO> create(
            @PathVariable Long veranstaltungId,
            @RequestBody ZahlungsnachweisUpdateDTO dto
    ) {
        return ApiResponse.of(
                zahlungsnachweisService.create(
                        veranstaltungId,
                        dto
                )
        );
    }

    @PutMapping("/{zahlungsnachweisId}")
    public ApiResponse<ZahlungsnachweisDetailDTO> update(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId,
            @Valid @RequestBody ZahlungsnachweisUpdateDTO dto
    ) {
        return ApiResponse.of(
                zahlungsnachweisService.update(
                        veranstaltungId,
                        zahlungsnachweisId,
                        dto
                )
        );
    }

    @DeleteMapping("/{zahlungsnachweisId}")
    public ApiResponse<Void> delete(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId
    ) {
        zahlungsnachweisService.delete(
                veranstaltungId,
                zahlungsnachweisId
        );

        return ApiResponse.of(null);
    }

    /* =========================================================
       DOKUMENTE
       ========================================================= */

    @GetMapping("/{zahlungsnachweisId}/dokumente")
    public ApiResponse<List<ZahlungsnachweisDokumentDTO>> getDokumente(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId
    ) {
        return ApiResponse.of(
                zahlungsnachweisDokumentService.findAll(
                        veranstaltungId,
                        zahlungsnachweisId
                )
        );
    }

    @PostMapping("/{zahlungsnachweisId}/dokumente")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ZahlungsnachweisDokumentDTO> uploadDokument(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.of(
                zahlungsnachweisDokumentService.upload(
                        veranstaltungId,
                        zahlungsnachweisId,
                        file
                )
        );
    }

    @DeleteMapping("/{zahlungsnachweisId}/dokumente/{dokumentId}")
    public ApiResponse<Void> deleteDokument(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId,
            @PathVariable Long dokumentId
    ) {
        zahlungsnachweisDokumentService.delete(
                veranstaltungId,
                zahlungsnachweisId,
                dokumentId
        );

        return ApiResponse.of(null);
    }

    @GetMapping("/{zahlungsnachweisId}/dokumente/{dokumentId}/download")
    public ResponseEntity<byte[]> downloadDokument(
            @PathVariable Long veranstaltungId,
            @PathVariable Long zahlungsnachweisId,
            @PathVariable Long dokumentId
    ) {
        ZahlungsnachweisDokument dokument =
                zahlungsnachweisDokumentService.get(
                        veranstaltungId,
                        zahlungsnachweisId,
                        dokumentId
                );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                dokument.getMimeType()
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                dokument.getOriginalDateiname() +
                                "\""
                )
                .body(dokument.getInhalt());
    }
}