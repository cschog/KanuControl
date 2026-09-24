package com.kcserver.service.pdf;

import com.kcserver.dto.finanzen.FinanzausgleichDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.enumtype.Zahlungsweg;
import com.kcserver.exception.ErrorMessages;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.repository.abrechnung.AbrechnungBuchungRepository;
import com.kcserver.repository.abrechnung.AbrechnungRepository;
import com.kcserver.repository.abrechnung.DokumentRepository;
import com.kcserver.repository.fahrkosten.ReisekostenabrechnungRepository;
import com.kcserver.repository.finanz.FinanzGruppeRepository;
import com.kcserver.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.service.finanz.FinanzausgleichService;
import com.kcserver.util.PdfFilenameUtil;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PDFFinanzausgleichService {

    private static final PDType1Font FONT =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

    private static final PDType1Font FONT_BOLD =
            new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

    private static final float TITLE_SIZE = 18f;
    private static final float SECTION_SIZE = 11f;
    private static final float TEXT_SIZE = 9f;

    private static final float ROW_HEIGHT = 20f;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final NumberFormat MONEY =
            NumberFormat.getCurrencyInstance(
                    Locale.GERMANY
            );

    private final VeranstaltungRepository veranstaltungRepository;
    private final FinanzGruppeRepository finanzGruppeRepository;
    private final DokumentRepository dokumentRepository;

    private final AbrechnungRepository abrechnungRepository;
    private final AbrechnungBuchungRepository abrechnungBuchungRepository;

    private final FinanzausgleichService finanzausgleichService;
    private final PDFDocumentSizeService documentSizeService;
    private final PDFLayoutService layoutService;
    private final A4LayoutEngine layoutEngine;
    private final PDFDocumentComposer composer;

    private final AbrechnungBelegRepository belegRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final ReisekostenabrechnungRepository reisekostenRepository;

    /*
     * =========================================================
     * INTERNE DATENSTRUKTUREN
     * =========================================================
     */

    record FinanzausgleichGruppe(
            int nummer,
            FinanzGruppe finanzGruppe,
            FinanzausgleichDTO ausgleich,
            List<Dokument> dokumente,
            List<A4LayoutItem> layoutItems
    ) {
    }

    record PlatzierteDokumentZuordnung(
            A4LayoutPlacement placement,
            FinanzausgleichGruppe gruppe,
            Dokument dokument
    ) {
    }

    record TeilnehmerDetail(
            Teilnehmer teilnehmer,
            BigDecimal soll,
            BigDecimal ueberweisung,
            BigDecimal quittung
    ) {
        BigDecimal ist() {
            return ueberweisung.add(quittung);
        }

        BigDecimal differenz() {
            return ist().subtract(soll);
        }
    }


    record ZahlungsDetail(
            LocalDate datum,
            String teilnehmer,
            String bemerkung,
            BigDecimal betrag,
            boolean rueckzahlung
    ) {
    }


    record BuchungsDetail(
            AbrechnungBeleg beleg,
            AbrechnungBuchung buchung
    ) {
    }


    record FinanzausgleichDetailDaten(
            List<TeilnehmerDetail> teilnehmer,
            List<ZahlungsDetail> ueberweisungen,
            List<ZahlungsDetail> quittungen,
            List<BuchungsDetail> buchungen,
            List<Reisekostenabrechnung> fahrkosten,
            BigDecimal sonstigeEinnahmen
    ) {
    }

    record VKEigenanteilDaten(
            BigDecimal ueberweisungen,
            BigDecimal kjfp,
            List<BuchungsDetail> einnahmen,
            List<BuchungsDetail> ausgaben,
            List<Reisekostenabrechnung> fahrkosten,
            BigDecimal finanzausgleich
    ) {

        BigDecimal sonstigeEinnahmen() {
            return einnahmen.stream()
                    .map(b -> safeStatic(b.buchung().getBetrag()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal direkteAusgaben() {
            return ausgaben.stream()
                    .map(b -> safeStatic(b.buchung().getBetrag()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal fahrkostenSumme() {
            return fahrkosten.stream()
                    .map(r -> safeStatic(r.getGesamtBetrag()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal gesamtEinnahmen() {
            return safeStatic(ueberweisungen)
                    .add(safeStatic(kjfp))
                    .add(sonstigeEinnahmen());
        }

        BigDecimal gesamtAusgaben() {
            return direkteAusgaben()
                    .add(fahrkostenSumme())
                    .add(safeStatic(finanzausgleich));
        }

        BigDecimal eigenanteil() {
            return gesamtAusgaben()
                    .subtract(gesamtEinnahmen());
        }

        private static BigDecimal safeStatic(BigDecimal value) {
            return value == null
                    ? BigDecimal.ZERO
                    : value;
        }
    }


    /*
     * =========================================================
     * GENERATE
     * =========================================================
     */
    @Transactional(readOnly = true)
    public byte[] generate(Long veranstaltungId) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findById(veranstaltungId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorMessages.VERANSTALTUNG_NOT_FOUND
                                )
                        );

        /*
         * -----------------------------------------------------
         * 1. FINANZAUSGLEICH BERECHNEN
         * -----------------------------------------------------
         */

        List<FinanzausgleichDTO> ausgleiche =
                finanzausgleichService.berechne(
                        veranstaltungId
                );

        /*
         * VK ist keine Finanzausgleichsgruppe.
         */
        ausgleiche =
                ausgleiche.stream()
                        .filter(dto ->
                                !"VK".equals(
                                        dto.getFinanzGruppeKuerzel()
                                )
                        )
                        .toList();

        try (
                PDDocument deckblatt =
                        new PDDocument()
        ) {

            /*
             * -------------------------------------------------
             * 2. GRUPPEN ERZEUGEN
             * -------------------------------------------------
             */

            List<FinanzausgleichGruppe> gruppen =
                    createGruppen(
                            veranstaltungId,
                            ausgleiche
                    );

            /*
             * -------------------------------------------------
             * 3. DECKBLATT
             * -------------------------------------------------
             */

            createDeckblatt(
                    deckblatt,
                    veranstaltung,
                    gruppen
            );

            /*
             * -------------------------------------------------
             * 4. DETAILSEITEN JE FINANZGRUPPE
             * -------------------------------------------------
             */

            createDetailseiten(
                    deckblatt,
                    veranstaltung,
                    gruppen
            );

            /*
             * -------------------------------------------------
             * 5. DOKUMENTE SAMMELN
             * -------------------------------------------------
             */

            Map<String, byte[]> documents =
                    collectDocuments(
                            gruppen
                    );

            /*
             * -------------------------------------------------
             * 6. LAYOUT ITEMS
             * -------------------------------------------------
             */

            List<A4LayoutItem> items =
                    createLayoutItems(
                            gruppen
                    );

            /*
             * -------------------------------------------------
             * 7. LAYOUT
             * -------------------------------------------------
             */

            List<A4LayoutPlacement> placements =
                    layoutEngine.layout(
                            items
                    );

            /*
             * -------------------------------------------------
             * 8. DOKUMENT-ZUORDNUNGEN
             * -------------------------------------------------
             */

            Map<String, String> gruppenNummern =
                    createGruppenNummern(
                            gruppen
                    );

            /*
             * -------------------------------------------------
             * 9. DOKUMENTE ZUSAMMENSETZEN
             * -------------------------------------------------
             */

            byte[] dokumentPdf =
                    composer.composeWithoutFooter(
                            documents,
                            placements,
                            gruppenNummern
                    );

            /*
             * -------------------------------------------------
             * 10. DECKBLATT + DOKUMENTE
             * -------------------------------------------------
             */

            byte[] gesamtesPdf =
                    mergeDocuments(
                            deckblatt,
                            dokumentPdf
                    );

            /*
             * -------------------------------------------------
             * 11. FOOTER
             * -------------------------------------------------
             */

            String filename =
                    PdfFilenameUtil.build(
                            LocalDate.now(),
                            PdfDokumentTyp.FINANZAUSGLEICH,
                            veranstaltung
                    );

            try (
                    PDDocument document =
                            org.apache.pdfbox.Loader.loadPDF(
                                    gesamtesPdf
                            );

                    ByteArrayOutputStream out =
                            new ByteArrayOutputStream()
            ) {

                layoutService.addFooter(
                        document
                );

                document.getDocumentInformation()
                        .setTitle(filename);

                document.getDocumentInformation()
                        .setAuthor("KanuControl");

                document.getDocumentInformation()
                        .setCreator("KanuControl");

                document.save(out);

                return out.toByteArray();
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Finanzausgleich-PDF konnte nicht erzeugt werden.",
                    e
            );
        }
    }


    /*
     * =========================================================
     * GRUPPEN
     * =========================================================
     */

    private List<FinanzausgleichGruppe> createGruppen(
            Long veranstaltungId,
            List<FinanzausgleichDTO> ausgleiche
    ) throws IOException {

        List<FinanzGruppe> finanzGruppen =
                finanzGruppeRepository
                        .findWithTeilnehmerByVeranstaltungId(
                                veranstaltungId
                        );

        Map<Long, FinanzGruppe> finanzGruppeMap =
                finanzGruppen.stream()
                        .collect(
                                java.util.stream.Collectors.toMap(
                                        FinanzGruppe::getId,
                                        fg -> fg
                                )
                        );

        List<FinanzausgleichGruppe> result =
                new ArrayList<>();

        int nummer = 1;

        for (FinanzausgleichDTO ausgleich : ausgleiche) {

            FinanzGruppe finanzGruppe =
                    finanzGruppeMap.get(
                            ausgleich.getFinanzGruppeId()
                    );

            if (finanzGruppe == null) {
                throw new IOException(
                        "FinanzGruppe nicht gefunden: "
                                + ausgleich.getFinanzGruppeId()
                );
            }

            List<Dokument> dokumente =
                    dokumentRepository
                            .findByFinanzGruppeIdOrderByReihenfolgeAsc(
                                    ausgleich.getFinanzGruppeId()
                            );

            List<A4LayoutItem> layoutItems =
                    new ArrayList<>();

            for (Dokument dokument : dokumente) {

                String itemId =
                        "FA-"
                                + finanzGruppe.getId()
                                + "-DOC-"
                                + dokument.getId();

                PDFDocumentSize size;

                try {

                    size =
                            documentSizeService.determine(
                                    dokument
                            );

                } catch (IllegalStateException e) {

                    throw new IOException(
                            "Keine Dokumentgröße hinterlegt: "
                                    + "FinanzGruppe "
                                    + finanzGruppe.getId()
                                    + ", Dokument "
                                    + dokument.getId()
                                    + ", Dateiname "
                                    + dokument.getOriginalDateiname(),
                            e
                    );
                }

                layoutItems.add(
                        new A4LayoutItem(
                                itemId,
                                size.width(),
                                size.height(),
                                size.density(),
                                size.referenzObjekt()
                        )
                );
            }

            result.add(
                    new FinanzausgleichGruppe(
                            nummer,
                            finanzGruppe,
                            ausgleich,
                            dokumente,
                            layoutItems
                    )
            );

            nummer++;
        }

        return result;
    }

    private void createDetailseiten(
            PDDocument document,
            Veranstaltung veranstaltung,
            List<FinanzausgleichGruppe> gruppen
    ) throws Exception {

        List<Zahlungsnachweis> zahlungen =
                zahlungsnachweisRepository.findDetailsByVeranstaltungId(
                        veranstaltung.getId()
                );

        Map<Long, BigDecimal> sollBeitraege =
                finanzausgleichService.getSollBeitraegeByVeranstaltung(
                        veranstaltung.getId()
                );

        /*
         * ---------------------------------------------------------
         * Detailseiten der Finanzgruppen
         * ---------------------------------------------------------
         */

        for (FinanzausgleichGruppe gruppe : gruppen) {

            FinanzausgleichDetailDaten daten =
                    createDetailDaten(
                            veranstaltung,
                            gruppe,
                            zahlungen,
                            sollBeitraege
                    );

            createDetailseite(
                    document,
                    veranstaltung,
                    gruppe,
                    daten
            );
        }

        /*
         * ---------------------------------------------------------
         * VK-Detailseite
         * ---------------------------------------------------------
         */

        FinanzausgleichDetailDaten vkDaten =
                createVKDetailDaten(
                        veranstaltung,
                        zahlungen
                );

        VKEigenanteilDaten vkEigenanteil =
                createVKEigenanteilDaten(
                        veranstaltung,
                        zahlungen,
                        gruppen
                );

        createVKDetailseite(
                document,
                veranstaltung,
                vkDaten,
                vkEigenanteil
        );
    }

    private FinanzausgleichDetailDaten createVKDetailDaten(
            Veranstaltung veranstaltung,
            List<Zahlungsnachweis> zahlungen
    ) {

        List<ZahlungsDetail> ueberweisungen =
                new ArrayList<>();

        List<ZahlungsDetail> quittungen =
                new ArrayList<>();

        for (Zahlungsnachweis z : zahlungen) {

            if (z.getZahlungsweg() == null) {
                continue;
            }

            /*
             * Jeder Zahlungsnachweis ist ein Teilnehmerbeitrag
             * und damit ein Geldfluss über das VK-Konto.
             *
             * Entscheidend ist für VK ausschließlich:
             * - Zahlungsweg
             * - Betrag des Zahlungsnachweises
             * - ob es eine Rückzahlung ist
             *
             * Die Finanzgruppe und die ZahlungsPositionen dienen
             * ausschließlich der Zuordnung zum Teilnehmerkonto /
             * Finanzausgleich.
             */

            boolean rueckzahlung =
                    z.getUrspruenglicherZahlungsnachweis() != null;

            BigDecimal betrag =
                    safe(z.getBetrag());

            if (rueckzahlung) {
                betrag = betrag.negate();
            }

            String teilnehmer =
                    getZahlungsTeilnehmerText(z);

            /*
             * ---------------------------------------------------------
             * ÜBERWEISUNG
             * ---------------------------------------------------------
             */

            if (z.getZahlungsweg() == Zahlungsweg.UEBERWEISUNG) {

                ueberweisungen.add(
                        new ZahlungsDetail(
                                z.getDatum(),
                                teilnehmer,
                                safe(z.getBemerkung()),
                                betrag,
                                rueckzahlung
                        )
                );
            }

            /*
             * ---------------------------------------------------------
             * QUITTUNG
             * ---------------------------------------------------------
             */

            if (z.getZahlungsweg() == Zahlungsweg.QUITTUNG) {

                quittungen.add(
                        new ZahlungsDetail(
                                z.getDatum(),
                                teilnehmer,
                                safe(z.getBemerkung()),
                                betrag,
                                rueckzahlung
                        )
                );
            }
        }

        /*
         * Chronologisch sortieren
         */

        ueberweisungen.sort(
                Comparator.comparing(
                        ZahlungsDetail::datum,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
        );

        quittungen.sort(
                Comparator.comparing(
                        ZahlungsDetail::datum,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
        );

        return new FinanzausgleichDetailDaten(
                Collections.emptyList(),
                ueberweisungen,
                quittungen,
                Collections.emptyList(),
                Collections.emptyList(),
                BigDecimal.ZERO
        );
    }

    private VKEigenanteilDaten createVKEigenanteilDaten(
            Veranstaltung veranstaltung,
            List<Zahlungsnachweis> zahlungen,
            List<FinanzausgleichGruppe> gruppen
    ) {

        List<FinanzGruppe> alleFinanzGruppen =
                finanzGruppeRepository.findWithTeilnehmerByVeranstaltungId(
                        veranstaltung.getId()
                );

        List<Teilnehmer> alleTeilnehmer =
                alleFinanzGruppen.stream()
                        .flatMap(fg -> fg.getTeilnehmer().stream())
                        .toList();

        var abrechnung = abrechnungRepository
                .findByVeranstaltungId(veranstaltung.getId())
                .orElse(null);

        List<AbrechnungBuchung> buchungen =
                abrechnung != null
                        ? abrechnungBuchungRepository
                        .findByBeleg_Abrechnung_Id(abrechnung.getId())
                        : List.of();

        BigDecimal kjfp = sum(
                buchungen,
                FinanzKategorie.KJFP_ZUSCHUSS
        );


        Long veranstaltungId =
                veranstaltung.getId();

        /*
         * =====================================================
         * 1. ÜBERWEISUNGEN
         * =====================================================
         *
         * Alle ursprünglichen Überweisungen gehören zum
         * Geldfluss des VK.
         *
         * Rückzahlungen werden abgezogen.
         *
         * Quittungen werden NICHT berücksichtigt, da sie bereits
         * über den Finanzausgleich der Finanzgruppen abgebildet
         * werden.
         */

        BigDecimal ueberweisungen =
                BigDecimal.ZERO;

        for (Zahlungsnachweis z : zahlungen) {

            if (z.getZahlungsweg() != Zahlungsweg.UEBERWEISUNG) {
                continue;
            }

            BigDecimal betrag =
                    safe(z.getBetrag());

            if (z.getUrspruenglicherZahlungsnachweis() != null) {
                ueberweisungen =
                        ueberweisungen.subtract(betrag);
            } else {
                ueberweisungen =
                        ueberweisungen.add(betrag);
            }
        }

        /*
         * =====================================================
         * 2. VK-FINANZGRUPPE
         * =====================================================
         */

        FinanzGruppe vk =
                finanzGruppeRepository
                        .findWithTeilnehmerByVeranstaltungId(
                                veranstaltungId
                        )
                        .stream()
                        .filter(fg ->
                                "VK".equals(
                                        fg.getKuerzel()
                                )
                        )
                        .findFirst()
                        .orElse(null);

        List<BuchungsDetail> einnahmen =
                new ArrayList<>();

        List<BuchungsDetail> ausgaben =
                new ArrayList<>();

        List<Reisekostenabrechnung> fahrkosten =
                new ArrayList<>();

        if (vk != null) {

            Long vkId =
                    vk.getId();

            /*
             * -----------------------------------------------------
             * VK-BELEGE
             * -----------------------------------------------------
             */

            List<AbrechnungBeleg> belege =
                    belegRepository
                            .findByAbrechnung_Veranstaltung_IdAndFinanzGruppe_IdOrderByDatumAscLfdNrAsc(
                                    veranstaltungId,
                                    vkId
                            );

            for (AbrechnungBeleg beleg : belege) {

                for (AbrechnungBuchung buchung :
                        beleg.getPositionen()) {

                    if (buchung.getBetrag() == null
                            || buchung.getBetrag().signum() <= 0) {
                        continue;
                    }

                    FinanzKategorie kategorie =
                            buchung.getKategorie();

                    /*
                     * Einnahmen
                     */

                    if (kategorie ==
                            FinanzKategorie.SONSTIGE_EINNAHMEN) {

                        einnahmen.add(
                                new BuchungsDetail(
                                        beleg,
                                        buchung
                                )
                        );

                        continue;
                    }

                    /*
                     * Ausgaben
                     */

                    if (isKostenKategorie(kategorie)) {

                        ausgaben.add(
                                new BuchungsDetail(
                                        beleg,
                                        buchung
                                )
                        );
                    }
                }
            }

            /*
             * -----------------------------------------------------
             * VK-FAHRKOSTEN
             * -----------------------------------------------------
             */

            fahrkosten =
                    reisekostenRepository.findByFinanzGruppe(
                            veranstaltungId,
                            vkId
                    );
        }

        /*
         * =====================================================
         * 3. FINANZAUSGLEICH
         * =====================================================
         *
         * Die Erstattungen an alle Finanzgruppen sind Ausgaben
         * des VK.
         */

        BigDecimal finanzausgleich =
                gruppen.stream()
                        .map(gruppe ->
                                safe(
                                        gruppe.ausgleich()
                                                .getErstattungVomVK()
                                )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new VKEigenanteilDaten(
                ueberweisungen,
                kjfp,
                einnahmen,
                ausgaben,
                fahrkosten,
                finanzausgleich
        );
    }

    private void createVKDetailseite(
            PDDocument document,
            Veranstaltung veranstaltung,
            FinanzausgleichDetailDaten daten,
            VKEigenanteilDaten eigenanteil
    ) throws Exception {

        try (
                PDFPageWriter page =
                        new PDFPageWriter(document)
        ) {

            page.write(
                    "Finanzausgleich – VK",
                    page.getLeft(),
                    FONT_BOLD,
                    TITLE_SIZE
            );

            page.moveY(-22f);

            page.write(
                    "Veranstaltung: "
                            + safe(veranstaltung.getName()),
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-20f);

            page.write(
                    "Zeitraum: "
                            + formatDate(
                            veranstaltung.getBeginnDatum()
                    )
                            + " - "
                            + formatDate(
                            veranstaltung.getEndeDatum()
                    ),
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-30f);

            /*
             * =====================================================
             * ÜBERWEISUNGEN
             * =====================================================
             */

            if (!daten.ueberweisungen().isEmpty()) {

                writeSectionTitle(
                        page,
                        "Überweisungen auf VK"
                );

                page.moveY(-10f);

                writeZahlungsTabelle(
                        page,
                        daten.ueberweisungen()
                );
            }

            /*
             * =====================================================
             * QUITTUNGEN
             * =====================================================
             */

            if (!daten.quittungen().isEmpty()) {

                page.moveY(-30f);

                ensureSectionSpace(
                        page,
                        80f
                );

                writeSectionTitle(
                        page,
                        "Quittungen auf VK"
                );

                page.moveY(-10f);

                writeZahlungsTabelle(
                        page,
                        daten.quittungen()
                );
            }

            /*
             * =====================================================
             * BERECHNUNG EIGENANTEIL
             * =====================================================
             */

            page.moveY(-30f);

            ensureSectionSpace(
                    page,
                    220f
            );

            writeVKEigenanteil(
                    page,
                    eigenanteil
            );
        }
    }

    private void writeVKEigenanteil(
            PDFPageWriter page,
            VKEigenanteilDaten daten
    ) throws Exception {

        float x =
                page.getLeft();

        float right =
                x + page.getContentWidth();

        /*
         * =====================================================
         * ÜBERSCHRIFT
         * =====================================================
         */

        page.write(
                "Berechnung Eigenanteil",
                x,
                page.getY(),
                FONT_BOLD,
                SECTION_SIZE
        );

        page.moveY(-20f);

        /*
         * =====================================================
         * EINNAHMEN
         * =====================================================
         */

        page.write(
                "Einnahmen",
                x,
                page.getY(),
                FONT_BOLD,
                TEXT_SIZE
        );

        page.moveY(-18f);

        /*
         * Teilnehmerbeiträge / Überweisungen
         */

        page.write(
                "Teilnehmerbeiträge / Überweisungen",
                x + 10,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(daten.ueberweisungen()),
                right,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.moveY(-18f);

        page.write(
                "KJFP",
                x + 10,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(daten.kjfp()),
                right,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.moveY(-18f);

        /*
         * Weitere VK-Einnahmen
         */

        for (BuchungsDetail detail :
                daten.einnahmen()) {

            AbrechnungBeleg beleg =
                    detail.beleg();

            AbrechnungBuchung buchung =
                    detail.buchung();

            String bezeichnung =
                    safe(beleg.getBelegnummer());

            if (!safe(buchung.getBeschreibung()).isBlank()) {
                bezeichnung +=
                        " – "
                                + safe(
                                buchung.getBeschreibung()
                        );
            }

            page.write(
                    bezeichnung,
                    x + 10,
                    page.getY(),
                    FONT,
                    TEXT_SIZE
            );

            page.writeRight(
                    formatMoney(
                            buchung.getBetrag()
                    ),
                    right,
                    page.getY(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-18f);
        }

        /*
         * =====================================================
         * SUMME EINNAHMEN
         * =====================================================
         */

        page.write(
                "Summe Einnahmen",
                x + 10,
                page.getY(),
                FONT_BOLD,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(
                        daten.gesamtEinnahmen()
                ),
                right,
                page.getY(),
                FONT_BOLD,
                TEXT_SIZE
        );

        page.moveY(-24f);

        /*
         * =====================================================
         * AUSGABEN
         * =====================================================
         */

        page.write(
                "Ausgaben",
                x,
                page.getY(),
                FONT_BOLD,
                TEXT_SIZE
        );

        page.moveY(-18f);

        /*
         * Direkte VK-Rechnungen
         */

        for (BuchungsDetail detail :
                daten.ausgaben()) {

            AbrechnungBeleg beleg =
                    detail.beleg();

            AbrechnungBuchung buchung =
                    detail.buchung();

            String bezeichnung =
                    safe(beleg.getBelegnummer());

            if (!safe(buchung.getBeschreibung()).isBlank()) {
                bezeichnung +=
                        " – "
                                + safe(
                                buchung.getBeschreibung()
                        );
            }

            page.write(
                    bezeichnung,
                    x + 10,
                    page.getY(),
                    FONT,
                    TEXT_SIZE
            );

            page.writeRight(
                    formatMoney(
                            buchung.getBetrag().negate()
                    ),
                    right,
                    page.getY(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-18f);
        }

        /*
         * Fahrkosten
         */

        if (!daten.fahrkosten().isEmpty()) {

            page.write(
                    "Fahrkosten VK",
                    x + 10,
                    page.getY(),
                    FONT,
                    TEXT_SIZE
            );

            page.writeRight(
                    formatMoney(
                            daten.fahrkostenSumme().negate()
                    ),
                    right,
                    page.getY(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-18f);
        }

        /*
         * Finanzausgleich
         */

        page.write(
                "Finanzausgleich an Finanzgruppen",
                x + 10,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(
                        daten.finanzausgleich().negate()
                ),
                right,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.moveY(-10f);

        /*
         * =====================================================
         * TRENNLINIE
         * =====================================================
         */

        page.line(
                x,
                page.getY(),
                right,
                page.getY()
        );

        page.moveY(-20f);

        /*
         * =====================================================
         * SUMME AUSGABEN
         * =====================================================
         */

        page.write(
                "Summe Ausgaben",
                x + 10,
                page.getY(),
                FONT_BOLD,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(
                        daten.gesamtAusgaben().negate()
                ),
                right,
                page.getY(),
                FONT_BOLD,
                TEXT_SIZE
        );

        page.moveY(-25f);

        /*
         * =====================================================
         * EIGENANTEIL
         * =====================================================
         */

        page.write(
                "Eigenanteil",
                x,
                page.getY(),
                FONT_BOLD,
                SECTION_SIZE
        );

        page.writeRight(
                formatMoney(
                        daten.eigenanteil()
                ),
                right,
                page.getY(),
                FONT_BOLD,
                SECTION_SIZE
        );

        page.moveY(-20f);
    }

    private FinanzausgleichDetailDaten createDetailDaten(
            Veranstaltung veranstaltung,
            FinanzausgleichGruppe gruppe,
            List<Zahlungsnachweis> zahlungen,
            Map<Long, BigDecimal> sollBeitraege
    ) {

        Long veranstaltungId =
                veranstaltung.getId();

        Long finanzGruppeId =
                gruppe.finanzGruppe().getId();

        /*
         * =====================================================
         * 1. Teilnehmer
         * =====================================================
         */

        List<TeilnehmerDetail> teilnehmer =
                gruppe.finanzGruppe()
                        .getTeilnehmer()
                        .stream()
                        .sorted((a, b) -> {

                            String nameA =
                                    getPersonName(a.getPerson());

                            String nameB =
                                    getPersonName(b.getPerson());

                            return nameA.compareToIgnoreCase(nameB);
                        })
                        .map(t -> {

                            // =========================================
                            // Soll-Beitrag exakt wie im
                            // FinanzausgleichService
                            // =========================================

                            BigDecimal soll =
                                    sollBeitraege.getOrDefault(
                                            t.getId(),
                                            BigDecimal.ZERO
                                    );

                            BigDecimal ueberweisung =
                                    BigDecimal.ZERO;

                            BigDecimal quittung =
                                    BigDecimal.ZERO;

                            // =========================================
                            // Ursprüngliche Zahlungsnachweise
                            // =========================================

                            for (Zahlungsnachweis z : zahlungen) {

                                if (z.getUrspruenglicherZahlungsnachweis()
                                        != null) {
                                    continue;
                                }

                                for (ZahlungsPosition p :
                                        z.getPositionen()) {

                                    if (p.getTeilnehmer() == null
                                            || !p.getTeilnehmer()
                                            .getId()
                                            .equals(t.getId())) {
                                        continue;
                                    }

                                    BigDecimal betrag =
                                            safe(p.getBetrag());

                                    if (z.getZahlungsweg()
                                            == Zahlungsweg.UEBERWEISUNG) {

                                        ueberweisung =
                                                ueberweisung.add(betrag);
                                    }

                                    if (z.getZahlungsweg()
                                            == Zahlungsweg.QUITTUNG) {

                                        quittung =
                                                quittung.add(betrag);
                                    }
                                }
                            }

                            // =========================================
                            // Rückzahlungen
                            // =========================================

                            for (Zahlungsnachweis rueckzahlung :
                                    zahlungen) {

                                Zahlungsnachweis original =
                                        rueckzahlung
                                                .getUrspruenglicherZahlungsnachweis();

                                if (original == null) {
                                    continue;
                                }

                                boolean teilnehmerBetroffen =
                                        original.getPositionen()
                                                .stream()
                                                .anyMatch(p ->
                                                        p.getTeilnehmer() != null
                                                                && p.getTeilnehmer()
                                                                .getId()
                                                                .equals(t.getId())
                                                );

                                if (!teilnehmerBetroffen) {
                                    continue;
                                }

                                BigDecimal betrag =
                                        safe(rueckzahlung.getBetrag());

                                if (rueckzahlung.getZahlungsweg()
                                        == Zahlungsweg.UEBERWEISUNG) {

                                    ueberweisung =
                                            ueberweisung.subtract(betrag);
                                }

                                if (rueckzahlung.getZahlungsweg()
                                        == Zahlungsweg.QUITTUNG) {

                                    quittung =
                                            quittung.subtract(betrag);
                                }
                            }

                            return new TeilnehmerDetail(
                                    t,
                                    soll,
                                    ueberweisung,
                                    quittung
                            );
                        })
                        .toList();

        /*
         * =====================================================
         * 2. Zahlungsnachweise
         * =====================================================
         */

        List<ZahlungsDetail> ueberweisungen =
                new ArrayList<>();

        List<ZahlungsDetail> quittungen =
                new ArrayList<>();

        for (Zahlungsnachweis z : zahlungen) {

            if (z.getZahlungsweg() == null) {
                continue;
            }

            /*
             * Rückzahlungen separat behandeln.
             */

            boolean rueckzahlung =
                    z.getUrspruenglicherZahlungsnachweis()
                            != null;

            if (z.getZahlungsweg()
                    == Zahlungsweg.QUITTUNG) {

                if (finanzGruppeId.equals(
                        z.getFinanzGruppe() != null
                                ? z.getFinanzGruppe().getId()
                                : null
                )) {

                    quittungen.add(
                            new ZahlungsDetail(
                                    z.getDatum(),
                                    getZahlungsTeilnehmerText(z),
                                    safe(z.getBemerkung()),
                                    rueckzahlung
                                            ? safe(z.getBetrag()).negate()
                                            : safe(z.getBetrag()),
                                    rueckzahlung
                            )
                    );
                }
            }

            if (z.getZahlungsweg()
                    == Zahlungsweg.UEBERWEISUNG) {

                boolean gehoertZurGruppe =
                        z.getPositionen()
                                .stream()
                                .anyMatch(p ->
                                        p.getTeilnehmer() != null
                                                && p.getTeilnehmer()
                                                .getFinanzGruppe() != null
                                                && finanzGruppeId.equals(
                                                p.getTeilnehmer()
                                                        .getFinanzGruppe()
                                                        .getId()
                                        )
                                );

                boolean istUeberzahlung =
                        z.getUeberzahlungsFinanzGruppe() != null
                                && finanzGruppeId.equals(
                                z.getUeberzahlungsFinanzGruppe()
                                        .getId()
                        );

                /*
                 * Rückzahlungen werden über ihre finanzGruppe
                 * zugeordnet.
                 */

                if (rueckzahlung) {

                    gehoertZurGruppe =
                            z.getFinanzGruppe() != null
                                    && finanzGruppeId.equals(
                                    z.getFinanzGruppe()
                                            .getId()
                            );
                }

                if (gehoertZurGruppe || istUeberzahlung) {

                    /*
                     * Bei einer ursprünglichen Überweisung zeigen wir
                     * die tatsächlich dieser FG zugeordneten Positionen.
                     */

                    if (!rueckzahlung
                            && !z.getPositionen().isEmpty()) {

                        for (ZahlungsPosition p :
                                z.getPositionen()) {

                            if (p.getTeilnehmer() == null
                                    || p.getTeilnehmer()
                                    .getFinanzGruppe() == null
                                    || !finanzGruppeId.equals(
                                    p.getTeilnehmer()
                                            .getFinanzGruppe()
                                            .getId()
                            )) {
                                continue;
                            }

                            ueberweisungen.add(
                                    new ZahlungsDetail(
                                            z.getDatum(),
                                            getPersonName(
                                                    p.getTeilnehmer()
                                                            .getPerson()
                                            ),
                                            safe(z.getBemerkung()),
                                            safe(p.getBetrag()),
                                            false
                                    )
                            );
                        }
                    }

                    /*
                     * Überzahlungsanteil
                     */

                    if (!rueckzahlung
                            && istUeberzahlung) {

                        BigDecimal zugeordnet =
                                z.getPositionen()
                                        .stream()
                                        .map(
                                                p -> safe(p.getBetrag())
                                        )
                                        .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add
                                        );

                        BigDecimal ueberzahlung =
                                safe(z.getBetrag())
                                        .subtract(zugeordnet);

                        if (ueberzahlung.signum() > 0) {

                            ueberweisungen.add(
                                    new ZahlungsDetail(
                                            z.getDatum(),
                                            "Überzahlung",
                                            safe(z.getBemerkung()),
                                            ueberzahlung,
                                            false
                                    )
                            );
                        }
                    }

                    /*
                     * Rückzahlung
                     */

                    if (rueckzahlung) {

                        ueberweisungen.add(
                                new ZahlungsDetail(
                                        z.getDatum(),
                                        getZahlungsTeilnehmerText(z),
                                        safe(z.getBemerkung()),
                                        safe(z.getBetrag()).negate(),
                                        true
                                )
                        );
                    }
                }
            }
        }

        /*
         * =====================================================
         * 3. Belege / Einnahmen
         * =====================================================
         */

        List<AbrechnungBeleg> belege =
                belegRepository
                        .findByAbrechnung_Veranstaltung_IdAndFinanzGruppe_IdOrderByDatumAscLfdNrAsc(
                                veranstaltungId,
                                finanzGruppeId
                        );

        List<BuchungsDetail> buchungen =
                new ArrayList<>();

        BigDecimal sonstigeEinnahmen =
                BigDecimal.ZERO;

        for (AbrechnungBeleg beleg : belege) {

            for (AbrechnungBuchung buchung :
                    beleg.getPositionen()) {

                if (buchung.getBetrag() == null
                        || buchung.getBetrag().signum() <= 0) {
                    continue;
                }

                FinanzKategorie kategorie =
                        buchung.getKategorie();

                /*
                 * =====================================================
                 * SONSTIGE EINNAHMEN
                 * =====================================================
                 *
                 * Diese werden:
                 *
                 * 1. für die Zusammenfassung gesammelt
                 * 2. trotzdem als Buchung in der Detailtabelle
                 *    aufgenommen
                 */

                if (kategorie == FinanzKategorie.SONSTIGE_EINNAHMEN) {

                    sonstigeEinnahmen =
                            sonstigeEinnahmen.add(
                                    buchung.getBetrag()
                            );

                    buchungen.add(
                            new BuchungsDetail(
                                    beleg,
                                    buchung
                            )
                    );

                    continue;
                }

                /*
                 * =====================================================
                 * NORMALE AUSGABEN
                 * =====================================================
                 */

                if (!isKostenKategorie(kategorie)) {
                    continue;
                }

                buchungen.add(
                        new BuchungsDetail(
                                beleg,
                                buchung
                        )
                );
            }
        }

        /*
         * =====================================================
         * 4. Fahrkosten
         * =====================================================
         */

        List<Reisekostenabrechnung> fahrkosten =
                reisekostenRepository.findByFinanzGruppe(
                        veranstaltungId,
                        finanzGruppeId
                );

        return new FinanzausgleichDetailDaten(
                teilnehmer,
                ueberweisungen,
                quittungen,
                buchungen,
                fahrkosten,
                sonstigeEinnahmen
        );
    }

    private String getPersonName(
            Person person
    ) {

        if (person == null) {
            return "";
        }

        return safe(person.getName())
                + ", "
                + safe(person.getVorname());
    }

    private String getZahlungsTeilnehmerText(
            Zahlungsnachweis zahlungsnachweis
    ) {

        Set<String> namen =
                new LinkedHashSet<>();

        for (ZahlungsPosition position :
                zahlungsnachweis.getPositionen()) {

            if (position.getTeilnehmer() == null
                    || position.getTeilnehmer().getPerson() == null) {
                continue;
            }

            namen.add(
                    getPersonName(
                            position.getTeilnehmer().getPerson()
                    )
            );
        }

        if (namen.isEmpty()) {

            if (zahlungsnachweis.getUeberzahlungsFinanzGruppe()
                    != null) {

                return "Überzahlung";
            }

            return "";
        }

        return String.join(
                ", ",
                namen
        );
    }

    private boolean isKostenKategorie(
            FinanzKategorie kategorie
    ) {

        if (kategorie == null) {
            return false;
        }

        return switch (kategorie) {

            case UNTERKUNFT,
                 VERPFLEGUNG,
                 HONORARE,
                 FAHRKOSTEN,
                 VERBRAUCHSMATERIAL,
                 KULTUR,
                 MIETE,
                 SONSTIGE_KOSTEN -> true;

            default -> false;
        };
    }

    private void createDetailseite(
            PDDocument document,
            Veranstaltung veranstaltung,
            FinanzausgleichGruppe gruppe,
            FinanzausgleichDetailDaten daten
    ) throws Exception {

        try (
                PDFPageWriter page =
                        new PDFPageWriter(document)
        ) {

            String titel =
                    String.format(
                            "Finanzausgleich #%02d – %s",
                            gruppe.nummer(),
                            safe(
                                    gruppe.finanzGruppe()
                                            .getKuerzel()
                            )
                    );

            page.write(
                    titel,
                    page.getLeft(),
                    FONT_BOLD,
                    TITLE_SIZE
            );

            page.moveY(-22f);

            page.write(
                    "Veranstaltung: "
                            + safe(veranstaltung.getName()),
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-20f);

            page.write(
                    "Zeitraum: "
                            + formatDate(
                            veranstaltung.getBeginnDatum()
                    )
                            + " - "
                            + formatDate(
                            veranstaltung.getEndeDatum()
                    ),
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-30f);

            /*
             * =====================================================
             * TEILNEHMERBEITRÄGE
             * =====================================================
             */

            writeSectionTitle(
                    page,
                    "Teilnehmerbeiträge"
            );

            page.moveY(-10f);

            writeTeilnehmerTabelle(
                    page,
                    daten.teilnehmer()
            );

            /*
             * =====================================================
             * ÜBERWEISUNGEN
             * =====================================================
             */

            if (!daten.ueberweisungen().isEmpty()) {
                page.moveY(-30f);

                ensureSectionSpace(page, 80f);

                writeSectionTitle(
                        page,
                     "Überweisungen"
             );

                page.moveY(-10f);

                writeZahlungsTabelle(
                        page,
                        daten.ueberweisungen()
                );
            }
            /*
             * =====================================================
             * QUITTUNGEN
             * =====================================================
             */

            if (!daten.quittungen().isEmpty()) {
                page.moveY(-30f);

                ensureSectionSpace(page, 80f);

                writeSectionTitle(
                        page,
                        "Quittungen"
                );

                page.moveY(-10f);

                writeZahlungsTabelle(
                        page,
                        daten.quittungen()
                );
            }

            /*
             * =====================================================
             * BELEGE
             * =====================================================
             */

            if (!daten.buchungen().isEmpty()) {
                page.moveY(-30f);

                ensureSectionSpace(page, 100f);

                writeSectionTitle(
                        page,
                        "Belege / Buchungen"
                );

                page.moveY(-10f);

                writeBelegTabelle(
                        page,
                        daten.buchungen()
                );
            }
            /*
             * =====================================================
             * FAHRKOSTEN
             * =====================================================
             */
            if (!daten.fahrkosten().isEmpty()) {
                page.moveY(-30f);

                ensureSectionSpace(page, 80f);

                writeSectionTitle(
                        page,
                        "Fahrkosten"
                );

                page.moveY(-10f);

                writeFahrkostenTabelle(
                        page,
                        daten.fahrkosten()
                );
            }
            /*
             * =====================================================
             * ZUSAMMENFASSUNG
             * =====================================================
             */

            page.moveY(-30f);

            ensureSectionSpace(page, 130f);

            writeFinanzausgleichZusammenfassung(
                    page,
                    gruppe,
                    daten
            );
        }
    }

    private void writeSectionTitle(
            PDFPageWriter page,
            String title
    ) throws Exception {

        page.write(
                title,
                page.getLeft(),
                FONT_BOLD,
                SECTION_SIZE
        );
    }

    private void writeTeilnehmerTabelle(
            PDFPageWriter page,
            List<TeilnehmerDetail> details
    ) throws Exception {

        float x = page.getLeft();

        float width = page.getContentWidth();

        float colName = width - 240f;
        float colSoll = 60f;
        float colUeberweisung = 70f;
        float colQuittung = 60f;
        float colDifferenz = 50f;

        float rowHeight = 18f;

        writeTeilnehmerHeader(
                page,
                x,
                colName,
                colSoll,
                colUeberweisung,
                colQuittung,
                colDifferenz
        );

        page.moveY(-rowHeight);

        BigDecimal sumSoll =
                BigDecimal.ZERO;

        BigDecimal sumUeberweisung =
                BigDecimal.ZERO;

        BigDecimal sumQuittung =
                BigDecimal.ZERO;

        BigDecimal sumDifferenz =
                BigDecimal.ZERO;

        for (TeilnehmerDetail detail : details) {

            if (page.ensureSpace(rowHeight)) {
                // Die Tabelle wird auf der nächsten Seite fortgesetzt.
                writeTeilnehmerHeader(
                        page,
                        x,
                        colName,
                        colSoll,
                        colUeberweisung,
                        colQuittung,
                        colDifferenz
                );

                page.moveY(-rowHeight);
            }

            float y = page.getY();

            float currentX = x;

            page.write(
                    getPersonName(
                            detail.teilnehmer().getPerson()
                    ),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colName;

            page.writeRight(
                    formatMoney(detail.soll()),
                    currentX + colSoll - 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colSoll;

            page.writeRight(
                    formatMoney(detail.ueberweisung()),
                    currentX + colUeberweisung - 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colUeberweisung;

            page.writeRight(
                    formatMoney(detail.quittung()),
                    currentX + colQuittung - 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colQuittung;

            page.writeRight(
                    formatMoney(detail.differenz()),
                    currentX + colDifferenz - 3,
                    y - 13,
                    FONT_BOLD,
                    TEXT_SIZE
            );

            page.line(
                    x,
                    y - rowHeight,
                    x + width,
                    y - rowHeight
            );

            sumSoll =
                    sumSoll.add(detail.soll());

            sumUeberweisung =
                    sumUeberweisung.add(
                            detail.ueberweisung()
                    );

            sumQuittung =
                    sumQuittung.add(
                            detail.quittung()
                    );

            sumDifferenz =
                    sumDifferenz.add(
                            detail.differenz()
                    );

            page.moveY(-rowHeight);
        }

        /*
         * Summe
         */

        page.ensureSpace(rowHeight);

        float y = page.getY();

        float currentX = x;

        page.write(
                "Summe",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colName;

        page.writeRight(
                formatMoney(sumSoll),
                currentX + colSoll - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colSoll;

        page.writeRight(
                formatMoney(sumUeberweisung),
                currentX + colUeberweisung - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colUeberweisung;

        page.writeRight(
                formatMoney(sumQuittung),
                currentX + colQuittung - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colQuittung;

        page.writeRight(
                formatMoney(sumDifferenz),
                currentX + colDifferenz - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.moveY(-rowHeight);
    }

    private void writeTeilnehmerHeader(
            PDFPageWriter page,
            float x,
            float colName,
            float colSoll,
            float colUeberweisung,
            float colQuittung,
            float colDifferenz
    ) throws Exception {

        float y = page.getY();

        page.write(
                "Teilnehmer",
                x + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        float currentX =
                x + colName;

        page.writeRight(
                "Soll",
                currentX + colSoll - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colSoll;

        page.writeRight(
                "Überweisung",
                currentX + colUeberweisung - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colUeberweisung;

        page.writeRight(
                "Quittung",
                currentX + colQuittung - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colQuittung;

        page.writeRight(
                "Diff.",
                currentX + colDifferenz - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.line(
                x,
                y - 18f,
                x + page.getContentWidth(),
                y - 18f
        );
    }

    private void writeZahlungsTabelle(
            PDFPageWriter page,
            List<ZahlungsDetail> details
    ) throws Exception {

        if (details.isEmpty()) {

            page.write(
                    "Keine Zahlungen.",
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-18f);

            return;
        }

        float x = page.getLeft();
        float width = page.getContentWidth();

        float colDatum = 65f;
        float colName = 170f;
        float colBemerkung = width - 65f - 170f - 75f;
        float colBetrag = 75f;

        float rowHeight = 18f;

        writeZahlungsHeader(
                page,
                x,
                colDatum,
                colName,
                colBemerkung,
                colBetrag
        );

        page.moveY(-rowHeight);

        BigDecimal summe =
                BigDecimal.ZERO;

        for (ZahlungsDetail detail : details) {

            if (page.ensureSpace(rowHeight)) {

                writeZahlungsHeader(
                        page,
                        x,
                        colDatum,
                        colName,
                        colBemerkung,
                        colBetrag
                );

                page.moveY(-rowHeight);
            }

            float y = page.getY();

            float currentX = x;

            page.write(
                    formatDate(detail.datum()),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colDatum;

            page.write(
                    detail.teilnehmer(),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colName;

            page.write(
                    detail.bemerkung(),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colBemerkung;

            page.writeRight(
                    formatMoney(detail.betrag()),
                    currentX + colBetrag - 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            page.line(
                    x,
                    y - rowHeight,
                    x + width,
                    y - rowHeight
            );

            summe =
                    summe.add(
                            detail.betrag()
                    );

            page.moveY(-rowHeight);
        }

        float y = page.getY();

        page.write(
                "Summe",
                x + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(summe),
                x + width - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.moveY(-rowHeight);
    }

    private void writeZahlungsHeader(
            PDFPageWriter page,
            float x,
            float colDatum,
            float colName,
            float colBemerkung,
            float colBetrag
    ) throws Exception {

        float y = page.getY();

        page.write(
                "Datum",
                x + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        float currentX =
                x + colDatum;

        page.write(
                "Teilnehmer / Zuordnung",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colName;

        page.write(
                "Bemerkung",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colBemerkung;

        page.writeRight(
                "Betrag",
                currentX + colBetrag - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.line(
                x,
                y - 18f,
                x + page.getContentWidth(),
                y - 18f
        );
    }

    private void writeBelegTabelle(
            PDFPageWriter page,
            List<BuchungsDetail> details
    ) throws Exception {

        if (details.isEmpty()) {

            page.write(
                    "Keine Buchungen.",
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-18f);

            return;
        }

        float x = page.getLeft();
        float width = page.getContentWidth();

        float colBeleg = 65f;
        float colDatum = 65f;
        float colAussteller = 110f;
        float colKategorie = 105f;
        float colBeschreibung =
                width
                        - colBeleg
                        - colDatum
                        - colAussteller
                        - colKategorie
                        - 75f;

        float colBetrag = 75f;

        float rowHeight = 18f;

        writeBelegHeader(
                page,
                x,
                colBeleg,
                colDatum,
                colAussteller,
                colKategorie,
                colBeschreibung,
                colBetrag
        );

        page.moveY(-rowHeight);

        BigDecimal summe =
                BigDecimal.ZERO;

        for (BuchungsDetail detail : details) {

            if (page.ensureSpace(rowHeight)) {

                writeBelegHeader(
                        page,
                        x,
                        colBeleg,
                        colDatum,
                        colAussteller,
                        colKategorie,
                        colBeschreibung,
                        colBetrag
                );

                page.moveY(-rowHeight);
            }

            AbrechnungBeleg beleg =
                    detail.beleg();

            AbrechnungBuchung buchung =
                    detail.buchung();

            float y = page.getY();

            float currentX = x;

            page.write(
                    safe(beleg.getBelegnummer()),
                    currentX + 3,
                    y - 13,
                    FONT_BOLD,
                    TEXT_SIZE
            );

            currentX += colBeleg;

            page.write(
                    formatDate(beleg.getDatum()),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colDatum;

            page.write(
                    safe(beleg.getAussteller()),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colAussteller;

            page.write(
                    buchung.getKategorie() == null
                            ? ""
                            : buchung.getKategorie().name(),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colKategorie;

            page.write(
                    safe(buchung.getBeschreibung()),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colBeschreibung;

            BigDecimal betrag =
                    safe(buchung.getBetrag());

            if (buchung.getKategorie()
                    == FinanzKategorie.SONSTIGE_EINNAHMEN) {

                betrag = betrag.negate();
            }

            page.writeRight(
                    formatMoney(betrag),
                    currentX + colBetrag - 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            page.line(
                    x,
                    y - rowHeight,
                    x + width,
                    y - rowHeight
            );

            summe =
                    summe.add(betrag);

            page.moveY(-rowHeight);
        }

        float y = page.getY();

        page.write(
                "Summe Netto",
                x + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(summe),
                x + width - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.moveY(-rowHeight);
    }

    private void writeBelegHeader(
            PDFPageWriter page,
            float x,
            float colBeleg,
            float colDatum,
            float colAussteller,
            float colKategorie,
            float colBeschreibung,
            float colBetrag
    ) throws Exception {

        float y = page.getY();

        float currentX = x;

        page.write(
                "Beleg",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colBeleg;

        page.write(
                "Datum",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colDatum;

        page.write(
                "Aussteller",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colAussteller;

        page.write(
                "Kategorie",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colKategorie;

        page.write(
                "Beschreibung",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colBeschreibung;

        page.writeRight(
                "Betrag",
                currentX + colBetrag - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.line(
                x,
                y - 18f,
                x + page.getContentWidth(),
                y - 18f
        );
    }

    private void writeFahrkostenTabelle(
            PDFPageWriter page,
            List<Reisekostenabrechnung> details
    ) throws Exception {

        if (details.isEmpty()) {

            page.write(
                    "Keine Fahrkosten.",
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-18f);

            return;
        }

        float x = page.getLeft();
        float width = page.getContentWidth();

        float colDatum = 70f;
        float colFahrer = 170f;
        float colKm = 70f;
        float colBemerkung =
                width
                        - colDatum
                        - colFahrer
                        - colKm
                        - 75f;

        float colBetrag = 75f;

        float rowHeight = 18f;

        float y = page.getY();

        page.write(
                "Datum",
                x + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        float currentX = x + colDatum;

        page.write(
                "Fahrer",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colFahrer;

        page.writeRight(
                "km",
                currentX + colKm - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colKm;

        page.write(
                "Bemerkung",
                currentX + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colBemerkung;

        page.writeRight(
                "Betrag",
                currentX + colBetrag - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.line(
                x,
                y - 18f,
                x + width,
                y - 18f
        );

        page.moveY(-rowHeight);

        BigDecimal summe =
                BigDecimal.ZERO;

        for (Reisekostenabrechnung reise :
                details) {

            if (page.ensureSpace(rowHeight)) {

                y = page.getY();

                page.write(
                        "Datum",
                        x + 3,
                        y - 13,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                currentX = x + colDatum;

                page.write(
                        "Fahrer",
                        currentX + 3,
                        y - 13,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                currentX += colFahrer;

                page.writeRight(
                        "km",
                        currentX + colKm - 3,
                        y - 13,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                currentX += colKm;

                page.write(
                        "Bemerkung",
                        currentX + 3,
                        y - 13,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                currentX += colBemerkung;

                page.writeRight(
                        "Betrag",
                        currentX + colBetrag - 3,
                        y - 13,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                page.line(
                        x,
                        y - 18f,
                        x + width,
                        y - 18f
                );

                page.moveY(-rowHeight);
            }

            y = page.getY();

            currentX = x;

            page.write(
                    formatDate(
                            reise.getAbrechnungsdatum()
                    ),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colDatum;

            page.write(
                    getPersonName(
                            reise.getFahrer()
                    ),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colFahrer;

            page.writeRight(
                    reise.getGesamtKilometer() == null
                            ? ""
                            : String.valueOf(
                            reise.getGesamtKilometer()
                    ),
                    currentX + colKm - 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colKm;

            page.write(
                    safe(reise.getBemerkung()),
                    currentX + 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            currentX += colBemerkung;

            page.writeRight(
                    formatMoney(
                            reise.getGesamtBetrag()
                    ),
                    currentX + colBetrag - 3,
                    y - 13,
                    FONT,
                    TEXT_SIZE
            );

            page.line(
                    x,
                    y - rowHeight,
                    x + width,
                    y - rowHeight
            );

            summe =
                    summe.add(
                            safe(
                                    reise.getGesamtBetrag()
                            )
                    );

            page.moveY(-rowHeight);
        }

        y = page.getY();

        page.write(
                "Summe Fahrkosten",
                x + 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(summe),
                x + width - 3,
                y - 13,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.moveY(-rowHeight);
    }

    private void writeFinanzausgleichZusammenfassung(
            PDFPageWriter page,
            FinanzausgleichGruppe gruppe,
            FinanzausgleichDetailDaten daten
    ) throws Exception {

        FinanzausgleichDTO dto =
                gruppe.ausgleich();

        page.write(
                "Finanzausgleich",
                page.getLeft(),
                FONT_BOLD,
                SECTION_SIZE
        );

        page.moveY(-20f);

        float x = page.getLeft();
        float right =
                x + page.getContentWidth();

        /*
         * -----------------------------------------------------
         * Ausgaben
         * -----------------------------------------------------
         */

        page.write(
                "Ausgaben",
                x,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(dto.getAusgaben()),
                right,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        /*
         * -----------------------------------------------------
         * Sonstige Einnahmen
         *
         * dto.getAusgaben() ist bereits netto.
         * Die Einnahmen werden hier deshalb nur transparent
         * dargestellt und NICHT erneut verrechnet.
         * -----------------------------------------------------
         */

        if (daten.sonstigeEinnahmen().signum() != 0) {

            page.moveY(-18f);

            page.write(
                    "davon sonstige Einnahmen",
                    x,
                    page.getY(),
                    FONT,
                    TEXT_SIZE
            );

            page.writeRight(
                    formatMoney(
                            daten.sonstigeEinnahmen().negate()
                    ),
                    right,
                    page.getY(),
                    FONT,
                    TEXT_SIZE
            );
        }

        /*
         * -----------------------------------------------------
         * Fahrkosten
         * -----------------------------------------------------
         */

        page.moveY(-18f);

        page.write(
                "Fahrkosten",
                x,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(dto.getFahrkosten()),
                right,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        /*
         * -----------------------------------------------------
         * Quittungen
         * -----------------------------------------------------
         */

        page.moveY(-18f);

        page.write(
                "Quittungen",
                x,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        page.writeRight(
                formatMoney(
                        safe(
                                dto.getTeilnehmerBeitraegeQuittung()
                        ).negate()
                ),
                right,
                page.getY(),
                FONT,
                TEXT_SIZE
        );

        /*
         * -----------------------------------------------------
         * Trennlinie
         * -----------------------------------------------------
         */

        page.moveY(-8f);

        page.line(
                x,
                page.getY(),
                right,
                page.getY()
        );

        /*
         * -----------------------------------------------------
         * Erstattung
         * -----------------------------------------------------
         */

        page.moveY(-20f);

        page.write(
                "Erstattung vom VK",
                x,
                page.getY(),
                FONT_BOLD,
                SECTION_SIZE
        );

        page.writeRight(
                formatMoney(
                        dto.getErstattungVomVK()
                ),
                right,
                page.getY(),
                FONT_BOLD,
                SECTION_SIZE
        );
    }

    private void ensureSectionSpace(
            PDFPageWriter page,
            float requiredHeight
    ) throws Exception {

        page.ensureSpace(requiredHeight);
    }

    /*
     * =========================================================
     * DECKBLATT
     * =========================================================
     */

    private void createDeckblatt(
            PDDocument document,
            Veranstaltung veranstaltung,
            List<FinanzausgleichGruppe> gruppen
    ) throws Exception {

        try (
                PDFPageWriter page =
                        new PDFPageWriter(document)
        ) {

            page.write(
                    "Finanzausgleich",
                    page.getLeft(),
                    FONT_BOLD,
                    TITLE_SIZE
            );

            page.moveY(-35f);

            page.write(
                    "Veranstaltung: "
                            + safe(
                            veranstaltung.getName()
                    ),
                    page.getLeft(),
                    FONT_BOLD,
                    SECTION_SIZE
            );

            page.moveY(-18f);

            String zeitraum =
                    formatDate(
                            veranstaltung.getBeginnDatum()
                    )
                            + " - "
                            + formatDate(
                            veranstaltung.getEndeDatum()
                    );

            page.write(
                    "Zeitraum: " + zeitraum,
                    page.getLeft(),
                    FONT,
                    TEXT_SIZE
            );

            page.moveY(-17f);

            if (veranstaltung.getOrt() != null
                    && !veranstaltung.getOrt().isBlank()) {

                page.write(
                        "Ort: "
                                + veranstaltung.getOrt(),
                        page.getLeft(),
                        FONT,
                        TEXT_SIZE
                );

                page.moveY(-25f);

            } else {

                page.moveY(-15f);
            }

            /*
             * -------------------------------------------------
             * HINWEIS
             * -------------------------------------------------
             */

            page.write(
                    "Nur für die eigene Dokumentation.",
                    page.getLeft(),
                    FONT_BOLD,
                    TEXT_SIZE
            );

            page.moveY(-13f);

            page.write(
                    "Die Nachweise sind zusammen mit der Abrechnung bei KVNRW einzureichen.",
                    page.getLeft(),
                    FONT_BOLD,
                    TEXT_SIZE
            );

            page.moveY(-25f);

            /*
             * -------------------------------------------------
             * TABELLE
             * -------------------------------------------------
             */

            float tableX =
                    page.getLeft();

            float tableWidth =
                    page.getContentWidth();

            float colNummer = 32f;
            float colGruppe = 45f;
            float colSoll = 65f;
            float colUeberweisung = 70f;
            float colQuittung = 60f;
            float colAusgaben = 65f;
            float colFahrkosten = 65f;

            float colAusgleich =
                    tableWidth
                            - colNummer
                            - colGruppe
                            - colSoll
                            - colUeberweisung
                            - colQuittung
                            - colAusgaben
                            - colFahrkosten;

            drawTableHeader(
                    page,
                    tableX,
                    colNummer,
                    colGruppe,
                    colSoll,
                    colUeberweisung,
                    colQuittung,
                    colAusgaben,
                    colFahrkosten,
                    colAusgleich
            );

            page.moveY(-ROW_HEIGHT);

            BigDecimal gesamt =
                    BigDecimal.ZERO;

            for (FinanzausgleichGruppe gruppe : gruppen) {

                boolean neueSeite =
                        page.ensureSpace(
                                ROW_HEIGHT
                        );

                if (neueSeite) {

                    drawTableHeader(
                            page,
                            tableX,
                            colNummer,
                            colGruppe,
                            colSoll,
                            colUeberweisung,
                            colQuittung,
                            colAusgaben,
                            colFahrkosten,
                            colAusgleich
                    );

                    page.moveY(-ROW_HEIGHT);
                }

                float y =
                        page.getY();

                FinanzausgleichDTO dto =
                        gruppe.ausgleich();

                page.write(
                        String.format("#%02d", gruppe.nummer()),
                        tableX + 3,
                        y - 14,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                float x = tableX + colNummer;

                page.write(
                        dto.getFinanzGruppeKuerzel(),
                        x + 3,
                        y - 14,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                x += colGruppe;

                page.writeRight(
                        formatMoney(
                                dto.getTeilnehmerBeitraegeSoll()
                        ),
                        x + colSoll - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colSoll;

                page.writeRight(
                        formatMoney(
                                dto.getTeilnehmerBeitraegeUeberweisung()
                        ),
                        x + colUeberweisung - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colUeberweisung;

                page.writeRight(
                        formatMoney(
                                dto.getTeilnehmerBeitraegeQuittung()
                        ),
                        x + colQuittung - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colQuittung;

                page.writeRight(
                        formatMoney(
                                dto.getAusgaben()
                        ),
                        x + colAusgaben - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colAusgaben;

                page.writeRight(
                        formatMoney(
                                dto.getFahrkosten()
                        ),
                        x + colFahrkosten - 3,
                        y - 14,
                        FONT,
                        TEXT_SIZE
                );

                x += colFahrkosten;

                page.writeRight(
                        formatMoney(
                                dto.getErstattungVomVK()
                        ),
                        x + colAusgleich - 3,
                        y - 14,
                        FONT_BOLD,
                        TEXT_SIZE
                );

                page.line(
                        tableX,
                        y - ROW_HEIGHT,
                        tableX + tableWidth,
                        y - ROW_HEIGHT
                );

                gesamt =
                        gesamt.add(
                                safe(
                                        dto.getErstattungVomVK()
                                )
                        );

                page.moveY(-ROW_HEIGHT);
            }

            /*
             * -------------------------------------------------
             * GESAMT
             * -------------------------------------------------
             */

            page.ensureSpace(30f);

            page.moveY(-15f);

            page.writeRight(
                    "Gesamterstattung: "
                            + formatMoney(gesamt),
                    tableX + tableWidth,
                    FONT_BOLD,
                    SECTION_SIZE
            );

            /*
             * -------------------------------------------------
             * FEHLENDE NACHWEISE
             * -------------------------------------------------
             */

            long fehlendeDokumente =
                    gruppen.stream()
                            .filter(gruppe ->
                                    gruppe.dokumente()
                                            .isEmpty()
                            )
                            .count();

            if (fehlendeDokumente > 0) {

                page.ensureSpace(30f);

                page.moveY(-15f);

                String hinweis =
                        fehlendeDokumente == 1
                                ? "Hinweis: Bei einer Finanzgruppe "
                                + "fehlt noch der Nachweis."
                                : "Hinweis: Bei "
                                + fehlendeDokumente
                                + " Finanzgruppen fehlen noch "
                                + "Nachweisdokumente.";

                page.write(
                        hinweis,
                        tableX,
                        page.getY(),
                        FONT_BOLD,
                        TEXT_SIZE
                );
            }
        }
    }


    /*
     * =========================================================
     * TABELLENKOPF
     * =========================================================
     */

    private void drawTableHeader(
            PDFPageWriter page,
            float x,
            float colNummer,
            float colGruppe,
            float colSoll,
            float colUeberweisung,
            float colQuittung,
            float colAusgaben,
            float colFahrkosten,
            float colAusgleich
    ) throws Exception {

        float y =
                page.getY();

        /*
         * -----------------------------------------------------
         * Nummer
         * -----------------------------------------------------
         */

        page.write(
                "Nr.",
                x + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        /*
         * -----------------------------------------------------
         * Gruppe
         * -----------------------------------------------------
         */

        float currentX =
                x + colNummer;

        page.write(
                "Gruppe",
                currentX + 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colGruppe;

        /*
         * -----------------------------------------------------
         * Soll
         * -----------------------------------------------------
         */

        page.writeRight(
                "Soll",
                currentX + colSoll - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colSoll;

        /*
         * -----------------------------------------------------
         * Überweisung
         * -----------------------------------------------------
         */

        page.writeRight(
                "Überweisung",
                currentX + colUeberweisung - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colUeberweisung;

        /*
         * -----------------------------------------------------
         * Quittung
         * -----------------------------------------------------
         */

        page.writeRight(
                "Quittung",
                currentX + colQuittung - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colQuittung;

        /*
         * -----------------------------------------------------
         * Ausgaben
         * -----------------------------------------------------
         */

        page.writeRight(
                "Ausgaben",
                currentX + colAusgaben - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colAusgaben;

        /*
         * -----------------------------------------------------
         * Fahrkosten
         * -----------------------------------------------------
         */

        page.writeRight(
                "Fahrkosten",
                currentX + colFahrkosten - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        currentX += colFahrkosten;

        /*
         * -----------------------------------------------------
         * Ausgleich
         * -----------------------------------------------------
         */

        page.writeRight(
                "Ausgleich",
                currentX + colAusgleich - 3,
                y - 14,
                FONT_BOLD,
                TEXT_SIZE
        );

        page.line(
                x,
                y - ROW_HEIGHT,
                currentX + colAusgleich,
                y - ROW_HEIGHT
        );
    }


    /*
     * =========================================================
     * DOKUMENTE
     * =========================================================
     */

    private Map<String, byte[]> collectDocuments(
            List<FinanzausgleichGruppe> gruppen
    ) {

        Map<String, byte[]> documents =
                new LinkedHashMap<>();

        for (FinanzausgleichGruppe gruppe : gruppen) {

            for (Dokument dokument :
                    gruppe.dokumente()) {

                String id =
                        "FA-"
                                + gruppe.finanzGruppe().getId()
                                + "-DOC-"
                                + dokument.getId();

                documents.put(
                        id,
                        dokument.getInhalt()
                );
            }
        }

        return documents;
    }


    private List<A4LayoutItem> createLayoutItems(
            List<FinanzausgleichGruppe> gruppen
    ) {

        List<A4LayoutItem> items =
                new ArrayList<>();

        for (FinanzausgleichGruppe gruppe : gruppen) {

            items.addAll(
                    gruppe.layoutItems()
            );
        }

        return items;
    }


    private Map<String, String> createGruppenNummern(
            List<FinanzausgleichGruppe> gruppen
    ) {

        Map<String, String> result =
                new LinkedHashMap<>();

        for (FinanzausgleichGruppe gruppe : gruppen) {

            String nummer =
                    String.format(
                            "#%02d",
                            gruppe.nummer()
                    );

            for (A4LayoutItem dokument :
                    gruppe.layoutItems()) {

                result.put(
                        dokument.id(),
                        nummer
                );
            }
        }

        return result;
    }


    /*
     * =========================================================
     * DECKBLATT + DOKUMENTE
     * =========================================================
     */

    private byte[] mergeDocuments(PDDocument deckblatt, byte[] dokumentPdf) throws IOException {

        Path deckblattFile = Files.createTempFile("kc-deckblatt-", ".pdf");
        Path dokumentFile = null;
        Path outputFile = Files.createTempFile("kc-finanzausgleich-", ".pdf");

        try {
            // Deckblatt als temporäre PDF-Datei speichern
            deckblatt.save(deckblattFile.toFile());

            PDFMergerUtility merger = new PDFMergerUtility();

            // Deckblatt
            merger.addSource(deckblattFile.toFile());

            // Detail-/Dokument-PDF
            if (dokumentPdf != null && dokumentPdf.length > 0) {
                dokumentFile = Files.createTempFile("kc-dokumente-", ".pdf");
                Files.write(dokumentFile, dokumentPdf);

                merger.addSource(dokumentFile.toFile());
            }

            // Zieldatei
            merger.setDestinationFileName(outputFile.toString());

            // PDFBox 3.x
            merger.mergeDocuments(null);

            return Files.readAllBytes(outputFile);

        } finally {
            Files.deleteIfExists(deckblattFile);

            if (dokumentFile != null) {
                Files.deleteIfExists(dokumentFile);
            }

            Files.deleteIfExists(outputFile);
        }
    }


    private BigDecimal sum(
            List<AbrechnungBuchung> buchungen,
            FinanzKategorie... kategorien
    ) {

        BigDecimal result = BigDecimal.ZERO;

        for (AbrechnungBuchung b : buchungen) {

            if (b.getBetrag() == null
                    || b.getKategorie() == null) {
                continue;
            }

            for (FinanzKategorie kategorie : kategorien) {

                if (b.getKategorie() == kategorie) {

                    result = result.add(
                            b.getBetrag()
                    );

                    break;
                }
            }
        }

        return result;
    }

    /*
     * =========================================================
     * FORMATIERUNG
     * =========================================================
     */

    private String formatDate(
            LocalDate date
    ) {

        return date == null
                ? ""
                : date.format(DATE_FORMAT);
    }


    private String formatMoney(
            BigDecimal value
    ) {

        return MONEY.format(
                safe(value)
        );
    }


    private BigDecimal safe(
            BigDecimal value
    ) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }


    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}