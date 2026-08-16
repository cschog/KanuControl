package com.kcserver.controller.abrechnung;

import com.kcserver.dto.abrechnung.DokumentDTO;
import com.kcserver.entity.Dokument;
import com.kcserver.service.abrechnung.DokumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DokumentController {

    private final DokumentService dokumentService;

    /*
     * =========================================================
     * BELEG
     * =========================================================
     */

    /**
     * Alle Dokumente eines Belegs.
     */
    @GetMapping("/belege/{belegId}/dokumente")
    public List<DokumentDTO> findAllByBeleg(
            @PathVariable Long belegId
    ) {
        return dokumentService.findAllByBeleg(belegId);
    }

    /**
     * Dokument zu einem Beleg hochladen.
     */
    @PostMapping(
            value = "/belege/{belegId}/dokumente",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DokumentDTO uploadForBeleg(
            @PathVariable Long belegId,
            @RequestParam("file") MultipartFile file
    ) {
        return dokumentService.uploadForBeleg(
                belegId,
                file
        );
    }

    /**
     * Dokument eines Belegs anzeigen/herunterladen.
     */
    @GetMapping("/belege/{belegId}/dokumente/{dokumentId}")
    public ResponseEntity<ByteArrayResource> getForBeleg(
            @PathVariable Long belegId,
            @PathVariable Long dokumentId
    ) {

        Dokument dokument =
                dokumentService.getForBeleg(
                        belegId,
                        dokumentId
                );

        return createResponse(dokument);
    }

    /**
     * Dokument eines Belegs löschen.
     */
    @DeleteMapping("/belege/{belegId}/dokumente/{dokumentId}")
    public void deleteForBeleg(
            @PathVariable Long belegId,
            @PathVariable Long dokumentId
    ) {
        dokumentService.deleteForBeleg(
                belegId,
                dokumentId
        );
    }

    /*
     * =========================================================
     * RESPONSE
     * =========================================================
     */

    private ResponseEntity<ByteArrayResource> createResponse(
            Dokument dokument
    ) {

        ByteArrayResource resource =
                new ByteArrayResource(
                        dokument.getInhalt()
                );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                dokument.getMimeType()
                        )
                )
                .contentLength(
                        dokument.getDateigroesse()
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(
                                        dokument.getOriginalDateiname()
                                )
                                .build()
                                .toString()
                )
                .body(resource);
    }
}