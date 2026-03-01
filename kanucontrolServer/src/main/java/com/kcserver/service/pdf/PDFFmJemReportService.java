package com.kcserver.service.pdf;

import com.kcserver.entity.Veranstaltung;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.temporal.ChronoUnit;

import static com.kcserver.util.StringUtils.heute;
import static com.kcserver.util.StringUtils.join;

@Service
@RequiredArgsConstructor
public class PDFFmJemReportService {

    private final VeranstaltungRepository veranstaltungRepository;

    public byte[] generate(Long veranstaltungId) {

        Veranstaltung v = veranstaltungRepository
                .findByIdWithRelations(veranstaltungId)
                .orElseThrow();

        try (
                PDDocument doc = Loader.loadPDF(
                        getClass().getClassLoader()
                                .getResourceAsStream("pdf/antrag_FM-JEM_template.pdf")
                                .readAllBytes()
                );
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

            /* =========================================================
               DEBUG – Feldnamen auslesen
               ========================================================= */

        /*    System.out.println("====== PDF FELDER ======");

            if (form != null) {
                form.getFieldTree().forEach(f ->
                        System.out.println(f.getFullyQualifiedName())
                );
            } else {
                System.out.println("KEIN FORMULAR GEFUNDEN");
            }

            System.out.println("========================");*/

            /* ================= Verein ================= */

            set(form, "ausrichter", v.getVerein().getName());
            if (v.getVerein() != null) {
                set(form, "ausrichter_telefon", v.getVerein().getTelefon());
            }
            set(form, "ausrichter_bank_name", v.getVerein().getBankName());
            set(form, "ausrichter_iban", v.getVerein().getIban());
            set(form, "ausrichter_bic", v.getVerein().getBic());

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
            set(form, "art_der_unterkunft", v.getArtDerUnterkunft());
            set(form, "art_der_verpflegung", v.getArtDerVerpflegung());

            String ortLand = (v.getOrt() != null ? v.getOrt() : "")
                    + (v.getLaenderCode() != null ? " (" + v.getLaenderCode().name() + ")" : "");

            set(form, "ort_und_land_der_veranstaltung", ortLand);

            set(form, "beginnDatum", v.getBeginnDatum().toString());
            set(form, "endeDatum", v.getEndeDatum().toString());

            long tage = ChronoUnit.DAYS.between(v.getBeginnDatum(), v.getEndeDatum()) + 1;
            set(form, "dauer_tage", String.valueOf(tage));


            /* ================= Teilnehmer ================= */

            Integer m = v.getGeplanteTeilnehmerMaennlich();
            Integer w = v.getGeplanteTeilnehmerWeiblich();

            set(form, "anz_tn_foerder_maennlich", m);
            set(form, "anz_tn_foerder_weiblich", w);
            set(form, "summe_tn_foerder", (m != null ? m : 0) + (w != null ? w : 0));


            /* ================= Mitarbeiter ================= */

            Integer mm = v.getGeplanteMitarbeiterMaennlich();
            Integer mw = v.getGeplanteMitarbeiterWeiblich();

            set(form, "anz_mitarbeiter_maennlich", mm);
            set(form, "anz_mitarbeiter_weiblich", mw);
            set(form, "summe_mitarbeiter", (mm != null ? mm : 0) + (mw != null ? mw : 0));

            set(form, "ort_datum",
                    join(", ", v.getVerein().getOrt(), heute()));

            /* ❗ NICHT flatten → Formular bleibt editierbar */

            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("FM/JEM PDF konnte nicht erzeugt werden", e);
        }
    }

    /* =========================================================
       Helper
       ========================================================= */

    private void set(PDAcroForm form, String field, Object value) {
        try {
            if (form.getField(field) != null && value != null) {
                form.getField(field).setValue(String.valueOf(value));
            }
        } catch (Exception ignored) {
        }
    }

}