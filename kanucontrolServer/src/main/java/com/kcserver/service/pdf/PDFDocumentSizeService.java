package com.kcserver.service.pdf;

import com.kcserver.entity.Dokument;
import com.kcserver.enumtype.PdfDocumentDensity;
import com.kcserver.enumtype.ReferenzObjekt;
import org.springframework.stereotype.Service;

@Service
public class PDFDocumentSizeService {

    public PDFDocumentSize determine(
            Dokument dokument
    ) {

        if (dokument.getDokumentBreiteMm() == null
                || dokument.getDokumentHoeheMm() == null) {

            throw new IllegalStateException(
                    "Für Dokument "
                            + dokument.getId()
                            + " ("
                            + dokument.getOriginalDateiname()
                            + ") ist keine Dokumentgröße hinterlegt."
            );
        }

        if (dokument.getReferenzObjekt() == null) {

            throw new IllegalStateException(
                    "Für Dokument "
                            + dokument.getId()
                            + " ("
                            + dokument.getOriginalDateiname()
                            + ") ist kein Referenzobjekt hinterlegt."
            );
        }

        double originalWidth =
                dokument.getDokumentBreiteMm();

        double originalHeight =
                dokument.getDokumentHoeheMm();

        ReferenzObjekt referenz =
                dokument.getReferenzObjekt();

        /*
         * Zielgröße des gewählten DIN-Formats.
         *
         * Die Ausrichtung des Originals wird beibehalten.
         */
        double targetWidth;
        double targetHeight;

        if (originalWidth >= originalHeight) {

            // Querformat
            targetWidth =
                    referenz.getHoeheMm();

            targetHeight =
                    referenz.getBreiteMm();

        } else {

            // Hochformat
            targetWidth =
                    referenz.getBreiteMm();

            targetHeight =
                    referenz.getHoeheMm();
        }

        /*
         * Proportional auf die gewünschte
         * Papiergröße skalieren.
         *
         * Dadurch wird niemals gestaucht oder
         * verzerrt.
         */
        double scaleX =
                targetWidth / originalWidth;

        double scaleY =
                targetHeight / originalHeight;

        double scale =
                Math.min(
                        scaleX,
                        scaleY
                );

        double width =
                originalWidth * scale;

        double height =
                originalHeight * scale;

        float widthPt =
                (float) (
                        width
                                * 72.0
                                / 25.4
                );

        float heightPt =
                (float) (
                        height
                                * 72.0
                                / 25.4
                );

        return new PDFDocumentSize(
                widthPt,
                heightPt,
                PdfDocumentDensity.MEDIUM,
                referenz
        );
    }
}