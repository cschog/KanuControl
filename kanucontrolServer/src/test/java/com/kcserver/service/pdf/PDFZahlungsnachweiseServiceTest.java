package com.kcserver.service.pdf;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.entity.ZahlungsnachweisDokument;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PDFZahlungsnachweiseServiceTest {

    private static final Long VERANSTALTUNG_ID = 123L;

    @Mock
    private VeranstaltungRepository veranstaltungRepository;

    @Mock
    private ZahlungsnachweisRepository zahlungsnachweisRepository;

    private PDFZahlungsnachweiseService service;

    @BeforeEach
    void setUp() {

        service =
                new PDFZahlungsnachweiseService(
                        veranstaltungRepository,
                        zahlungsnachweisRepository,
                        new PDFLayoutService(),
                        new A4LayoutEngine(
                                new PDFLayoutService()
                        ),
                        new PDFDocumentComposer(
                                new PDFLayoutService()
                        )
                );
    }

    @Test
    void erzeugtGesamtesZahlungsnachweisePdf()
            throws Exception {

        Veranstaltung veranstaltung =
                createVeranstaltung();

        Zahlungsnachweis nachweis1 =
                createNachweis(
                        1L,
                        "Beleg 1",
                        "01.08.2026",
                        "25,00"
                );

        Zahlungsnachweis nachweis2 =
                createNachweis(
                        2L,
                        "Beleg 2",
                        "02.08.2026",
                        "50,00"
                );

        nachweis1.addDokument(
                createDokument(
                        101L,
                        1,
                        "beleg1.pdf",
                        createA6Pdf(
                                "BELEG 1"
                        )
                )
        );

        nachweis2.addDokument(
                createDokument(
                        102L,
                        1,
                        "beleg2.pdf",
                        createA6Pdf(
                                "BELEG 2"
                        )
                )
        );

        when(
                veranstaltungRepository.findById(
                        VERANSTALTUNG_ID
                )
        ).thenReturn(
                Optional.of(veranstaltung)
        );

        when(
                zahlungsnachweisRepository
                        .findByVeranstaltungIdOrderByDatumDescIdDesc(
                                VERANSTALTUNG_ID
                        )
        ).thenReturn(
                List.of(
                        nachweis2,
                        nachweis1
                )
        );

        byte[] result =
                service.generate(
                        VERANSTALTUNG_ID
                );

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (
                PDDocument document =
                        Loader.loadPDF(result)
        ) {

            /*
             * Deckblatt + eine A4-Seite
             * mit den beiden A6-Belegen.
             */
            assertEquals(
                    2,
                    document.getNumberOfPages()
            );

            for (PDPage page :
                    document.getPages()) {

                PDRectangle mediaBox =
                        page.getMediaBox();

                assertEquals(
                        PDRectangle.A4.getWidth(),
                        mediaBox.getWidth(),
                        0.01f
                );

                assertEquals(
                        PDRectangle.A4.getHeight(),
                        mediaBox.getHeight(),
                        0.01f
                );
            }
        }
    }

    @Test
    void mehrereA6DokumenteWerdenAufEineA4SeiteGelegt()
            throws Exception {

        Veranstaltung veranstaltung =
                createVeranstaltung();

        Zahlungsnachweis nachweis =
                createNachweis(
                        1L,
                        "Mehrere Belege",
                        "01.08.2026",
                        "100,00"
                );

        nachweis.addDokument(
                createDokument(
                        101L,
                        1,
                        "beleg1.pdf",
                        createA6Pdf(
                                "BELEG 1"
                        )
                )
        );

        nachweis.addDokument(
                createDokument(
                        102L,
                        2,
                        "beleg2.pdf",
                        createA6Pdf(
                                "BELEG 2"
                        )
                )
        );

        nachweis.addDokument(
                createDokument(
                        103L,
                        3,
                        "beleg3.pdf",
                        createA6Pdf(
                                "BELEG 3"
                        )
                )
        );

        nachweis.addDokument(
                createDokument(
                        104L,
                        4,
                        "beleg4.pdf",
                        createA6Pdf(
                                "BELEG 4"
                        )
                )
        );

        when(
                veranstaltungRepository.findById(
                        VERANSTALTUNG_ID
                )
        ).thenReturn(
                Optional.of(veranstaltung)
        );

        when(
                zahlungsnachweisRepository
                        .findByVeranstaltungIdOrderByDatumDescIdDesc(
                                VERANSTALTUNG_ID
                        )
        ).thenReturn(
                List.of(nachweis)
        );

        byte[] result =
                service.generate(
                        VERANSTALTUNG_ID
                );

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (
                PDDocument document =
                        Loader.loadPDF(result)
        ) {

            /*
             * Deckblatt + eine A4-Belegseite.
             */
            assertEquals(
                    2,
                    document.getNumberOfPages()
            );
        }
    }

    @Test
    void fuenfA6DokumenteErzeugenZweiBelegseiten()
            throws Exception {

        Veranstaltung veranstaltung =
                createVeranstaltung();

        Zahlungsnachweis nachweis =
                createNachweis(
                        1L,
                        "Fünf Belege",
                        "01.08.2026",
                        "125,00"
                );

        for (int i = 1; i <= 5; i++) {

            nachweis.addDokument(
                    createDokument(
                            100L + i,
                            i,
                            "beleg" + i + ".pdf",
                            createA6Pdf(
                                    "BELEG " + i
                            )
                    )
            );
        }

        when(
                veranstaltungRepository.findById(
                        VERANSTALTUNG_ID
                )
        ).thenReturn(
                Optional.of(veranstaltung)
        );

        when(
                zahlungsnachweisRepository
                        .findByVeranstaltungIdOrderByDatumDescIdDesc(
                                VERANSTALTUNG_ID
                        )
        ).thenReturn(
                List.of(nachweis)
        );

        byte[] result =
                service.generate(
                        VERANSTALTUNG_ID
                );

        assertNotNull(result);

        try (
                PDDocument document =
                        Loader.loadPDF(result)
        ) {

            /*
             * Deckblatt
             *
             * +
             *
             * zwei A4-Belegseiten.
             */
            assertEquals(
                    3,
                    document.getNumberOfPages()
            );
        }
    }

    @Test
    void gedrehtesDokumentWirdVerarbeitet()
            throws Exception {

        Veranstaltung veranstaltung =
                createVeranstaltung();

        Zahlungsnachweis nachweis =
                createNachweis(
                        1L,
                        "Gedrehter Beleg",
                        "01.08.2026",
                        "75,00"
                );

        /*
         * Hochformat mit 100 x 250 mm.
         *
         * Die Layout-Engine sollte dieses Dokument
         * auf der A4-Seite drehen können.
         */
        byte[] pdf =
                createPdf(
                        100f * 72f / 25.4f,
                        250f * 72f / 25.4f,
                        "GEDREHTER BELEG"
                );

        nachweis.addDokument(
                createDokument(
                        101L,
                        1,
                        "gedreht.pdf",
                        pdf
                )
        );

        when(
                veranstaltungRepository.findById(
                        VERANSTALTUNG_ID
                )
        ).thenReturn(
                Optional.of(veranstaltung)
        );

        when(
                zahlungsnachweisRepository
                        .findByVeranstaltungIdOrderByDatumDescIdDesc(
                                VERANSTALTUNG_ID
                        )
        ).thenReturn(
                List.of(nachweis)
        );

        byte[] result =
                service.generate(
                        VERANSTALTUNG_ID
                );

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (
                PDDocument document =
                        Loader.loadPDF(result)
        ) {

            assertEquals(
                    2,
                    document.getNumberOfPages()
            );
        }
    }

    @Test
    void nachweisOhneDokumentWirdNichtBeruecksichtigt()
            throws Exception {

        Veranstaltung veranstaltung =
                createVeranstaltung();

        Zahlungsnachweis ohneDokument =
                createNachweis(
                        1L,
                        "Ohne Dokument",
                        "01.08.2026",
                        "10,00"
                );

        when(
                veranstaltungRepository.findById(
                        VERANSTALTUNG_ID
                )
        ).thenReturn(
                Optional.of(veranstaltung)
        );

        when(
                zahlungsnachweisRepository
                        .findByVeranstaltungIdOrderByDatumDescIdDesc(
                                VERANSTALTUNG_ID
                        )
        ).thenReturn(
                List.of(
                        ohneDokument
                )
        );

        byte[] result =
                service.generate(
                        VERANSTALTUNG_ID
                );

        assertNotNull(result);

        try (
                PDDocument document =
                        Loader.loadPDF(result)
        ) {

            /*
             * Es gibt trotzdem ein Deckblatt.
             */
            assertEquals(
                    1,
                    document.getNumberOfPages()
            );
        }
    }

    /*
     * =========================================================
     * TESTDATEN
     * =========================================================
     */

    private Veranstaltung createVeranstaltung() {

        Veranstaltung veranstaltung =
                new Veranstaltung();

        veranstaltung.setName(
                "Testveranstaltung"
        );

        veranstaltung.setBeginnDatum(
                LocalDate.of(
                        2026,
                        8,
                        1
                )
        );

        veranstaltung.setEndeDatum(
                LocalDate.of(
                        2026,
                        8,
                        7
                )
        );

        veranstaltung.setOrt(
                "Testort"
        );

        return veranstaltung;
    }

    private Zahlungsnachweis createNachweis(
            Long id,
            String bemerkung,
            String datum,
            String betrag
    ) {

        Zahlungsnachweis nachweis =
                new Zahlungsnachweis();

        nachweis.setId(id);

        nachweis.setDatum(
                LocalDate.of(
                        2026,
                        8,
                        Integer.parseInt(
                                datum.substring(
                                        0,
                                        2
                                )
                        )
                )
        );

        nachweis.setBetrag(
                new BigDecimal(
                        betrag.replace(
                                ",",
                                "."
                        )
                )
        );

        nachweis.setBemerkung(
                bemerkung
        );

        return nachweis;
    }

    private ZahlungsnachweisDokument createDokument(
            Long id,
            int reihenfolge,
            String dateiname,
            byte[] inhalt
    ) {

        ZahlungsnachweisDokument dokument =
                new ZahlungsnachweisDokument();

        dokument.setId(id);

        dokument.setReihenfolge(
                reihenfolge
        );

        dokument.setTitel(
                dateiname
        );

        dokument.setOriginalDateiname(
                dateiname
        );

        dokument.setMimeType(
                "application/pdf"
        );

        dokument.setDateigroesse(
                (long) inhalt.length
        );

        dokument.setInhalt(
                inhalt
        );

        return dokument;
    }

    /*
     * =========================================================
     * TEST-PDF
     * =========================================================
     */

    private byte[] createA6Pdf(
            String text
    ) throws Exception {

        return createPdf(
                PDRectangle.A6.getWidth(),
                PDRectangle.A6.getHeight(),
                text
        );
    }

    private byte[] createPdf(
            float width,
            float height,
            String text
    ) throws Exception {

        try (
                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            PDPage page =
                    new PDPage(
                            new PDRectangle(
                                    width,
                                    height
                            )
                    );

            document.addPage(page);

            try (
                    PDPageContentStream content =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                PDType1Font font =
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA
                        );

                content.beginText();

                content.setFont(
                        font,
                        14
                );

                content.newLineAtOffset(
                        20,
                        height - 30
                );

                content.showText(
                        text
                );

                content.endText();
            }

            document.save(out);

            return out.toByteArray();
        }
    }
}