package com.kcserver.util;

import com.kcserver.entity.Dokument;

import java.util.List;

public final class PdfPaperFormatUtil {

    private static final double TOLERANCE_MM = 1.0;

    private PdfPaperFormatUtil() {
    }

    public static String format(Dokument dokument) {

        if (dokument == null
                || dokument.getDokumentBreiteMm() == null
                || dokument.getDokumentHoeheMm() == null) {

            return "?";
        }

        double breite =
                dokument.getDokumentBreiteMm().doubleValue();

        double hoehe =
                dokument.getDokumentHoeheMm().doubleValue();

        double kurz =
                Math.min(breite, hoehe);

        double lang =
                Math.max(breite, hoehe);

        return ermittleFormat(kurz, lang);
    }

    private static String ermittleFormat(
            double kurz,
            double lang
    ) {

        List<Papierformat> formate =
                List.of(
                        new Papierformat("A0", 841, 1189),
                        new Papierformat("A1", 594, 841),
                        new Papierformat("A2", 420, 594),
                        new Papierformat("A3", 297, 420),
                        new Papierformat("A4", 210, 297),
                        new Papierformat("A5", 148, 210),
                        new Papierformat("A6", 105, 148),
                        new Papierformat("A7", 74, 105),
                        new Papierformat("A8", 52, 74),
                        new Papierformat("A9", 37, 52),
                        new Papierformat("A10", 26, 37)
                );

        for (Papierformat format : formate) {

            if (passt(
                    kurz,
                    format.kurz()
            )
                    && passt(
                    lang,
                    format.lang()
            )) {

                return format.name();
            }
        }

        return "?";
    }

    private static boolean passt(
            double actual,
            double expected
    ) {

        return Math.abs(
                actual - expected
        ) <= TOLERANCE_MM;
    }

    private record Papierformat(
            String name,
            double kurz,
            double lang
    ) {
    }
}