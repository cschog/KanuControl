package com.kcserver.service.pdf;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PDFTeilnehmerlisteService {

    private static final int TN_PER_PAGE = 15;


    /* =========================================================
       PUBLIC ENTRY
       ========================================================= */

    public byte[] generate(VeranstaltungDetailDTO v, List<TeilnehmerDetailDTO> tnList) throws IOException {

        tnList = sortTeilnehmer(tnList, v.getLeiterId());

        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream resultOut = new ByteArrayOutputStream();

        int pages = (int) Math.ceil((double) tnList.size() / TN_PER_PAGE);

        for (int p = 0; p < pages; p++) {

            try (PDDocument template = PDDocument.load(
                    new ClassPathResource("pdf/teilnehmer_template.pdf").getInputStream()
            )) {

                PDAcroForm form = template.getDocumentCatalog().getAcroForm();
                form.setNeedAppearances(true);

                fillHeader(form, v);

                int start = p * TN_PER_PAGE;
                int end = Math.min(start + TN_PER_PAGE, tnList.size());

                set(form, "seite_nr", String.valueOf(p + 1));
                set(form, "seite_total", String.valueOf(pages));

                for (int i = start; i < end; i++) {
                    int globalNr = i + 1;
                    int row = i - start + 1;
                    fillTeilnehmer(form, tnList.get(i), row, v, globalNr);
                }

                ByteArrayOutputStream pageOut = new ByteArrayOutputStream();
                template.save(pageOut);

                merger.addSource(new ByteArrayInputStream(pageOut.toByteArray()));
            }
        }

        merger.setDestinationStream(resultOut);
        merger.mergeDocuments(null);

        return resultOut.toByteArray();
    }

    /* =========================================================
       HEADER FILL  ← DEINE METHODE GEHÖRT GENAU HIERHIN
       ========================================================= */

    private void fillHeader(PDAcroForm form, VeranstaltungDetailDTO v) throws IOException {

        /* ================= Veranstaltung ================= */

        set(form, "veranstaltung_name", v.getName());

        String von = formatDate(v.getBeginnDatum());
        String bis = formatDate(v.getEndeDatum());

        set(form, "veranstaltung_von", von);
        set(form, "veranstaltung_bis", bis);

        /* ================= Zeitraum (zusammengesetzt) ================= */

        if (!von.isEmpty() && !bis.isEmpty()) {
            set(form, "zeitraum", von + " - " + bis);
        } else if (!von.isEmpty()) {
            set(form, "zeitraum", von);
        } else if (!bis.isEmpty()) {
            set(form, "zeitraum", bis);
        }

        /* ================= Träger = Vereinsname ================= */

        if (v.getVerein() != null) {
            set(form, "traeger", v.getVerein().getName());
        }

        /* ================= Leiter ================= */

        if (v.getLeiter() != null) {
            set(form, "leiter_name",
                    v.getLeiter().getVorname() + " " + v.getLeiter().getName());
        }

        /* ================= Footer Ort + Datum ================= */

        if (v.getVerein() != null) {

            String ort = v.getVerein().getOrt();   // Vereinsort
            String heute = formatDate(LocalDate.now());

            if (ort != null && !ort.isBlank()) {
                set(form, "footer_ortdatum", ort + ", " + heute);
            } else {
                set(form, "footer_ortdatum", heute);
            }
        }

        /* ================= CHECKBOXEN ================= */

        if (v.getTyp() != null) {

            switch (v.getTyp()) {

                case JEM, FM -> {
                    setCheckbox(form, "jem_fm_control", true);
                    setCheckbox(form, "bm_control", false);
                    setCheckbox(form, "qm_control", false);
                }

                case BM -> {
                    setCheckbox(form, "bm_control", true);
                    setCheckbox(form, "jem_fm_control", false);
                    setCheckbox(form, "qm_control", false);
                }

                default -> {
                    setCheckbox(form, "jem_fm_control", false);
                    setCheckbox(form, "bm_control", false);
                    setCheckbox(form, "qm_control", false);
                }
            }
        }
    }

    /* =========================================================
       TEILNEHMER FILL
       ========================================================= */

    private void fillTeilnehmer(PDAcroForm form,
                                TeilnehmerDetailDTO tn,
                                int row,
                                VeranstaltungDetailDTO v,
                                int globalNr) throws IOException {

        PersonRefDTO p = tn.getPerson();
        if (p == null) return;

        /* ================= NR ================= */
        set(form, "nr_" + row, String.valueOf(globalNr));

        /* ================= NAME ================= */
        set(form, "name_" + row, (p.getName()) + ", " + p.getVorname()) ;

        /* ================= ROLLE ================= */
        if (tn.getRolle() != null) {
            set(form, "rolle_" + row, tn.getRolle().getCode());
        }

        /* ================= ALTER (zum Beginn) ================= */
        set(form, "alter_" + row,
                calcAgeAtDate(tn.getGeburtsdatum(), v.getBeginnDatum()));

        /* ================= PLZ ================= */
        set(form, "plz_" + row, tn.getPlz() != null ? tn.getPlz() : "");

        /* ================= GESCHLECHT ================= */
        if (tn.getSex() != null) {
            set(form, "geschlecht_" + row, tn.getSex().getCode());
        }
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private void set(PDAcroForm form, String field, String value) throws IOException {
        PDField f = form.getField(field);
        if (f != null && value != null) {
            f.setValue(value);
        }
    }

    private String formatDate(LocalDate d) {
        if (d == null) return "";
        return d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String calcAgeAtDate(LocalDate birth, LocalDate refDate) {
        if (birth == null || refDate == null) return "";
        return String.valueOf(java.time.Period.between(birth, refDate).getYears());
    }

    private List<TeilnehmerDetailDTO> sortTeilnehmer(
            List<TeilnehmerDetailDTO> list,
            Long leiterId
    ) {
        return list.stream()
                .sorted((a, b) -> {

                    // Leiter immer zuerst
                    if (a.getPersonId().equals(leiterId)) return -1;
                    if (b.getPersonId().equals(leiterId)) return 1;

                    String vereinA = a.getPerson().getHauptvereinAbk() == null ? "" : a.getPerson().getHauptvereinAbk();
                    String vereinB = b.getPerson().getHauptvereinAbk() == null ? "" : b.getPerson().getHauptvereinAbk();

                    int v = vereinA.compareToIgnoreCase(vereinB);
                    if (v != 0) return v;

                    int n = a.getPerson().getName().compareToIgnoreCase(b.getPerson().getName());
                    if (n != 0) return n;

                    return a.getPerson().getVorname().compareToIgnoreCase(b.getPerson().getVorname());
                })
                .toList();
    }
    private void setCheckbox(PDAcroForm form, String fieldName, boolean checked) throws IOException {
        PDField field = form.getField(fieldName);
        if (field != null) {
            field.setValue(checked ? "Ja" : "Off");
        }
    }
}
