package com.kcserver.controller;

import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.service.TeilnehmerService;
import com.kcserver.service.VeranstaltungService;
import com.kcserver.service.pdf.PDFTeilnehmerlisteService;
import com.kcserver.service.pdf.PDFErhebungsbogenService;
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
@RequestMapping("/api/veranstaltung")
@RequiredArgsConstructor
public class VeranstaltungPdfController {

    private final PDFTeilnehmerlisteService pdfService;
    private final VeranstaltungService veranstaltungService;
    private final TeilnehmerService teilnehmerService;
    private final PDFErhebungsbogenService erhebungsbogenService;

    /* =========================================================
       PDF DOWNLOAD
       ========================================================= */

    @GetMapping("/{id}/teilnehmer/pdf")
    public ResponseEntity<byte[]> downloadTeilnehmerPdf(@PathVariable Long id) throws Exception {

        /* 1️⃣ Veranstaltung laden */
        VeranstaltungDetailDTO veranstaltung =
                veranstaltungService.getById(id);

        /* 2️⃣ Teilnehmer laden */
        List<TeilnehmerDetailDTO> teilnehmer =
                teilnehmerService.getTeilnehmerForVeranstaltung(id);

        /* 3️⃣ PDF generieren */
        byte[] pdf = pdfService.generate(veranstaltung, teilnehmer);

        /* 4️⃣ Dateiname */
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        String rawName = veranstaltung.getName() == null ? "Veranstaltung" : veranstaltung.getName();

        /* nur Dateisystem-sichere Zeichen */
        String cleanName = rawName
                .replace("ä","ae").replace("ö","oe").replace("ü","ue")
                .replace("Ä","Ae").replace("Ö","Oe").replace("Ü","Ue")
                .replace("ß","ss")
                .replaceAll("[^a-zA-Z0-9]", "");

        String filename = date + "_TN_" + cleanName + ".pdf";

        /* ⭐ wichtig für Browser (Chrome, Safari, Edge, Acrobat) */
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    @GetMapping("/{id}/erhebungsbogen/pdf")
    public ResponseEntity<byte[]> downloadErhebungsbogen(@PathVariable Long id) throws Exception {

        VeranstaltungDetailDTO v = veranstaltungService.getById(id);
        List<TeilnehmerDetailDTO> tn = teilnehmerService.getTeilnehmerForVeranstaltung(id);

        byte[] pdf = erhebungsbogenService.generate(v, tn);

        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

        String rawName = v.getName() == null ? "Veranstaltung" : v.getName();

        String cleanName = rawName
                .replace("ä","ae").replace("ö","oe").replace("ü","ue")
                .replace("Ä","Ae").replace("Ö","Oe").replace("Ü","Ue")
                .replace("ß","ss")
                .replaceAll("[^a-zA-Z0-9]", "");

        String filename = date + "_EB_" + cleanName + ".pdf";

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename +
                                "\"; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}