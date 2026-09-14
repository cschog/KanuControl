package com.kcserver.controller.abrechnung;

import com.kcserver.dto.abrechnung.DokumentDTO;
import com.kcserver.entity.Dokument;
import com.kcserver.enumtype.ReferenzObjekt;
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
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "referenzObjekt", required = false)
            ReferenzObjekt referenzObjekt
    ) {
        return dokumentService.uploadForBeleg(
                belegId,
                file,
                referenzObjekt
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

    @PutMapping("/belege/{belegId}/dokumente/{dokumentId}/referenz-objekt")
    public DokumentDTO updateReferenzObjektForBeleg(
            @PathVariable Long belegId,
            @PathVariable Long dokumentId,
            @RequestParam ReferenzObjekt referenzObjekt
    ) {
        return dokumentService.updateReferenzObjektForBeleg(
                belegId,
                dokumentId,
                referenzObjekt
        );
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
     * FINANZAUSGLEICHSZAHLUNG
     * =========================================================
     */

    /**
     * Alle Dokumente einer Finanzausgleichszahlung.
     */
    @GetMapping(
            "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}" +
                    "/finanzausgleich-zahlungen/{zahlungId}/dokumente"
    )
    public List<DokumentDTO> findAllByFinanzausgleichZahlung(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long zahlungId
    ) {
        return dokumentService.findAllByFinanzausgleichZahlung(
                veranstaltungId,
                finanzGruppeId,
                zahlungId
        );
    }

    /**
     * Dokument zu einer Finanzausgleichszahlung hochladen.
     */
    @PostMapping(
            value = "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}" +
                    "/finanzausgleich-zahlungen/{zahlungId}/dokumente",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DokumentDTO uploadForFinanzausgleichZahlung(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long zahlungId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "referenzObjekt", required = false)
            ReferenzObjekt referenzObjekt
    ) {
        return dokumentService.uploadForFinanzausgleichZahlung(
                veranstaltungId,
                finanzGruppeId,
                zahlungId,
                file,
                referenzObjekt
        );
    }

    /**
     * Dokument einer Finanzausgleichszahlung anzeigen/herunterladen.
     */
    @GetMapping(
            "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}" +
                    "/finanzausgleich-zahlungen/{zahlungId}/dokumente/{dokumentId}"
    )
    public ResponseEntity<ByteArrayResource> getForFinanzausgleichZahlung(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long zahlungId,
            @PathVariable Long dokumentId
    ) {

        Dokument dokument =
                dokumentService.getForFinanzausgleichZahlung(
                        veranstaltungId,
                        finanzGruppeId,
                        zahlungId,
                        dokumentId
                );

        return createResponse(dokument);
    }

    /**
     * Dokument einer Finanzausgleichszahlung löschen.
     */
    @DeleteMapping(
            "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}" +
                    "/finanzausgleich-zahlungen/{zahlungId}/dokumente/{dokumentId}"
    )
    public void deleteForFinanzausgleichZahlung(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long zahlungId,
            @PathVariable Long dokumentId
    ) {
        dokumentService.deleteForFinanzausgleichZahlung(
                veranstaltungId,
                finanzGruppeId,
                zahlungId,
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