package com.kcserver.util;

import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.PdfDokumentTyp;

import java.time.LocalDate;

public class PdfFilenameUtil {

    private PdfFilenameUtil() {
    }

    /* =========================================================
       ENTITY
       ========================================================= */

    public static String build(
            LocalDate druckdatum,
            PdfDokumentTyp dokumentTyp,
            Veranstaltung veranstaltung
    ) {

        String typ =
                veranstaltung.getTyp() != null
                        ? veranstaltung.getTyp().name()
                        : "";

        return buildInternal(
                druckdatum,
                dokumentTyp,
                typ,
                veranstaltung.getName()
        );
    }

    /* =========================================================
       DTO
       ========================================================= */

    public static String build(
            LocalDate druckdatum,
            PdfDokumentTyp dokumentTyp,
            VeranstaltungDetailDTO veranstaltung
    ) {

        String typ =
                veranstaltung.getTyp() != null
                        ? veranstaltung.getTyp().name()
                        : "";

        return buildInternal(
                druckdatum,
                dokumentTyp,
                typ,
                veranstaltung.getName()
        );
    }

    /* =========================================================
       INTERNAL
       ========================================================= */

    private static String buildInternal(
            LocalDate druckdatum,
            PdfDokumentTyp dokumentTyp,
            String typ,
            String veranstaltungsname
    ) {

        String name = sanitize(veranstaltungsname);

        return String.format(
                        "%s %s %s %s.pdf",
                        druckdatum,
                        dokumentTyp.getLabel(),
                        typ,
                        name
                )
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String sanitize(String input) {

        if (input == null || input.isBlank()) {
            return "Veranstaltung";
        }

        return input
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("Ä", "Ae")
                .replace("Ö", "Oe")
                .replace("Ü", "Ue")
                .replace("ß", "ss")
                .replaceAll("[\\\\/:*?\"<>|]", "")
                .trim();
    }
    public static String buildReisekosten(
            LocalDate datum,
            Veranstaltung veranstaltung,
            String nachname,
            String vorname
    ) {

        return sanitize(
                datum +
                        " Fahrkostenabrechnung " +
                        veranstaltung.getName() +
                        " " +
                        nachname +
                        "_" +
                        vorname
        ) + ".pdf";
    }
}