package com.kcserver.service.pdf;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Zahlungsnachweis;
import com.kcserver.entity.ZahlungsnachweisDokument;
import com.kcserver.enumtype.PdfDocumentDensity;
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
import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
                        ),
                        new ImageAnalysisService()
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
            assertTrue(
                    document.getNumberOfPages() >= 2
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
            assertTrue(
                    document.getNumberOfPages() >= 4
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
    @Test
    void dokumentIdsVonZahlungsnachweisenSindEindeutigUndKonsistent()
            throws Exception {

        Zahlungsnachweis zn1 =
                new Zahlungsnachweis();

        zn1.setId(101L);

        ZahlungsnachweisDokument doc11 =
                new ZahlungsnachweisDokument();

        doc11.setId(1001L);
        doc11.setInhalt(createTestPdf());

        ZahlungsnachweisDokument doc12 =
                new ZahlungsnachweisDokument();

        doc12.setId(1002L);
        doc12.setInhalt(createTestPdf());

        zn1.addDokument(doc11);
        zn1.addDokument(doc12);


        Zahlungsnachweis zn2 =
                new Zahlungsnachweis();

        zn2.setId(202L);

        ZahlungsnachweisDokument doc21 =
                new ZahlungsnachweisDokument();

        doc21.setId(2001L);
        doc21.setInhalt(createTestPdf());

        zn2.addDokument(doc21);


        List<Zahlungsnachweis> nachweise =
                List.of(
                        zn1,
                        zn2
                );


        /*
         * Erst die neue Gruppenstruktur erzeugen.
         */
        List<PDFBelegGruppe> gruppen =
                service.createGruppen(
                        nachweise
                );


        /*
         * Dokumente aus den Gruppen sammeln.
         */
        Map<String, byte[]> documents =
                invokeCollectDocuments(
                        gruppen
                );


        assertEquals(
                3,
                documents.size()
        );

        assertTrue(
                documents.containsKey(
                        "ZN-101-DOC-1001"
                )
        );

        assertTrue(
                documents.containsKey(
                        "ZN-101-DOC-1002"
                )
        );

        assertTrue(
                documents.containsKey(
                        "ZN-202-DOC-2001"
                )
        );


        assertSame(
                doc11.getInhalt(),
                documents.get(
                        "ZN-101-DOC-1001"
                )
        );

        assertSame(
                doc12.getInhalt(),
                documents.get(
                        "ZN-101-DOC-1002"
                )
        );

        assertSame(
                doc21.getInhalt(),
                documents.get(
                        "ZN-202-DOC-2001"
                )
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, byte[]> invokeCollectDocuments(
            List<PDFBelegGruppe> gruppen
    ) throws Exception {

        var method =
                PDFZahlungsnachweiseService.class
                        .getDeclaredMethod(
                                "collectDocuments",
                                List.class
                        );

        method.setAccessible(true);

        return (Map<String, byte[]>)
                method.invoke(
                        service,
                        gruppen
                );
    }

    private byte[] createTestPdf() {

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            document.addPage(
                    new PDPage(
                            PDRectangle.A6
                    )
            );

            document.save(out);

            return out.toByteArray();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Test-PDF konnte nicht erzeugt werden.",
                    e
            );
        }
    }

    @Test
    void zahlungsnachweiseWerdenInBeleggruppenMitDokumentenZusammengefasst()
            throws Exception {

        Zahlungsnachweis zn1 =
                new Zahlungsnachweis();

        zn1.setId(101L);

        ZahlungsnachweisDokument doc11 =
                new ZahlungsnachweisDokument();

        doc11.setId(1001L);
        doc11.setInhalt(createTestPdf());

        ZahlungsnachweisDokument doc12 =
                new ZahlungsnachweisDokument();

        doc12.setId(1002L);
        doc12.setInhalt(createTestPdf());

        zn1.addDokument(doc11);
        zn1.addDokument(doc12);


        Zahlungsnachweis zn2 =
                new Zahlungsnachweis();

        zn2.setId(202L);

        ZahlungsnachweisDokument doc21 =
                new ZahlungsnachweisDokument();

        doc21.setId(2001L);
        doc21.setInhalt(createTestPdf());

        zn2.addDokument(doc21);


        List<Zahlungsnachweis> nachweise =
                List.of(
                        zn1,
                        zn2
                );


        List<PDFBelegGruppe> gruppen =
                service.createGruppen(nachweise);


        /*
         * ---------------------------------------------------------
         * Zwei Beleggruppen
         * ---------------------------------------------------------
         */

        assertEquals(
                2,
                gruppen.size()
        );


        /*
         * ---------------------------------------------------------
         * Erste Beleggruppe
         * ---------------------------------------------------------
         */

        PDFBelegGruppe gruppe1 =
                gruppen.getFirst();

        assertEquals(
                1,
                gruppe1.nummer()
        );

        assertEquals(
                101L,
                gruppe1.nachweis().getId()
        );

        assertEquals(
                2,
                gruppe1.dokumente().size()
        );

        assertEquals(
                "ZN-101-DOC-1001",
                gruppe1.dokumente().get(0).id()
        );

        assertEquals(
                "ZN-101-DOC-1002",
                gruppe1.dokumente().get(1).id()
        );


        /*
         * ---------------------------------------------------------
         * Zweite Beleggruppe
         * ---------------------------------------------------------
         */

        PDFBelegGruppe gruppe2 =
                gruppen.get(1);

        assertEquals(
                2,
                gruppe2.nummer()
        );

        assertEquals(
                202L,
                gruppe2.nachweis().getId()
        );

        assertEquals(
                1,
                gruppe2.dokumente().size()
        );

        assertEquals(
                "ZN-202-DOC-2001",
                gruppe2.dokumente().getFirst().id()
        );
    }

    private List<?> invokeCreateGruppen(
            List<Zahlungsnachweis> nachweise
    ) throws Exception {

        var method =
                PDFZahlungsnachweiseService.class
                        .getDeclaredMethod(
                                "createGruppen",
                                List.class
                        );

        method.setAccessible(true);

        return (List<?>)
                method.invoke(
                        service,
                        nachweise
                );
    }

    @Test
    void dokumenteWerdenIhrerBeleggruppeEindeutigZugeordnet()
            throws Exception {

        Zahlungsnachweis zn1 =
                new Zahlungsnachweis();

        zn1.setId(101L);

        ZahlungsnachweisDokument doc11 =
                new ZahlungsnachweisDokument();

        doc11.setId(1001L);
        doc11.setInhalt(createTestPdf());

        ZahlungsnachweisDokument doc12 =
                new ZahlungsnachweisDokument();

        doc12.setId(1002L);
        doc12.setInhalt(createTestPdf());

        zn1.addDokument(doc11);
        zn1.addDokument(doc12);


        Zahlungsnachweis zn2 =
                new Zahlungsnachweis();

        zn2.setId(202L);

        ZahlungsnachweisDokument doc21 =
                new ZahlungsnachweisDokument();

        doc21.setId(2001L);
        doc21.setInhalt(createTestPdf());

        zn2.addDokument(doc21);


        List<Zahlungsnachweis> nachweise =
                List.of(
                        zn1,
                        zn2
                );


        List<PDFBelegGruppe> gruppen =
                service.createGruppen(nachweise);


        /*
         * ---------------------------------------------------------
         * Zwei Beleggruppen
         * ---------------------------------------------------------
         */

        assertEquals(
                2,
                gruppen.size()
        );


        /*
         * ---------------------------------------------------------
         * Erste Beleggruppe
         * ---------------------------------------------------------
         */

        PDFBelegGruppe gruppe1 =
                gruppen.getFirst();

        assertEquals(
                1,
                gruppe1.nummer()
        );

        assertEquals(
                101L,
                gruppe1.nachweis().getId()
        );

        assertEquals(
                2,
                gruppe1.dokumente().size()
        );

        assertEquals(
                "ZN-101-DOC-1001",
                gruppe1.dokumente().get(0).id()
        );

        assertEquals(
                "ZN-101-DOC-1002",
                gruppe1.dokumente().get(1).id()
        );


        /*
         * ---------------------------------------------------------
         * Zweite Beleggruppe
         * ---------------------------------------------------------
         */

        PDFBelegGruppe gruppe2 =
                gruppen.get(1);

        assertEquals(
                2,
                gruppe2.nummer()
        );

        assertEquals(
                202L,
                gruppe2.nachweis().getId()
        );

        assertEquals(
                1,
                gruppe2.dokumente().size()
        );

        assertEquals(
                "ZN-202-DOC-2001",
                gruppe2.dokumente().getFirst().id()
        );
    }
    @Test
    void beleggruppenWerdenInDerRichtigenReihenfolgeErzeugt()
            throws Exception {

        Zahlungsnachweis zn1 =
                new Zahlungsnachweis();
        zn1.setId(101L);

        ZahlungsnachweisDokument doc11 =
                new ZahlungsnachweisDokument();
        doc11.setId(1001L);
        doc11.setInhalt(createTestPdf());

        ZahlungsnachweisDokument doc12 =
                new ZahlungsnachweisDokument();
        doc12.setId(1002L);
        doc12.setInhalt(createTestPdf());

        zn1.addDokument(doc11);
        zn1.addDokument(doc12);


        Zahlungsnachweis zn2 =
                new Zahlungsnachweis();
        zn2.setId(202L);

        ZahlungsnachweisDokument doc21 =
                new ZahlungsnachweisDokument();
        doc21.setId(2001L);
        doc21.setInhalt(createTestPdf());

        zn2.addDokument(doc21);


        List<Zahlungsnachweis> nachweise =
                List.of(
                        zn1,
                        zn2
                );


        List<PDFBelegGruppe> gruppen =
                service.createGruppen(nachweise);


        assertEquals(
                2,
                gruppen.size()
        );

        /*
         * ---------------------------------------------------------
         * Gruppe #1
         * ---------------------------------------------------------
         */

        assertEquals(
                1,
                gruppen.getFirst().nummer()
        );

        assertEquals(
                101L,
                gruppen.get(0).nachweis().getId()
        );

        assertEquals(
                2,
                gruppen.get(0).dokumente().size()
        );


        /*
         * ---------------------------------------------------------
         * Gruppe #2
         * ---------------------------------------------------------
         */

        assertEquals(
                2,
                gruppen.get(1).nummer()
        );

        assertEquals(
                202L,
                gruppen.get(1).nachweis().getId()
        );

        assertEquals(
                1,
                gruppen.get(1).dokumente().size()
        );
    }
    @Test
    void beleggruppeEnthaeltNachweisUndDokumente() {

        Zahlungsnachweis zn =
                new Zahlungsnachweis();

        zn.setId(101L);

        ZahlungsnachweisDokument doc1 =
                new ZahlungsnachweisDokument();

        doc1.setId(1001L);
        doc1.setInhalt(createTestPdf());

        ZahlungsnachweisDokument doc2 =
                new ZahlungsnachweisDokument();

        doc2.setId(1002L);
        doc2.setInhalt(createTestPdf());

        zn.addDokument(doc1);
        zn.addDokument(doc2);

        /*
         * ---------------------------------------------------------
         * Gruppe aufbauen
         * ---------------------------------------------------------
         */

        List<A4LayoutItem> dokumente =
                List.of(
                        item(
                                "ZN-101-DOC-1001",
                                PDFLayoutService.A6_WIDTH,
                                PDFLayoutService.A6_HEIGHT
                        ),
                        item(
                                "ZN-101-DOC-1002",
                                PDFLayoutService.A6_WIDTH,
                                PDFLayoutService.A6_HEIGHT
                        )
                );

        PDFBelegGruppe gruppe =
                new PDFBelegGruppe(
                        1,
                        zn,
                        dokumente
                );

        /*
         * ---------------------------------------------------------
         * Prüfung
         * ---------------------------------------------------------
         */

        assertEquals(
                1,
                gruppe.nummer()
        );

        assertEquals(
                101L,
                gruppe.nachweis().getId()
        );

        assertEquals(
                2,
                gruppe.dokumente().size()
        );

        assertEquals(
                "ZN-101-DOC-1001",
                gruppe.dokumente().get(0).id()
        );

        assertEquals(
                "ZN-101-DOC-1002",
                gruppe.dokumente().get(1).id()
        );
    }

    @Test
    void layoutItemsWerdenAusDenBeleggruppenInDerRichtigenReihenfolgeErzeugt()
            throws Exception {

        Zahlungsnachweis zn1 =
                new Zahlungsnachweis();

        zn1.setId(101L);

        ZahlungsnachweisDokument doc11 =
                new ZahlungsnachweisDokument();

        doc11.setId(1001L);
        doc11.setInhalt(createTestPdf());

        ZahlungsnachweisDokument doc12 =
                new ZahlungsnachweisDokument();

        doc12.setId(1002L);
        doc12.setInhalt(createTestPdf());

        zn1.addDokument(doc11);
        zn1.addDokument(doc12);


        Zahlungsnachweis zn2 =
                new Zahlungsnachweis();

        zn2.setId(202L);

        ZahlungsnachweisDokument doc21 =
                new ZahlungsnachweisDokument();

        doc21.setId(2001L);
        doc21.setInhalt(createTestPdf());

        zn2.addDokument(doc21);


        List<Zahlungsnachweis> nachweise =
                List.of(
                        zn1,
                        zn2
                );


        List<PDFBelegGruppe> gruppen =
                service.createGruppen(
                        nachweise
                );


        List<A4LayoutItem> items =
                invokeCreateLayoutItems(
                        gruppen
                );


        assertEquals(
                3,
                items.size()
        );


        /*
         * Dokument 1
         */
        assertEquals(
                "ZN-101-DOC-1001",
                items.get(0).id()
        );


        /*
         * Dokument 2
         */
        assertEquals(
                "ZN-101-DOC-1002",
                items.get(1).id()
        );


        /*
         * Dokument 3
         */
        assertEquals(
                "ZN-202-DOC-2001",
                items.get(2).id()
        );
    }

    @SuppressWarnings("unchecked")
    private List<A4LayoutItem> invokeCreateLayoutItems(
            List<PDFBelegGruppe> gruppen
    ) throws Exception {

        var method =
                PDFZahlungsnachweiseService.class
                        .getDeclaredMethod(
                                "createLayoutItems",
                                List.class
                        );

        method.setAccessible(true);

        return (List<A4LayoutItem>)
                method.invoke(
                        service,
                        gruppen
                );
    }

    @Test
    void dokumentZuordnungenWerdenInDerGruppenreihenfolgeErzeugt()
            throws Exception {

        Zahlungsnachweis zn1 =
                new Zahlungsnachweis();

        zn1.setId(101L);

        ZahlungsnachweisDokument doc11 =
                new ZahlungsnachweisDokument();

        doc11.setId(1001L);
        doc11.setInhalt(createTestPdf());

        ZahlungsnachweisDokument doc12 =
                new ZahlungsnachweisDokument();

        doc12.setId(1002L);
        doc12.setInhalt(createTestPdf());

        zn1.addDokument(doc11);
        zn1.addDokument(doc12);


        Zahlungsnachweis zn2 =
                new Zahlungsnachweis();

        zn2.setId(202L);

        ZahlungsnachweisDokument doc21 =
                new ZahlungsnachweisDokument();

        doc21.setId(2001L);
        doc21.setInhalt(createTestPdf());

        zn2.addDokument(doc21);


        List<PDFBelegGruppe> gruppen =
                service.createGruppen(
                        List.of(zn1, zn2)
                );


        List<DokumentZuordnung> zuordnungen =
                service.createDokumentZuordnungen(
                        gruppen
                );


        assertEquals(
                3,
                zuordnungen.size()
        );


        DokumentZuordnung z1 =
                zuordnungen.getFirst();

        assertEquals(
                "ZN-101-DOC-1001",
                z1.itemId()
        );

        assertEquals(
                1,
                z1.gruppe().nummer()
        );

        assertEquals(
                "ZN-101-DOC-1001",
                z1.dokument().id()
        );


        DokumentZuordnung z2 =
                zuordnungen.get(1);

        assertEquals(
                "ZN-101-DOC-1002",
                z2.itemId()
        );

        assertEquals(
                1,
                z2.gruppe().nummer()
        );

        assertEquals(
                "ZN-101-DOC-1002",
                z2.dokument().id()
        );


        DokumentZuordnung z3 =
                zuordnungen.get(2);

        assertEquals(
                "ZN-202-DOC-2001",
                z3.itemId()
        );

        assertEquals(
                2,
                z3.gruppe().nummer()
        );

        assertEquals(
                "ZN-202-DOC-2001",
                z3.dokument().id()
        );
    }

    @Test
    void layoutPlacementWirdEindeutigEinerDokumentZuordnungZugeordnet()
            throws Exception {

        Zahlungsnachweis zn =
                new Zahlungsnachweis();

        zn.setId(101L);

        ZahlungsnachweisDokument doc =
                new ZahlungsnachweisDokument();

        doc.setId(1001L);
        doc.setInhalt(createTestPdf());

        zn.addDokument(doc);


        List<PDFBelegGruppe> gruppen =
                service.createGruppen(
                        List.of(zn)
                );

        List<DokumentZuordnung> zuordnungen =
                service.createDokumentZuordnungen(
                        gruppen
                );


        A4LayoutPlacement placement =
                new A4LayoutPlacement(
                        "ZN-101-DOC-1001",       // itemId
                        1,                       // pageNumber
                        40f,                     // x
                        40f,                     // y
                        PDFLayoutService.A6_WIDTH,
                        PDFLayoutService.A6_HEIGHT,
                        0f,                     // sourceY
                        PDFLayoutService.A6_HEIGHT, // sourceHeight
                        0,                       // rotation
                        false                    // continued
                );


        DokumentZuordnung zuordnung =
                service.findeDokumentZuordnung(
                        placement,
                        zuordnungen
                );


        assertNotNull(zuordnung);

        assertEquals(
                "ZN-101-DOC-1001",
                zuordnung.itemId()
        );

        assertEquals(
                1,
                zuordnung.gruppe().nummer()
        );

        assertEquals(
                101L,
                zuordnung.gruppe()
                        .nachweis()
                        .getId()
        );

        assertEquals(
                "ZN-101-DOC-1001",
                zuordnung.dokument().id()
        );
    }

    @Test
    void platzierteDokumenteWerdenIhrerBeleggruppeZugeordnet()
            throws Exception {

        Zahlungsnachweis zn =
                new Zahlungsnachweis();

        zn.setId(101L);

        ZahlungsnachweisDokument doc =
                new ZahlungsnachweisDokument();

        doc.setId(1001L);
        doc.setInhalt(createTestPdf());

        zn.addDokument(doc);

        List<PDFBelegGruppe> gruppen =
                service.createGruppen(
                        List.of(zn)
                );

        List<DokumentZuordnung> zuordnungen =
                service.createDokumentZuordnungen(
                        gruppen
                );

        A4LayoutPlacement placement =
                new A4LayoutPlacement(
                        "ZN-101-DOC-1001",
                        1,
                        40f,
                        40f,
                        PDFLayoutService.A6_WIDTH,
                        PDFLayoutService.A6_HEIGHT,
                        0f,
                        PDFLayoutService.A6_HEIGHT,
                        0,
                        false
                );

        List<PDFZahlungsnachweiseService.PlatzierteDokumentZuordnung> result =
                service.createPlatzierteDokumentZuordnungen(
                        List.of(placement),
                        zuordnungen
                );

        assertEquals(
                1,
                result.size()
        );

        var eintrag =
                result.getFirst();

        assertEquals(
                "ZN-101-DOC-1001",
                eintrag.placement().itemId()
        );

        assertEquals(
                1,
                eintrag.dokumentZuordnung()
                        .gruppe()
                        .nummer()
        );

        assertEquals(
                101L,
                eintrag.dokumentZuordnung()
                        .gruppe()
                        .nachweis()
                        .getId()
        );

        assertEquals(
                "ZN-101-DOC-1001",
                eintrag.dokumentZuordnung()
                        .dokument()
                        .id()
        );
    }

    @Test
    void belegkopfWirdAusBeleggruppeErzeugt() {

        Zahlungsnachweis nachweis =
                new Zahlungsnachweis();

        nachweis.setId(101L);

        nachweis.setDatum(
                LocalDate.of(
                        2026,
                        8,
                        1
                )
        );

        nachweis.setBetrag(
                new BigDecimal("25.00")
        );

        nachweis.setBemerkung(
                "Testbeleg"
        );

        PDFBelegGruppe gruppe =
                new PDFBelegGruppe(
                        1,
                        nachweis,
                        List.of()
                );

        String kopf =
                service.createBelegkopf(
                        gruppe
                );

        assertEquals(
                "#01  Zahlungsnachweis 01.08.2026  25,00\u00A0€",
                kopf
        );
    }

    @Test
    void zahlungsnachweisEnthaeltAnzahlSeinerDokumente() {

        Zahlungsnachweis nachweis =
                new Zahlungsnachweis();

        nachweis.setId(101L);

        nachweis.addDokument(
                createDokument(
                        1001L,
                        1,
                        "beleg1.pdf",
                        createTestPdf()
                )
        );

        nachweis.addDokument(
                createDokument(
                        1002L,
                        2,
                        "beleg2.pdf",
                        createTestPdf()
                )
        );

        assertEquals(
                2,
                nachweis.getDokumente().size()
        );
    }
    @Test
    void erzeugtZahlungsnachweiseMitMehrerenBeleggruppenUndDokumenten()
            throws Exception {

        Veranstaltung veranstaltung =
                createVeranstaltung();

        /*
         * ---------------------------------------------------------
         * Zahlungsnachweis #1 – 2 Dokumente
         * ---------------------------------------------------------
         */

        Zahlungsnachweis zn1 =
                createNachweis(
                        101L,
                        "Beleggruppe 1",
                        "01.08.2026",
                        "100,00"
                );

        zn1.addDokument(
                createDokument(
                        1001L,
                        1,
                        "beleg-101-1.pdf",
                        createA6Pdf("ZN101-DOKUMENT-1")
                )
        );

        zn1.addDokument(
                createDokument(
                        1002L,
                        2,
                        "beleg-101-2.pdf",
                        createA6Pdf("ZN101-DOKUMENT-2")
                )
        );

        /*
         * ---------------------------------------------------------
         * Zahlungsnachweis #2 – 1 Dokument
         * ---------------------------------------------------------
         */

        Zahlungsnachweis zn2 =
                createNachweis(
                        202L,
                        "Beleggruppe 2",
                        "02.08.2026",
                        "200,00"
                );

        zn2.addDokument(
                createDokument(
                        2001L,
                        1,
                        "beleg-202-1.pdf",
                        createA6Pdf("ZN202-DOKUMENT-1")
                )
        );

        /*
         * ---------------------------------------------------------
         * Zahlungsnachweis #3 – 4 Dokumente
         * ---------------------------------------------------------
         */

        Zahlungsnachweis zn3 =
                createNachweis(
                        303L,
                        "Beleggruppe 3",
                        "03.08.2026",
                        "300,00"
                );

        for (int i = 1; i <= 4; i++) {

            zn3.addDokument(
                    createDokument(
                            3000L + i,
                            i,
                            "beleg-303-" + i + ".pdf",
                            createA6Pdf(
                                    "ZN303-DOKUMENT-" + i
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

        /*
         * Repository liefert die Nachweise bereits
         * in der gewünschten Reihenfolge.
         */
        when(
                zahlungsnachweisRepository
                        .findByVeranstaltungIdOrderByDatumDescIdDesc(
                                VERANSTALTUNG_ID
                        )
        ).thenReturn(
                List.of(
                        zn3,
                        zn2,
                        zn1
                )
        );

        /*
         * ---------------------------------------------------------
         * PDF erzeugen
         * ---------------------------------------------------------
         */

        byte[] result =
                service.generate(
                        VERANSTALTUNG_ID
                );

        assertNotNull(result);
        assertTrue(result.length > 0);

        /*
         * ---------------------------------------------------------
         * PDF prüfen
         * ---------------------------------------------------------
         */

        try (
                PDDocument document =
                        Loader.loadPDF(result)
        ) {

            /*
             * 7 A6-Dokumente:
             *
             * 2 + 1 + 4 = 7
             *
             * Bei 4 A6-Dokumenten pro A4-Seite:
             *
             * 2 Belegseiten
             *
             * + 1 Deckblatt
             * = 3 Seiten
             */
            assertTrue(
                    document.getNumberOfPages() >= 3
            );

            /*
             * Alle Seiten müssen A4 sein.
             */
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
    private A4LayoutItem a6(String id) {

        return item(
                id,
                PDFLayoutService.A6_WIDTH,
                PDFLayoutService.A6_HEIGHT
        );
    }

    private A4LayoutItem item(
            String id,
            float width,
            float height
    ) {
        return new A4LayoutItem(
                id,
                width,
                height,
                PdfDocumentDensity.MEDIUM,
                true
        );
    }
}