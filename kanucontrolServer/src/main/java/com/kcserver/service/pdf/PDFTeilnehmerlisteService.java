package com.kcserver.service.pdf;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

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

    public byte[] generate(VeranstaltungDetailDTO v,
                           List<TeilnehmerDetailDTO> tnList) {

        try {

            tnList = sortTeilnehmer(tnList, v.getLeiterId());

            PDDocument masterDoc = new PDDocument();
            PDFMergerUtility merger = new PDFMergerUtility();

            int pages = (int) Math.ceil((double) tnList.size() / TN_PER_PAGE);

            for (int p = 0; p < pages; p++) {

                try (PDDocument template = Loader.loadPDF(
                        StreamUtils.copyToByteArray(
                                new ClassPathResource("pdf/teilnehmer_template.pdf")
                                        .getInputStream()
                        )
                )) {

                    PDAcroForm form = template.getDocumentCatalog().getAcroForm();
                    if (form != null) {
                        form.setNeedAppearances(true);
                    }

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

                    merger.appendDocument(masterDoc, template);
                }
            }

            String filename =
                    LocalDate.now() + "_TN_" +
                            sanitizeFilename(v.getName()) + ".pdf";

            PDDocumentInformation info = masterDoc.getDocumentInformation();
            info.setTitle(filename);
            info.setAuthor("KanuControl");
            info.setCreator("KanuControl");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            masterDoc.save(out);
            masterDoc.close();

            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Teilnehmerliste PDF konnte nicht erzeugt werden", e);
        }
    }

    /* =========================================================
       HEADER
       ========================================================= */

    private void fillHeader(PDAcroForm form,
                            VeranstaltungDetailDTO v) throws IOException {

        if (form == null) return;

        set(form, "veranstaltung_name", v.getName());

        set(form, "veranstaltung_ort", v.getOrt());

        String von = formatDate(v.getBeginnDatum());
        String bis = formatDate(v.getEndeDatum());

        set(form, "veranstaltung_von", von);
        set(form, "veranstaltung_bis", bis);

        if (!von.isEmpty() && !bis.isEmpty())
            set(form, "zeitraum", von + " - " + bis);

        if (v.getVerein() != null) {
            set(form, "traeger", v.getVerein().getName());

            String ort = v.getVerein().getOrt();
            String heute = formatDate(LocalDate.now());

            set(form, "footer_ortdatum",
                    (ort != null && !ort.isBlank())
                            ? ort + ", " + heute
                            : heute);
        }

        if (v.getLeiter() != null) {
            set(form, "leiter_name",
                    v.getLeiter().getVorname() + " " + v.getLeiter().getName());
        }

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
       TEILNEHMER
       ========================================================= */

    private void fillTeilnehmer(PDAcroForm form,
                                TeilnehmerDetailDTO tn,
                                int row,
                                VeranstaltungDetailDTO v,
                                int globalNr) throws IOException {

        if (form == null || tn.getPerson() == null) return;

        PersonRefDTO p = tn.getPerson();

        set(form, "nr_" + row, String.valueOf(globalNr));
        set(form, "name_" + row, p.getName() + ", " + p.getVorname());

        if (tn.getRolle() != null)
            set(form, "rolle_" + row, tn.getRolle().getCode());

        set(form, "alter_" + row,
                calcAgeAtDate(tn.getGeburtsdatum(), v.getBeginnDatum()));

        set(form, "plz_" + row,
                tn.getPlz() != null ? tn.getPlz() : "");

        if (tn.getSex() != null)
            set(form, "geschlecht_" + row,
                    tn.getSex().getCode().toLowerCase());
    }

    /* =========================================================
       HELPER
       ========================================================= */

    private void set(PDAcroForm form,
                     String field,
                     String value) throws IOException {

        if (form == null) return;

        PDField f = form.getField(field);
        if (f != null && value != null) {
            f.setValue(value);
        }
    }

    private void setCheckbox(PDAcroForm form,
                             String fieldName,
                             boolean checked) throws IOException {

        if (form == null) return;

        PDField field = form.getField(fieldName);
        if (field != null) {
            field.setValue(checked ? "Ja" : "Off");
        }
    }

    private String formatDate(LocalDate d) {
        if (d == null) return "";
        return d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String calcAgeAtDate(LocalDate birth,
                                 LocalDate refDate) {

        if (birth == null || refDate == null) return "";
        return String.valueOf(
                java.time.Period.between(birth, refDate).getYears()
        );
    }

    private List<TeilnehmerDetailDTO> sortTeilnehmer(
            List<TeilnehmerDetailDTO> list,
            Long leiterId) {

        return list.stream()
                .sorted((a, b) -> {

                    if (a.getPersonId().equals(leiterId)) return -1;
                    if (b.getPersonId().equals(leiterId)) return 1;

                    String vereinA = a.getPerson().getHauptvereinAbk() == null
                            ? "" : a.getPerson().getHauptvereinAbk();

                    String vereinB = b.getPerson().getHauptvereinAbk() == null
                            ? "" : b.getPerson().getHauptvereinAbk();

                    int v = vereinA.compareToIgnoreCase(vereinB);
                    if (v != 0) return v;

                    int n = a.getPerson().getName()
                            .compareToIgnoreCase(b.getPerson().getName());
                    if (n != 0) return n;

                    return a.getPerson().getVorname()
                            .compareToIgnoreCase(b.getPerson().getVorname());
                })
                .toList();
    }

    private String sanitizeFilename(String name) {
        if (name == null) return "Veranstaltung";
        return name
                .replaceAll("[\\\\/:*?\"<>|]", "")
                .replaceAll("\\s+", "_")
                .trim();
    }
}