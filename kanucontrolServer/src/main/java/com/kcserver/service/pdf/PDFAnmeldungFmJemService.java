package com.kcserver.service.pdf;

import com.kcserver.entity.Veranstaltung;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

import static com.kcserver.util.StringUtils.heute;
import static com.kcserver.util.StringUtils.join;
import com.kcserver.entity.Planung;
import com.kcserver.entity.PlanungPosition;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.repository.PlanungRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import org.springframework.transaction.annotation.Transactional;

import com.kcserver.util.CurrencyUtil;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.util.PdfFilenameUtil;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PDFAnmeldungFmJemService {

    private final PlanungRepository planungRepository;

    @Transactional(readOnly = true)
    public byte[] generate(Long veranstaltungId) {

        Planung planung =
                planungRepository
                        .findByVeranstaltungIdWithPositionen(veranstaltungId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Für diese Veranstaltung existiert keine Planung."
                        ));

        Veranstaltung v = planung.getVeranstaltung();

        try (
                PDDocument doc = Loader.loadPDF(
                        StreamUtils.copyToByteArray(
                                new ClassPathResource(
                                        "pdf/antrag_FM-JEM_template.pdf"
                                ).getInputStream()
                        )
                );
                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();


            /* ================= Verein ================= */

            var verein = v.getVerein();

            set(form, "ausrichter", verein.getName());
            set(form, "ausrichter_telefon", verein.getTelefon());
            set(form, "ausrichter_bank_name", verein.getBankName());
            set(form, "ausrichter_iban", verein.getIban());
            set(form, "ausrichter_bic", verein.getBic());

            String plzOrt = (v.getVerein().getPlz() != null ? v.getVerein().getPlz() : "")
                    + " "
                    + (v.getVerein().getOrt() != null ? v.getVerein().getOrt() : "");

            set(form, "ausrichter_plz_ort", plzOrt.trim());
            set(form, "ausrichter_anschrift", v.getVerein().getStrasse());

            /* =========================
   KONTOINHABER
   ========================= */

            if (v.getVerein().getKontoinhaber() != null) {

                var k = v.getVerein().getKontoinhaber();

                set(form, "kontoinhaber_name",
                        join(" ", k.getVorname(), k.getName()));

                set(form, "kontoinhaber_anschrift", k.getStrasse());

                set(form, "kontoinhaber_plz_ort",
                        join(" ", k.getPlz(), k.getOrt()));
            }

            /* =========================
   LEITUNG
   ========================= */

            if (v.getLeiter() != null) {

                var l = v.getLeiter();

                set(form, "leitung_name",
                        join(" ", l.getVorname(), l.getName()));

                set(form, "leitung_anschrift", l.getStrasse());

                set(form, "leitung_plz_ort",
                        join(l.getPlz(), l.getOrt()));

                set(form, "leitung_telefon", l.getTelefon());

                set(form, "leitung_telefon_festnetz", l.getTelefonFestnetz());

                set(form, "leitung_email", l.getEmail());
            }

            /* ================= Veranstaltung ================= */

            set(form, "veranstaltung_name", v.getName());
            set(form, "art_der_unterkunft", unterkunft(v));
            set(form, "art_der_verpflegung", verpflegung(v));

            String ortLand = (v.getOrt() != null ? v.getOrt() : "")
                    + (v.getCountryCode() != null ? " (" + v.getCountryCode().name() + ")" : "");

            set(form, "ort_und_land_der_veranstaltung", ortLand);

            set(form, "beginnDatum", v.getBeginnDatum().toString());
            set(form, "endeDatum", v.getEndeDatum().toString());

            long tage = ChronoUnit.DAYS.between(
                    v.getBeginnDatum(),
                    v.getEndeDatum()
            ) + 1;

            set(form, "dauer_tage", String.valueOf(tage));


            /* ================= Teilnehmer ================= */

            Integer m = planung.getGeplanteTeilnehmerMaennlich();
            Integer w = planung.getGeplanteTeilnehmerWeiblich();

            set(form, "anz_tn_foerder_maennlich", m);
            set(form, "anz_tn_foerder_weiblich", w);
            set(form, "summe_tn_foerder", (m != null ? m : 0) + (w != null ? w : 0));


            /* ================= Mitarbeiter ================= */

            Integer mm = planung.getGeplanteMitarbeiterMaennlich();
            Integer mw = planung.getGeplanteMitarbeiterWeiblich();

            set(form, "anz_mitarbeiter_maennlich", mm);
            set(form, "anz_mitarbeiter_weiblich", mw);
            set(form, "summe_mitarbeiter", (mm != null ? mm : 0) + (mw != null ? mw : 0));

            set(form, "ort_datum",
                    join(", ", v.getVerein().getOrt(), heute()));

            /* =========================
               PLAN-KOSTEN / EINNAHMEN
               ========================= */

                BigDecimal unterkunftVerpflegung = sum(
                        planung,
                        FinanzKategorie.UNTERKUNFT,
                        FinanzKategorie.VERPFLEGUNG
                );

                BigDecimal honorare = sum(
                        planung,
                        FinanzKategorie.HONORARE
                );

                BigDecimal fahrtkosten = sum(
                        planung,
                        FinanzKategorie.FAHRTKOSTEN
                );

                BigDecimal verbrauchsmaterial = sum(
                        planung,
                        FinanzKategorie.VERBRAUCHSMATERIAL
                );

                BigDecimal mietkosten = sum(
                        planung,
                        FinanzKategorie.MIETE
                );

                BigDecimal sonstigeKosten = sum(
                        planung,
                        FinanzKategorie.SONSTIGE_KOSTEN
                );

                BigDecimal tnBeitragUnter21 =
                        planung.getTeilnehmerBeitragUnter21Jahre() != null
                                ? planung.getTeilnehmerBeitragUnter21Jahre()
                                : BigDecimal.ZERO;
                BigDecimal mitarbeiterBeitrag =
                    planung.getMitarbeiterBeitrag() != null
                            ? planung.getMitarbeiterBeitrag()
                            : BigDecimal.ZERO;

                BigDecimal sonstigeEinnahmen = sum(
                        planung,
                        FinanzKategorie.SONSTIGE_EINNAHMEN
                );

                BigDecimal kjfp = sum(
                        planung,
                        FinanzKategorie.KJFP_ZUSCHUSS
                );

                /* ================= PDF FELDER ================= */

                set(form, "unterkunft_und_verpflegung",
                        CurrencyUtil.decimal(unterkunftVerpflegung));

                set(form, "honorare", CurrencyUtil.decimal(honorare));

                set(form, "fahrtkosten", CurrencyUtil.decimal(fahrtkosten));

                set(form, "verbrauchsmaterial",
                        CurrencyUtil.decimal(verbrauchsmaterial));

                set(form, "mietkosten", CurrencyUtil.decimal(mietkosten));

                set(form, "sonstige_kosten",
                        CurrencyUtil.decimal(sonstigeKosten));

                set(form, "kjfp_zuschuss", CurrencyUtil.decimal(kjfp));

                /* Teilnehmerbeiträge Gesamt */

            int anzahlTeilnehmer =
                    (m != null ? m : 0)
                            + (w != null ? w : 0)
                            + (planung.getGeplanteTeilnehmerDivers() != null
                            ? planung.getGeplanteTeilnehmerDivers() : 0);

            int anzahlMitarbeiter =
                    (mm != null ? mm : 0)
                            + (mw != null ? mw : 0)
                            + (planung.getGeplanteMitarbeiterDivers() != null
                            ? planung.getGeplanteMitarbeiterDivers() : 0);

            BigDecimal summeTnBeitraege =
                    tnBeitragUnter21.multiply(BigDecimal.valueOf(anzahlTeilnehmer))
                            .add(
                                    mitarbeiterBeitrag.multiply(BigDecimal.valueOf(anzahlMitarbeiter))
                            );

            int gesamtPersonen = anzahlTeilnehmer + anzahlMitarbeiter;

            set(form, "anz_tn", gesamtPersonen);


            set(form, "summe_TN_beitraege",
                        CurrencyUtil.decimal(summeTnBeitraege));

            BigDecimal formularBeitrag = BigDecimal.ZERO;

            if (gesamtPersonen > 0) {
                formularBeitrag = summeTnBeitraege.divide(
                        BigDecimal.valueOf(gesamtPersonen),
                        2,
                        RoundingMode.HALF_UP
                );
            }

            set(form, "tn_beitrag", CurrencyUtil.decimal(formularBeitrag));

                /* Summen */

                BigDecimal summeAusgaben =
                        unterkunftVerpflegung
                                .add(honorare)
                                .add(fahrtkosten)
                                .add(verbrauchsmaterial)
                                .add(mietkosten)
                                .add(sonstigeKosten);

                BigDecimal summeEinnahmen =
                        summeTnBeitraege
                                .add(sonstigeEinnahmen)
                                .add(kjfp);

            // Eigenanteil berechnen

            BigDecimal eigenanteil = summeAusgaben
                    .subtract(summeEinnahmen);

            // Im Formular werden die sonstigen Einnahmen inkl. Eigenanteil ausgewiesen
            BigDecimal sonstigeEinnahmenGesamt = sonstigeEinnahmen.add(eigenanteil);

            set(form, "sonstige_einnahmen",
                    CurrencyUtil.decimal(sonstigeEinnahmenGesamt));

            set(form, "summe_ausgaben",
                    CurrencyUtil.decimal(summeAusgaben));

            BigDecimal gesamtSummeinnahmen =
                    summeEinnahmen
                            .add(eigenanteil);

            set(form, "summe_einnahmen",
                    CurrencyUtil.decimal(gesamtSummeinnahmen));

            /* ❗ NICHT flatten → Formular bleibt editierbar */

            String filename = PdfFilenameUtil.build(
                    java.time.LocalDate.now(),
                    PdfDokumentTyp.ANMELDUNG,
                    v
            );

            doc.getDocumentInformation().setTitle(filename);
            doc.getDocumentInformation().setAuthor("KanuControl");
            doc.getDocumentInformation().setCreator("KanuControl");

            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Anmeldung FM/JEM PDF konnte nicht erzeugt werden", e);
        }
    }

    /* =========================================================
       Helper
       ========================================================= */

    private void set(PDAcroForm form, String fieldName, Object value) {
        try {
            PDField field = form.getField(fieldName);

            if (field == null) {
                System.out.println("PDF-Feld fehlt: " + fieldName);
                return;
            }

            if (value != null) {
                field.setValue(String.valueOf(value));
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Fehler beim Setzen des PDF-Feldes '" + fieldName + "' mit Wert '" + value + "'",
                    e
            );
        }
    }

    private BigDecimal sum(
            Planung planung,
            FinanzKategorie... kategorien
    ) {

        BigDecimal result = BigDecimal.ZERO;

        for (PlanungPosition p : planung.getPositionen()) {

            for (FinanzKategorie k : kategorien) {

                if (p.getKategorie() == k
                        && p.getBetrag() != null) {

                    result = result.add(
                            p.getBetrag()
                    );
                }
            }
        }

        return result;
    }

    private String unterkunft(Veranstaltung v) {
        return v.getUnterkunftsart() == null
                ? ""
                : v.getUnterkunftsart().getBezeichnung();
    }

    private String verpflegung(Veranstaltung v) {
        return v.getVerpflegungsmodell() == null
                ? ""
                : v.getVerpflegungsmodell().getBezeichnung();
    }

}