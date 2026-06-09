package com.kcserver.service.pdf;

import com.kcserver.entity.*;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.repository.*;
import com.kcserver.service.FoerderService;
import com.kcserver.service.VeranstaltungValidator;
import com.kcserver.util.PdfFilenameUtil;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.kcserver.util.StringUtils.heute;
import static com.kcserver.util.StringUtils.join;

import com.kcserver.enumtype.FinanzKategorie;

import java.math.BigDecimal;
import com.kcserver.util.CurrencyUtil;

@Service
@RequiredArgsConstructor
public class PDFAbrechnungService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final AbrechnungRepository abrechnungRepository;
    private final AbrechnungBuchungRepository abrechnungBuchungRepository;
    private final FoerderService foerderService;

    private final VeranstaltungValidator validator;

    public byte[] generate(Long veranstaltungId) {


        Veranstaltung v = veranstaltungRepository
                .findByIdWithRelations(veranstaltungId)
                .orElseThrow();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(veranstaltungId);

        validator.validateAbrechnungFaehigOrThrow(
                v,
                teilnehmer
        );

        var abrechnung =
                abrechnungRepository
                        .findByVeranstaltungId(
                                veranstaltungId
                        )
                        .orElse(null);

        List<AbrechnungBuchung> buchungen =
                abrechnung != null
                        ? abrechnungBuchungRepository
                        .findByBeleg_Abrechnung_Id(
                                abrechnung.getId()
                        )
                        : List.of();

        if (v.getTyp() != VeranstaltungTyp.FM
                && v.getTyp() != VeranstaltungTyp.JEM) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nur FM oder JEM erlaubt"
            );
        }
        try (
                PDDocument doc = Loader.loadPDF(
                        StreamUtils.copyToByteArray(
                                new ClassPathResource(
                                        "pdf/abrechnung-JEM-FM_template.pdf"
                                ).getInputStream()
                        )
                );
                ByteArrayOutputStream out =
                        new ByteArrayOutputStream()
        ) {

            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

            setTypCheckbox(form, v);
            fillVeranstalter(form, v);
            fillLeitung(form, v);
            fillVeranstaltung(form, v, teilnehmer);
            fillFoerderung(
                    form,
                    v,
                    teilnehmer
            );
            fillFinanzen(
                    form,
                    v,
                    buchungen,
                    teilnehmer
            );

            String filename = PdfFilenameUtil.build(
                    java.time.LocalDate.now(),
                    PdfDokumentTyp.ABRECHNUNG,
                    v
            );

            doc.getDocumentInformation().setTitle(filename);
            doc.getDocumentInformation().setAuthor("KanuControl");
            doc.getDocumentInformation().setCreator("KanuControl");

            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Abrechnungs-PDF konnte nicht erzeugt werden", e);
        }


    }

    /* ========================================================= */

    private void setTypCheckbox(PDAcroForm form, Veranstaltung v) {

        if (form == null) return;

        try {

            if (v.getTyp() == VeranstaltungTyp.FM) {
                form.getField("fm").setValue("Ja");
            }

            if (v.getTyp() == VeranstaltungTyp.JEM) {
                form.getField("jem").setValue("Ja");
            }

        } catch (Exception ignored) {}
    }

    private void fillVeranstalter(PDAcroForm form, Veranstaltung v) {

        var verein = v.getVerein();

        // set(form, "datumBeginn", verein.get());
        set(form, "veranstalter_name", verein.getName());
        set(form, "veranstalter_iban", verein.getIban());
        set(form, "veranstalter_bank_name", verein.getBankName());
        set(form, "veranstalter_bic", verein.getBic());

        set(form, "veranstalter_anschrift",
                join(", ", verein.getStrasse(),
                        join(" ",verein.getPlz(), verein.getOrt())
                )
        );
    }

    private void fillLeitung(PDAcroForm form, Veranstaltung v) {

        if (v.getLeiter() == null) return;

        var l = v.getLeiter();

        set(form, "leitung_name",
                join (" ", l.getVorname(), l.getName()));

        set(form, "leitung_anschrift", l.getStrasse());
        set(form, "leitung_plz_ort",
                join(" ",l.getPlz(), l.getOrt()));

        set(form, "leitung_email", l.getEmail());
        set(form, "leitung_telefon", l.getTelefon());

    }

    private void fillVeranstaltung(
            PDAcroForm form,
            Veranstaltung v,
            List<Teilnehmer> teilnehmer
    ){

        if (v.getBeginnDatum() != null) {
            set(form, "datumBeginn",
                    v.getBeginnDatum()
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }

        if (v.getEndeDatum() != null) {
            set(form, "datumEnde",
                    v.getEndeDatum()
                            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }

        /* ================= Ort + Land ================= */

        String ort = v.getOrt();
        String land = v.getCountryCode() != null
                ? v.getCountryCode().getCode()   // 2-Zeichen-Code!
                : null;

        set(form, "veranstaltung_ort_land",
                join(" / ",ort, land));

        int gefoerdert =
                (int) foerderService
                        .countFoerderfaehigeTeilnehmer(
                                v,
                                teilnehmer
                        );

        int dauer =
                foerderService
                        .berechneFoerdertage(v);

        set(form, "anz_teilnehmer_gefoerdert", gefoerdert);
        set(form, "veranstaltung_dauer_tage", dauer);
        set(form, "datum", heute());
    }

    private void fillFoerderung(
            PDAcroForm form,
            Veranstaltung v,
            List<Teilnehmer> teilnehmer
    ) {

        long gefoerdert =
                foerderService
                        .countFoerderfaehigeTeilnehmer(
                                v,
                                teilnehmer
                        );

        int tage =
                foerderService
                        .berechneFoerdertage(v);

        BigDecimal foerdersatz =
                foerderService
                        .berechneAngewandtenFoerdersatz(
                                v
                        );

        BigDecimal beihilfe =
                foerderService
                        .berechneGesamtfoerderung(
                                v,
                                teilnehmer
                        );

        set(form, "anz_teilnehmer_gefoerdert",
                gefoerdert);

        set(form, "veranstaltung_dauer",
                tage);

        set(form, "foerdersatz",
                CurrencyUtil.decimal(foerdersatz));

        set(form, "berechnete_beihilfe",
                CurrencyUtil.decimal(beihilfe));
    }

    private void fillFinanzen(
            PDAcroForm form,
            Veranstaltung v,
            List<AbrechnungBuchung> buchungen,
            List<Teilnehmer> teilnehmer
    ) {

        BigDecimal unterkunftVerpflegung =
                sum(
                        buchungen,
                        FinanzKategorie.UNTERKUNFT,
                        FinanzKategorie.VERPFLEGUNG
                );

        BigDecimal honorare =
                sum(
                        buchungen,
                        FinanzKategorie.HONORARE
                );

        BigDecimal fahrtkosten =
                sum(
                        buchungen,
                        FinanzKategorie.FAHRTKOSTEN
                );

        BigDecimal material =
                sum(
                        buchungen,
                        FinanzKategorie.VERBRAUCHSMATERIAL
                );

        BigDecimal mieteSonstige =
                sum(
                        buchungen,
                        FinanzKategorie.MIETE,
                        FinanzKategorie.SONSTIGE_KOSTEN
                );

        BigDecimal tnGesamt =
                sum(
                        buchungen,
                        FinanzKategorie.TEILNEHMERBEITRAG
                );

        BigDecimal sonstigeEinnahmen =
                sum(
                        buchungen,
                        FinanzKategorie.SONSTIGE_EINNAHMEN,
                        FinanzKategorie.PFAND
                );

        BigDecimal kjfp =
                foerderService
                        .berechneGesamtfoerderung(
                                v,
                                teilnehmer
                        );

        BigDecimal gesamtKosten =
                unterkunftVerpflegung
                        .add(honorare)
                        .add(fahrtkosten)
                        .add(material)
                        .add(mieteSonstige);

        BigDecimal bisherigeEinnahmen =
                tnGesamt
                        .add(sonstigeEinnahmen)
                        .add(kjfp);

        BigDecimal eigenleistung =
                gesamtKosten.subtract(
                        bisherigeEinnahmen
                );

        if (eigenleistung.compareTo(BigDecimal.ZERO) < 0) {
            eigenleistung = BigDecimal.ZERO;
        }

        BigDecimal gesamtEinnahmen =
                bisherigeEinnahmen.add(
                        eigenleistung
                );

        /* ================= PDF ================= */

        set(form,
                "kosten_unterkunft_verpflegung",
                CurrencyUtil.decimal(unterkunftVerpflegung));

        set(form,
                "kosten_leitung_verguetung",
                CurrencyUtil.decimal(honorare));

        set(form,
                "kosten_fahrkosten",
                CurrencyUtil.decimal(fahrtkosten));

        set(form,
                "kosten_material",
                CurrencyUtil.decimal(material));

        set(form,
                "kosten_miete",
                CurrencyUtil.decimal(mieteSonstige));

        set(form,
                "kosten_gesamt",
                CurrencyUtil.decimal(gesamtKosten));

        set(form,
                "einnahmen_eigenleistung_veranstalter",
                CurrencyUtil.decimal(eigenleistung));

        set(form,
                "einnahmen_teilnehmer_beitraege",
                CurrencyUtil.decimal(tnGesamt));

        set(form,
                "einnahmen_sonstige",
                CurrencyUtil.decimal(sonstigeEinnahmen));

        set(form,
                "einnahmen_kjfp_zuschuss",
                CurrencyUtil.decimal(kjfp));

        set(form,
                "einnahmen_gesamt",
                CurrencyUtil.decimal(gesamtEinnahmen));
    }

    /* ========================================================= */

    private void set(PDAcroForm form, String field, Object value) {
        try {
            if (form != null && form.getField(field) != null && value != null) {
                form.getField(field).setValue(String.valueOf(value));
            }
        } catch (Exception ignored) {}
    }

    private BigDecimal sum(
            List<AbrechnungBuchung> buchungen,
            FinanzKategorie... kategorien
    ) {

        BigDecimal result = BigDecimal.ZERO;

        for (AbrechnungBuchung b : buchungen) {

            for (FinanzKategorie k : kategorien) {

                if (b.getKategorie() == k
                        && b.getBetrag() != null) {

                    result =
                            result.add(
                                    b.getBetrag()
                            );
                }
            }
        }

        return result;
    }
}
