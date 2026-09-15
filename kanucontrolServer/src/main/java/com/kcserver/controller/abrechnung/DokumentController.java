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
     * FINANZAUSGLEICH
     * =========================================================
     */

    /**
     * Alle Finanzausgleich-Dokumente einer FinanzGruppe.
     */
    @GetMapping(
            "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}/finanzausgleich-dokumente"
    )
    public List<DokumentDTO> findAllByFinanzGruppe(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId
    ) {
        return dokumentService.findAllByFinanzGruppe(
                veranstaltungId,
                finanzGruppeId
        );
    }

    /**
     * Dokument zum Finanzausgleich einer FinanzGruppe hochladen.
     */
    @PostMapping(
            value = "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}/finanzausgleich-dokumente",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DokumentDTO uploadForFinanzGruppe(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "referenzObjekt", required = false)
            ReferenzObjekt referenzObjekt
    ) {
        return dokumentService.uploadForFinanzGruppe(
                veranstaltungId,
                finanzGruppeId,
                file,
                referenzObjekt
        );
    }

    /**
     * Dokument eines Finanzausgleichs anzeigen/herunterladen.
     */
    @GetMapping(
            "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}/finanzausgleich-dokumente/{dokumentId}"
    )
    public ResponseEntity<ByteArrayResource> getForFinanzGruppe(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long dokumentId
    ) {
        Dokument dokument =
                dokumentService.getForFinanzGruppe(
                        veranstaltungId,
                        finanzGruppeId,
                        dokumentId
                );

        return createResponse(dokument);
    }

    /**
     * Referenzobjekt eines Finanzausgleich-Dokuments ändern.
     */
    @PutMapping(
            "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}/finanzausgleich-dokumente/{dokumentId}/referenz-objekt"
    )
    public DokumentDTO updateReferenzObjektForFinanzGruppe(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long dokumentId,
            @RequestParam ReferenzObjekt referenzObjekt
    ) {
        return dokumentService.updateReferenzObjektForFinanzGruppe(
                veranstaltungId,
                finanzGruppeId,
                dokumentId,
                referenzObjekt
        );
    }

    /**
     * Dokument eines Finanzausgleichs löschen.
     */
    @DeleteMapping(
            "/veranstaltungen/{veranstaltungId}/finanzgruppen/{finanzGruppeId}/finanzausgleich-dokumente/{dokumentId}"
    )
    public void deleteForFinanzGruppe(
            @PathVariable Long veranstaltungId,
            @PathVariable Long finanzGruppeId,
            @PathVariable Long dokumentId
    ) {
        dokumentService.deleteForFinanzGruppe(
                veranstaltungId,
                finanzGruppeId,
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