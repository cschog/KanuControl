package com.kcserver.controller;

import com.kcserver.dto.reisekosten.ReisekostenabrechnungListResponse;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.service.VeranstaltungService;
import com.kcserver.service.pdf.*;
import com.kcserver.service.reisekosten.ReisekostenabrechnungService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.util.PdfFilenameUtil;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}")
@RequiredArgsConstructor
public class ReportController {

    private final DokumentService dokumentService;
    private final DokumentValidationService dokumentValidationService;
    private final VeranstaltungService veranstaltungService;
    private final PDFReisekostenabrechnungService reisekostenPdfService;
    private final ReisekostenabrechnungService reisekostenabrechnungService;

   /* =========================================================
   FM / JEM Antrag (Vorschau)
   ========================================================= */

    @GetMapping("/fm-jem-report/view")
    public ResponseEntity<byte[]> viewFmJem(
            @PathVariable Long veranstaltungId
    ) {
        byte[] pdf =
                dokumentService.generateAnmeldung(
                        veranstaltungId
                );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.ANMELDUNG,
                veranstaltung
        );

        return buildInlineResponse(pdf, filename);
    }

/* =========================================================
   FM / JEM Antrag (Download)
   ========================================================= */

    @GetMapping("/fm-jem-report/download")
    public ResponseEntity<byte[]> downloadFmJem(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf =
                dokumentService.generateAnmeldung(
                        veranstaltungId
                );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.ANMELDUNG,
                veranstaltung
        );

        return buildAttachmentResponse(pdf, filename);
    }

   /* =========================================================
   Teilnehmerliste PDF (Vorschau)
   ========================================================= */

    @GetMapping("/teilnehmer/pdf/view")
    public ResponseEntity<byte[]> viewTeilnehmerPdf(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf =
                dokumentService.generateTeilnehmerliste(
                        veranstaltungId
                );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(
                        veranstaltungId
                );

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.TEILNEHMERLISTE,
                veranstaltung
        );

        return buildInlineResponse(pdf, filename);
    }

/* =========================================================
   Teilnehmerliste PDF (Download)
   ========================================================= */

    @GetMapping("/teilnehmer/pdf/download")
    public ResponseEntity<byte[]> downloadTeilnehmerPdf(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf =
                dokumentService.generateTeilnehmerliste(
                        veranstaltungId
                );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(
                        veranstaltungId
                );

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.TEILNEHMERLISTE,
                veranstaltung
        );

        return buildAttachmentResponse(pdf, filename);
    }

   /* =========================================================
   Erhebungsbogen PDF (Vorschau)
   ========================================================= */

    @GetMapping("/erhebungsbogen/pdf/view")
    public ResponseEntity<byte[]> viewErhebungsbogen(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf =
                dokumentService.generateErhebungsbogen(
                        veranstaltungId
                );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.ERHEBUNGSBOGEN,
                veranstaltung
        );

        return buildInlineResponse(pdf, filename);
    }

/* =========================================================
   Erhebungsbogen PDF (Download)
   ========================================================= */

    @GetMapping("/erhebungsbogen/pdf/download")
    public ResponseEntity<byte[]> downloadErhebungsbogen(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf =
                dokumentService.generateErhebungsbogen(
                        veranstaltungId
                );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.ERHEBUNGSBOGEN,
                veranstaltung
        );

        return buildAttachmentResponse(pdf, filename);
    }

    /* =========================================================
       Abrechnung PDF (Inline anzeigen)
       ========================================================= */

    @GetMapping("/abrechnung/pdf/view")
    public ResponseEntity<byte[]> viewAbrechnung(
            @PathVariable Long veranstaltungId
    ) {
        byte[] pdf =
                dokumentService.generateAbrechnung(
                        veranstaltungId
                );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.ABRECHNUNG,
                veranstaltung
        );

        return buildInlineResponse(pdf, filename);
    }

     /* =========================================================
       Abrechnung PDF (download)
       ========================================================= */

    @GetMapping("/abrechnung/pdf/download")
    public ResponseEntity<byte[]> downloadAbrechnung(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf =
                dokumentService.generateAbrechnung(
                        veranstaltungId
                );

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.ABRECHNUNG,
                veranstaltung
        );

        return buildAttachmentResponse(pdf, filename);
    }

    /* =========================================================
       Helper: Attachment Download
       ========================================================= */

    private ResponseEntity<byte[]> buildAttachmentResponse(
            byte[] pdf,
            String filename
    ) {

        String encodedFilename = URLEncoder
                .encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename +
                                "\"; filename*=UTF-8''" + encodedFilename
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /* =========================================================
       Helper: Inline PDF Anzeige
       ========================================================= */

    private ResponseEntity<byte[]> buildInlineResponse(
            byte[] pdf,
            String filename
    ) {
        String encodedFilename = URLEncoder
                .encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename +
                                "\"; filename*=UTF-8''" + encodedFilename
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/reisekosten/{abrechnungId}/pdf/view")
    public ResponseEntity<byte[]> viewReisekosten(
            @PathVariable Long veranstaltungId,
            @PathVariable Long abrechnungId
    ) {

        byte[] pdf = reisekostenPdfService.generate(abrechnungId);

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = PdfFilenameUtil.build(
                LocalDate.now(),
                PdfDokumentTyp.REISEKOSTENABRECHNUNG,
                veranstaltung
        );

        return buildInlineResponse(pdf, filename);
    }

    @GetMapping("/reisekosten/{abrechnungId}/pdf/download")
    public ResponseEntity<byte[]> downloadReisekosten(
            @PathVariable Long veranstaltungId,
            @PathVariable Long abrechnungId
    ) {

        byte[] pdf = reisekostenPdfService.generate(abrechnungId);

        String filename =
                reisekostenPdfService.buildFilename(
                        abrechnungId
                );

        return buildAttachmentResponse(pdf, filename);

    }
    @GetMapping("/reisekosten")
    public List<ReisekostenabrechnungListResponse> listReisekosten(
            @PathVariable Long veranstaltungId
    ) {
        return reisekostenabrechnungService
                .listByVeranstaltung(veranstaltungId);
    }

    @GetMapping("/dokumente/{typ}/validation")
    public ValidationResult validateDokument(
            @PathVariable Long veranstaltungId,
            @PathVariable PdfDokumentTyp typ
    ) {
        return dokumentValidationService.validate(
                veranstaltungId,
                typ
        );
    }
}