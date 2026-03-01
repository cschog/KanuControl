package com.kcserver.service.pdf;

import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.kcserver.util.StringUtils.heute;
import static com.kcserver.util.StringUtils.join;

@Service
@RequiredArgsConstructor
public class PDFAbrechnungService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;

    public byte[] generate(Long veranstaltungId) {

        Veranstaltung v = veranstaltungRepository
                .findByIdWithRelations(veranstaltungId)
                .orElseThrow();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findByVeranstaltungWithPerson(veranstaltungId);

        if (v.getTyp() != VeranstaltungTyp.FM
                && v.getTyp() != VeranstaltungTyp.JEM) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nur FM oder JEM erlaubt"
            );
        }

        try (
                PDDocument doc = Loader.loadPDF(
                        getClass().getClassLoader()
                                .getResourceAsStream("pdf/abrechnung-JEM-FM_template.pdf")
                                .readAllBytes()
                );
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {

            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

            setTypCheckbox(form, v);
            fillVeranstalter(form, v);
            fillLeitung(form, v);
            fillVeranstaltung(form, v, teilnehmer);
            fillFoerderung(form, v);

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
        String land = v.getLaenderCode() != null
                ? v.getLaenderCode().getCode()   // 2-Zeichen-Code!
                : null;

        set(form, "veranstaltung_ort_land",
                join(" / ",ort, land));

        int gefoerdert =
                countGefoerderteTeilnehmer(
                        teilnehmer,
                        v.getBeginnDatum()
                );

        long dauer = berechneDauer(v);

        set(form, "anz_teilnehmer_gefoerdert", gefoerdert);
        set(form, "veranstaltung_dauer_tage", dauer);
        set(form, "datum", heute());
    }

    private void fillFoerderung(PDAcroForm form, Veranstaltung v) {

        Integer tn =
                (v.getGeplanteTeilnehmerMaennlich() != null
                        ? v.getGeplanteTeilnehmerMaennlich() : 0)
                        +
                        (v.getGeplanteTeilnehmerWeiblich() != null
                                ? v.getGeplanteTeilnehmerWeiblich() : 0);

        set(form, "anz_tn", tn);

        long tage =
                ChronoUnit.DAYS.between(
                        v.getBeginnDatum(),
                        v.getEndeDatum()
                ) + 1;

        set(form, "tage", tage);

        // Fördersatz später dynamisch aus FoerdersatzService holen
    }

    /* ========================================================= */

    private void set(PDAcroForm form, String field, Object value) {
        try {
            if (form != null && form.getField(field) != null && value != null) {
                form.getField(field).setValue(String.valueOf(value));
            }
        } catch (Exception ignored) {}
    }

    private int countGefoerderteTeilnehmer(
            List<Teilnehmer> teilnehmer,
            LocalDate stichtag
    ) {

        int count = 0;

        for (Teilnehmer t : teilnehmer) {

            Person p = t.getPerson();
            if (p == null || p.getGeburtsdatum() == null) continue;

            int alter = Period
                    .between(p.getGeburtsdatum(), stichtag)
                    .getYears();

            if (alter >= 6 && alter <= 20) {
                count++;
            }
        }

        return count;
    }

    private long berechneDauer(Veranstaltung v) {

        return ChronoUnit.DAYS.between(
                v.getBeginnDatum(),
                v.getEndeDatum()
        ) + 1;
    }


}
