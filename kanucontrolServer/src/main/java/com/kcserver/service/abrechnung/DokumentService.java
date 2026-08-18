package com.kcserver.service.abrechnung;

import com.kcserver.dto.abrechnung.DokumentDTO;
import com.kcserver.entity.AbrechnungBeleg;
import com.kcserver.entity.Dokument;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.mapper.DokumentMapper;
import com.kcserver.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.repository.abrechnung.DokumentRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.kcserver.enumtype.ReferenzObjekt;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DokumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private record DocumentDimensions(
            double widthMm,
            double heightMm
    ) {
    }

    private final DokumentRepository dokumentRepository;
    private final AbrechnungBelegRepository abrechnungBelegRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final DokumentMapper dokumentMapper;

    /**
     * Alle Dokumente eines Belegs.
     */
    @Transactional(readOnly = true)
    public List<DokumentDTO> findAllByBeleg(Long belegId) {

        getBeleg(belegId);

        return dokumentMapper.toDto(
                dokumentRepository
                        .findByBelegIdOrderByReihenfolgeAsc(belegId)
        );
    }

    /**
     * Alle Dokumente eines Zahlungsnachweises.
     */
    @Transactional(readOnly = true)
    public List<DokumentDTO> findAllByZahlungsnachweis(
            Long veranstaltungId,
            Long zahlungsnachweisId
    ) {

        getZahlungsnachweis(
                veranstaltungId,
                zahlungsnachweisId
        );

        return dokumentMapper.toDto(
                dokumentRepository
                        .findByZahlungsnachweisIdOrderByReihenfolgeAsc(
                                zahlungsnachweisId
                        )
        );
    }

    /**
     * Dokument zu einem Beleg hochladen.
     */
    public DokumentDTO uploadForBeleg(
            Long belegId,
            MultipartFile file,
            ReferenzObjekt referenzObjekt
    ) {

        AbrechnungBeleg beleg = getBeleg(belegId);

        int reihenfolge =
                getNextReihenfolgeForBeleg(belegId);

        Dokument dokument =
                createDokument(
                        file,
                        reihenfolge,
                        referenzObjekt
                );

        beleg.addDokument(dokument);

        abrechnungBelegRepository.flush();

        return dokumentMapper.toDto(dokument);
    }

    /**
     * Dokument zu einem Zahlungsnachweis hochladen.
     */
    public DokumentDTO uploadForZahlungsnachweis(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            MultipartFile file,
            ReferenzObjekt referenzObjekt
    ) {

        Zahlungsnachweis zahlungsnachweis =
                getZahlungsnachweis(
                        veranstaltungId,
                        zahlungsnachweisId
                );

        int reihenfolge =
                getNextReihenfolgeForZahlungsnachweis(
                        zahlungsnachweisId
                );

        Dokument dokument =
                createDokument(
                        file,
                        reihenfolge,
                        referenzObjekt
                );

        zahlungsnachweis.addDokument(dokument);

        zahlungsnachweisRepository.flush();

        return dokumentMapper.toDto(dokument);
    }

    /**
     * Dokument eines Belegs laden.
     */
    @Transactional(readOnly = true)
    public Dokument getForBeleg(
            Long belegId,
            Long dokumentId
    ) {

        getBeleg(belegId);

        return dokumentRepository
                .findById(dokumentId)
                .filter(dokument ->
                        dokument.getBeleg() != null
                                && dokument.getBeleg()
                                .getId()
                                .equals(belegId)
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dokument nicht gefunden."
                        )
                );
    }

    /**
     * Dokument eines Zahlungsnachweises laden.
     */
    @Transactional(readOnly = true)
    public Dokument getForZahlungsnachweis(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            Long dokumentId
    ) {

        Zahlungsnachweis zahlungsnachweis =
                getZahlungsnachweis(
                        veranstaltungId,
                        zahlungsnachweisId
                );

        return dokumentRepository
                .findById(dokumentId)
                .filter(dokument ->
                        dokument.getZahlungsnachweis() != null
                                && dokument.getZahlungsnachweis()
                                .getId()
                                .equals(zahlungsnachweis.getId())
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dokument nicht gefunden."
                        )
                );
    }

    /**
     * Dokument eines Belegs löschen.
     */
    public void deleteForBeleg(
            Long belegId,
            Long dokumentId
    ) {

        AbrechnungBeleg beleg = getBeleg(belegId);

        Dokument dokument =
                getForBeleg(
                        belegId,
                        dokumentId
                );

        beleg.removeDokument(dokument);

        abrechnungBelegRepository.flush();
    }

    /**
     * Dokument eines Zahlungsnachweises löschen.
     */
    public void deleteForZahlungsnachweis(
            Long veranstaltungId,
            Long zahlungsnachweisId,
            Long dokumentId
    ) {

        Zahlungsnachweis zahlungsnachweis =
                getZahlungsnachweis(
                        veranstaltungId,
                        zahlungsnachweisId
                );

        Dokument dokument =
                getForZahlungsnachweis(
                        veranstaltungId,
                        zahlungsnachweisId,
                        dokumentId
                );

        zahlungsnachweis.removeDokument(dokument);

        zahlungsnachweisRepository.flush();
    }

    private int getNextReihenfolgeForBeleg(Long belegId) {

        Dokument letztes =
                dokumentRepository
                        .findTopByBelegIdOrderByReihenfolgeDesc(
                                belegId
                        );

        return letztes == null
                ? 1
                : letztes.getReihenfolge() + 1;
    }

    private int getNextReihenfolgeForZahlungsnachweis(
            Long zahlungsnachweisId
    ) {

        Dokument letztes =
                dokumentRepository
                        .findTopByZahlungsnachweisIdOrderByReihenfolgeDesc(
                                zahlungsnachweisId
                        );

        return letztes == null
                ? 1
                : letztes.getReihenfolge() + 1;
    }

    private AbrechnungBeleg getBeleg(Long belegId) {

        return abrechnungBelegRepository
                .findById(belegId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Beleg nicht gefunden."
                        )
                );
    }

    private Zahlungsnachweis getZahlungsnachweis(
            Long veranstaltungId,
            Long zahlungsnachweisId
    ) {

        return zahlungsnachweisRepository
                .findByIdAndVeranstaltungId(
                        zahlungsnachweisId,
                        veranstaltungId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Zahlungsnachweis nicht gefunden."
                        )
                );
    }

    /**
     * Gemeinsame Erstellung eines Dokuments.
     */
    private Dokument createDokument(
            MultipartFile file,
            int reihenfolge,
            ReferenzObjekt referenzObjekt
    ) {

        validateFile(file);

        if (referenzObjekt == null) {
            referenzObjekt = ReferenzObjekt.DIN_A6;
        }

        String mimeType = file.getContentType();

        if (mimeType != null) {
            mimeType = mimeType.toLowerCase();
        }

        Dokument dokument = new Dokument();

        dokument.setReihenfolge(reihenfolge);

        /*
         * Dokumentformat
         *
         * Fotos werden bei KanuControl grundsätzlich
         * im Querformat fotografiert.
         *
         * Bei PDFs wird die Ausrichtung aus der
         * ersten PDF-Seite übernommen.
         */
        dokument.setReferenzObjekt(referenzObjekt);

        DocumentDimensions dimensions =
                determineDocumentDimensions(
                        file,
                        referenzObjekt,
                        mimeType
                );

        dokument.setDokumentBreiteMm(
                dimensions.widthMm()
        );

        dokument.setDokumentHoeheMm(
                dimensions.heightMm()
        );

        String dateiname = file.getOriginalFilename();

        if (dateiname == null || dateiname.isBlank()) {
            dateiname = "Dokument";
        }

        dokument.setTitel(dateiname);
        dokument.setOriginalDateiname(dateiname);
        dokument.setMimeType(mimeType);
        dokument.setDateigroesse(file.getSize());

        try {
            dokument.setInhalt(file.getBytes());
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Datei konnte nicht gelesen werden.",
                    ex
            );
        }

        return dokument;
    }

    private DocumentDimensions determineDocumentDimensions(
            MultipartFile file,
            ReferenzObjekt referenzObjekt,
            String mimeType
    ) {

        /*
         * =========================================================
         * FOTO / BILD
         * =========================================================
         *
         * Fotos von Belegen werden bei KanuControl
         * grundsätzlich im Querformat fotografiert.
         *
         * Deshalb:
         *
         * DIN A4 -> 297 x 210 mm
         * DIN A5 -> 210 x 148 mm
         * DIN A6 -> 148 x 105 mm
         * DIN A7 -> 105 x 74 mm
         */
        if (mimeType != null
                && mimeType.startsWith("image/")) {

            return new DocumentDimensions(
                    referenzObjekt.getHoeheMm(),
                    referenzObjekt.getBreiteMm()
            );
        }

        /*
         * =========================================================
         * PDF
         * =========================================================
         *
         * Die physische Größe kommt aus dem
         * ausgewählten Referenzobjekt.
         *
         * Die Ausrichtung wird aus der ersten PDF-Seite
         * ermittelt.
         */
        if (MediaType.APPLICATION_PDF_VALUE.equals(mimeType)) {

            try (
                    PDDocument pdf =
                            Loader.loadPDF(
                                    file.getBytes()
                            )
            ) {

                if (pdf.getNumberOfPages() == 0) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Die PDF-Datei enthält keine Seite."
                    );
                }

                PDPage page =
                        pdf.getPage(0);

                PDRectangle box =
                        page.getMediaBox();

                float width =
                        box.getWidth();

                float height =
                        box.getHeight();

                int rotation =
                        page.getRotation();

                /*
                 * Bei 90° bzw. 270° ist die sichtbare
                 * Ausrichtung gegenüber der MediaBox gedreht.
                 */
                boolean landscape =
                        rotation == 90
                                || rotation == 270
                                ? height > width
                                : width > height;

                if (landscape) {

                    return new DocumentDimensions(
                            referenzObjekt.getHoeheMm(),
                            referenzObjekt.getBreiteMm()
                    );

                } else {

                    return new DocumentDimensions(
                            referenzObjekt.getBreiteMm(),
                            referenzObjekt.getHoeheMm()
                    );
                }

            } catch (IOException e) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "PDF-Datei konnte nicht gelesen werden.",
                        e
                );
            }
        }

        /*
         * Sollte eigentlich durch validateFile()
         * ausgeschlossen sein.
         */
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nicht unterstützter Dokumenttyp."
        );
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Keine Datei ausgewählt."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Datei ist größer als 10 MB."
            );
        }

        String mimeType = file.getContentType();

        if (mimeType != null) {
            mimeType = mimeType.toLowerCase();
        }

        boolean erlaubt =
                MediaType.APPLICATION_PDF_VALUE.equals(mimeType)
                        || (mimeType != null && mimeType.startsWith("image/"));

        if (!erlaubt) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Es dürfen nur Bilder oder PDF-Dateien hochgeladen werden."
            );
        }
    }
    @Transactional(readOnly = true)
    public Dokument getById(Long dokumentId) {

        return dokumentRepository
                .findById(dokumentId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dokument nicht gefunden."
                        )
                );
    }

    public void deleteById(Long dokumentId) {

        Dokument dokument =
                getById(dokumentId);

        if (dokument.getBeleg() != null) {

            AbrechnungBeleg beleg =
                    dokument.getBeleg();

            beleg.removeDokument(dokument);

            abrechnungBelegRepository.flush();

            return;
        }

        if (dokument.getZahlungsnachweis() != null) {

            Zahlungsnachweis zahlungsnachweis =
                    dokument.getZahlungsnachweis();

            zahlungsnachweis.removeDokument(dokument);

            zahlungsnachweisRepository.flush();

            return;
        }

        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Dokument hat keinen Besitzer."
        );
    }
}