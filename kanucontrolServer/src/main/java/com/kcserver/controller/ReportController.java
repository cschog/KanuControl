package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.service.VeranstaltungService;
import com.kcserver.service.pdf.PDFAbrechnungService;
import com.kcserver.service.pdf.PDFErhebungsbogenService;
import com.kcserver.service.pdf.PDFFmJemReportService;
import com.kcserver.service.pdf.PDFTeilnehmerlisteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/veranstaltungen/{veranstaltungId}")
@RequiredArgsConstructor
public class ReportController {

    private final PDFFmJemReportService fmJemReportService;
    private final PDFTeilnehmerlisteService teilnehmerlisteService;
    private final PDFErhebungsbogenService erhebungsbogenService;
    private final PDFAbrechnungService abrechnungService;

    private final VeranstaltungService veranstaltungService;
    private final TeilnehmerService teilnehmerService;

   /* =========================================================
   FM / JEM Antrag (Vorschau)
   ========================================================= */

    @GetMapping("/fm-jem-report/view")
    public ResponseEntity<byte[]> viewFmJem(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf = fmJemReportService.generate(veranstaltungId);

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = buildFilename("FMJEM", veranstaltung.getName());

        return buildInlineResponse(pdf, filename);
    }

/* =========================================================
   FM / JEM Antrag (Download)
   ========================================================= */

    @GetMapping("/fm-jem-report/download")
    public ResponseEntity<byte[]> downloadFmJem(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf = fmJemReportService.generate(veranstaltungId);

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = buildFilename("FMJEM", veranstaltung.getName());

        return buildAttachmentResponse(pdf, filename);
    }

   /* =========================================================
   Teilnehmerliste PDF (Vorschau)
   ========================================================= */

    @GetMapping("/teilnehmer/pdf/view")
    public ResponseEntity<byte[]> viewTeilnehmerPdf(
            @PathVariable Long veranstaltungId
    ) throws Exception {

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        List<TeilnehmerDetailDTO> teilnehmer =
                teilnehmerService.findAllDetails(veranstaltungId);

        byte[] pdf = teilnehmerlisteService.generate(
                veranstaltung,
                teilnehmer
        );

        String filename = buildFilename("TN", veranstaltung.getName());

        return buildInlineResponse(pdf, filename);
    }

/* =========================================================
   Teilnehmerliste PDF (Download)
   ========================================================= */

    @GetMapping("/teilnehmer/pdf/download")
    public ResponseEntity<byte[]> downloadTeilnehmerPdf(
            @PathVariable Long veranstaltungId
    ) throws Exception {

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        List<TeilnehmerDetailDTO> teilnehmer =
                teilnehmerService.findAllDetails(veranstaltungId);

        byte[] pdf = teilnehmerlisteService.generate(
                veranstaltung,
                teilnehmer
        );

        String filename = buildFilename("TN", veranstaltung.getName());

        return buildAttachmentResponse(pdf, filename);
    }

   /* =========================================================
   Erhebungsbogen PDF (Vorschau)
   ========================================================= */

    @GetMapping("/erhebungsbogen/pdf/view")
    public ResponseEntity<byte[]> viewErhebungsbogen(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf = erhebungsbogenService.generate(veranstaltungId);

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = buildFilename("EB", veranstaltung.getName());

        return buildInlineResponse(pdf, filename);
    }

/* =========================================================
   Erhebungsbogen PDF (Download)
   ========================================================= */

    @GetMapping("/erhebungsbogen/pdf/download")
    public ResponseEntity<byte[]> downloadErhebungsbogen(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf = erhebungsbogenService.generate(veranstaltungId);

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = buildFilename("EB", veranstaltung.getName());

        return buildAttachmentResponse(pdf, filename);
    }

    /* =========================================================
       Abrechnung PDF (Inline anzeigen)
       ========================================================= */

    @GetMapping("/abrechnung/pdf/view")
    public ResponseEntity<byte[]> viewAbrechnung(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf = abrechnungService.generate(veranstaltungId);

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = buildFilename("AB", veranstaltung.getName());

        return buildInlineResponse(pdf, filename);
    }

     /* =========================================================
       Abrechnung PDF (download)
       ========================================================= */

    @GetMapping("/abrechnung/pdf/download")
    public ResponseEntity<byte[]> downloadAbrechnung(
            @PathVariable Long veranstaltungId
    ) {

        byte[] pdf = abrechnungService.generate(veranstaltungId);

        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(veranstaltungId);

        String filename = buildFilename("AB", veranstaltung.getName());

        return buildAttachmentResponse(pdf, filename);
    }

    /* =========================================================
       Helper: Dateiname
       ========================================================= */

    private String buildFilename(String prefix, String rawName) {

        String date = LocalDate.now()
                .format(DateTimeFormatter.ISO_DATE);

        String safeName = (rawName == null ? "Veranstaltung" : rawName)
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("Ä", "Ae")
                .replace("Ö", "Oe")
                .replace("Ü", "Ue")
                .replace("ß", "ss")
                .replaceAll("[^a-zA-Z0-9]+", "_");

        return date + "_" + prefix + "_" + safeName + ".pdf";
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
}