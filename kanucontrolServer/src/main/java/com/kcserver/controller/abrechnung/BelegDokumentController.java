package com.kcserver.controller.abrechnung;

import com.kcserver.dto.abrechnung.BelegDokumentDTO;
import com.kcserver.entity.BelegDokument;
import com.kcserver.service.abrechnung.BelegDokumentService;
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
@RequestMapping("/api/belege")
@RequiredArgsConstructor
public class BelegDokumentController {

    private final BelegDokumentService belegDokumentService;

    /**
     * Alle Dokumente eines Belegs.
     */
    @GetMapping("/{belegId}/dokumente")
    public List<BelegDokumentDTO> findAll(
            @PathVariable Long belegId
    ) {
        return belegDokumentService.findAll(belegId);
    }

    /**
     * Dokument hochladen.
     */
    @PostMapping(
            value = "/{belegId}/dokumente",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public BelegDokumentDTO upload(
            @PathVariable Long belegId,
            @RequestParam("file") MultipartFile file
    ) {
        return belegDokumentService.upload(belegId, file);
    }

    /**
     * Liefert den eigentlichen Dokumentinhalt.
     * .
     * Bilder werden im Browser angezeigt,
     * PDFs geöffnet,
     * andere Dateitypen heruntergeladen.
     */
    @GetMapping("/dokumente/{dokumentId}")
    public ResponseEntity<ByteArrayResource> download(
            @PathVariable Long dokumentId
    ) {

        BelegDokument dokument =
                belegDokumentService.findById(dokumentId);

        ByteArrayResource resource =
                new ByteArrayResource(dokument.getInhalt());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dokument.getMimeType()))
                .contentLength(dokument.getDateigroesse())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(dokument.getOriginalDateiname())
                                .build()
                                .toString()
                )
                .body(resource);
    }

    /**
     * Dokument löschen.
     */
    @DeleteMapping("/dokumente/{dokumentId}")
    public void delete(
            @PathVariable Long dokumentId
    ) {
        belegDokumentService.delete(dokumentId);
    }
}