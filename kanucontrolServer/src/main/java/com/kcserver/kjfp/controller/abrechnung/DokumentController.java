package com.kcserver.kjfp.controller.abrechnung;

import com.kcserver.kjfp.dto.abrechnung.DokumentDTO;
import com.kcserver.kjfp.entity.Dokument;
import com.kcserver.kjfp.enumtype.ReferenzObjekt;
import com.kcserver.kjfp.service.abrechnung.DokumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'KJFP')")
public class DokumentController {

    private final DokumentService dokumentService;
    private final com.kcserver.kjfp.service.pdf.DokumentAusrichtung ausrichtung;

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

        var datei=ausrichtung.ausgabe(dokument);
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore())
                .contentType(MediaType.parseMediaType(datei.mimeType())).contentLength(datei.bytes().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.inline().filename(datei.dateiname(),java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .body(new ByteArrayResource(datei.bytes()));
    }

    @GetMapping("/belege/{belegId}/dokumente/{dokumentId}/ausrichtung")
    public com.kcserver.kjfp.service.pdf.DokumentAusrichtung.Info ausrichtung(@PathVariable Long belegId,@PathVariable Long dokumentId) {
        return ausrichtung.info(dokumentService.getForBeleg(belegId,dokumentId));
    }

    @PutMapping("/belege/{belegId}/dokumente/{dokumentId}/ausrichtung")
    public com.kcserver.kjfp.service.pdf.DokumentAusrichtung.Info ausrichtungSpeichern(@PathVariable Long belegId,@PathVariable Long dokumentId,
            @RequestBody com.kcserver.kjfp.service.pdf.DokumentAusrichtung.Aenderung aenderung) {
        return dokumentService.updateAusrichtung(belegId,dokumentId,aenderung);
    }

    @GetMapping("/belege/{belegId}/dokumente/{dokumentId}/vorschau/{seite}")
    public ResponseEntity<byte[]> vorschau(@PathVariable Long belegId,@PathVariable Long dokumentId,@PathVariable int seite) {
        byte[] bytes=ausrichtung.vorschau(dokumentService.getForBeleg(belegId,dokumentId),seite);
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).contentType(MediaType.IMAGE_PNG).body(bytes);
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