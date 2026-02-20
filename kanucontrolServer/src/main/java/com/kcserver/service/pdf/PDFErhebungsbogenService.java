package com.kcserver.service.pdf;

import com.kcserver.dto.teilnehmer.TeilnehmerDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PDFErhebungsbogenService {

    public byte[] generate(
            VeranstaltungDetailDTO v,
            List<TeilnehmerDetailDTO> tnList
    ) throws IOException {

        try (PDDocument doc = PDDocument.load(
                new ClassPathResource("pdf/erhebungsbogen_template.pdf").getInputStream()
        )) {

            /* ===== PDF METADATEN ===== */

            String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

            String rawName = v.getName() == null ? "Veranstaltung" : v.getName();

            /* Dateisystem-sichere Variante wie beim Download */
            String cleanName = rawName
                    .replace("ä","ae").replace("ö","oe").replace("ü","ue")
                    .replace("Ä","Ae").replace("Ö","Oe").replace("Ü","Ue")
                    .replace("ß","ss")
                    .replaceAll("[^a-zA-Z0-9]", "");

            String pdfTitle = date + "_EB_" + cleanName;

            PDDocumentInformation info = doc.getDocumentInformation();

            info.setTitle(pdfTitle);
            info.setAuthor("KanuControl");
            info.setSubject("Erhebungsbogen");
            info.setCreator("KanuControl System");
            info.setProducer("Apache PDFBox");
            info.setCreationDate(Calendar.getInstance());

            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            form.setNeedAppearances(true);

            fillVeranstaltung(form, v);
            fillStatistik(form, v, tnList);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
    private void fillVeranstaltung(PDAcroForm form, VeranstaltungDetailDTO v) throws IOException {

        if (v.getBeginnDatum() != null) {
            set(form, "veranstaltungsjahr",
                    String.valueOf(v.getBeginnDatum().getYear()));
        }

        set(form, "veranstaltung_name", v.getName());
        // set(form, "veranstaltung_name#1", v.getName());

        /* ================= Leiter ================= */

        if (v.getLeiter() != null) {

            String leitung = Stream.of(
                            v.getLeiter().getVorname(),
                            v.getLeiter().getName()
                    )
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "));

            if (!leitung.isBlank()) {
                set(form, "leitung", leitung);
            }

            if (v.getLeiter().getTelefon() != null &&
                    !v.getLeiter().getTelefon().isBlank()) {

                set(form, "telefon", v.getLeiter().getTelefon());
            }
        }

        /* ================= Träger ================= */

        if (v.getVerein() != null) {

            if (v.getVerein().getName() != null)
                set(form, "traeger", v.getVerein().getName());

            if (v.getVerein().getAbk() != null)
                set(form, "traeger_abk", v.getVerein().getAbk());

            if (v.getVerein().getPlz() != null)
                set(form, "plz_traeger", v.getVerein().getPlz());
        }

        /* ================= Datum ================= */

        set(form, "beginnDatum", format(v.getBeginnDatum()));
        set(form, "endeDatum", format(v.getEndeDatum()));

        /* ================= PLZ + Ort ================= */

        if ((v.getPlz() != null && !v.getPlz().isBlank()) ||
                (v.getOrt() != null && !v.getOrt().isBlank())) {

            String plzOrt = Stream.of(v.getPlz(), v.getOrt())
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.joining(" "));

            set(form, "plz_ort", plzOrt);
        }

        /* ================= Land / Typ ================= */

        if (v.getLaenderCode() != null) {
            set(form, "land", v.getLaenderCode().name());
            set(form, "veranstaltung_land", v.getLaenderCode().getLabel());
        }

        if (v.getTyp() != null)
            set(form, "typ", v.getTyp().name());

        /* ================= Dauer + Übernachtungen ================= */

        if (v.getBeginnDatum() != null && v.getEndeDatum() != null) {

            long dauer = ChronoUnit.DAYS.between(
                    v.getBeginnDatum(),
                    v.getEndeDatum()
            ) + 1;

            set(form, "veranstaltung_tage", String.valueOf(dauer));
            set(form, "veranstaltung_anz_uebernachtungen",
                    String.valueOf(Math.max(dauer - 1, 0)));
        }

        /* ================= Durchführungsort ================= */

        if (v.getLaenderCode() != null) {

            boolean deutschland =
                    "DE".equalsIgnoreCase(v.getLaenderCode().getCode());

            // PLZ Durchführungsort
            if (deutschland && v.getPlz() != null && !v.getPlz().isBlank()) {
                set(form, "plzDurchfuehrungsort", v.getPlz());
            } else {
                set(form, "plzDurchfuehrungsort", "11111");
            }

            // Radiobutton
            PDField field = form.getField("Durchfuehrungsort");

            if (field != null && v.getLaenderCode() != null) {

                deutschland =
                        "DE".equalsIgnoreCase(v.getLaenderCode().getCode());

                String value = deutschland ? "EinOrt" : "ausland";

                try {
                    field.setValue(value);
                    System.out.println("Radiobutton gesetzt auf: " + value);
                } catch (Exception ex) {
                    System.out.println("Radiobutton Fehler: " + ex.getMessage());
                }
            }
        }
    }
    private void fillStatistik(
            PDAcroForm form,
            VeranstaltungDetailDTO v,
            List<TeilnehmerDetailDTO> list
    ) throws IOException {

        int[][] stats = new int[3][5];
        // [Geschlecht][Altersgruppe]
        // 0 = weiblich, 1 = maennlich, 2 = divers
        // Altersgruppen:
        // 0 <10
        // 1 10-13
        // 2 14-17
        // 3 18-26
        // 4 >=27

        for (TeilnehmerDetailDTO t : list) {

            if (t.getSex() == null) continue;

            int sexIndex = switch (t.getSex()) {
                case WEIBLICH -> 0;
                case MAENNLICH -> 1;
                case DIVERS -> 2;
            };

            int age = calcAge(t.getGeburtsdatum(), v.getBeginnDatum());
            int ageGroup;

            if (age < 10) ageGroup = 0;
            else if (age < 14) ageGroup = 1;
            else if (age < 18) ageGroup = 2;
            else if (age < 27) ageGroup = 3;
            else ageGroup = 4;

            stats[sexIndex][ageGroup]++;
        }

        /* ================= WEIBLICH ================= */

        set(form, "unter_10_Jahre_weiblich", String.valueOf(stats[0][0]));
        set(form, "10_bis_unter_14_Jahre_weiblich", String.valueOf(stats[0][1]));
        set(form, "14_bis_unter_18_Jahre_weiblich", String.valueOf(stats[0][2]));
        set(form, "18_bis_unter_27_Jahre_weiblich", String.valueOf(stats[0][3]));
        set(form, "27_Jahre_und_aelter_weiblich", String.valueOf(stats[0][4]));

        /* ================= MAENNLICH ================= */

        set(form, "unter_10_Jahre_maennlich", String.valueOf(stats[1][0]));
        set(form, "10_bis_unter_14_Jahre_maennlich", String.valueOf(stats[1][1]));
        set(form, "14_bis_unter_18_Jahre_maennlich", String.valueOf(stats[1][2]));
        set(form, "18_bis_unter_27_Jahre_maennlich", String.valueOf(stats[1][3]));
        set(form, "27_Jahre_und_aelter_maennlich", String.valueOf(stats[1][4]));

        /* ================= DIVERS ================= */

        set(form, "unter_10_Jahre_divers", String.valueOf(stats[2][0]));
        set(form, "10_bis_unter_14_Jahre_divers", String.valueOf(stats[2][1]));
        set(form, "14_bis_unter_18_Jahre_divers", String.valueOf(stats[2][2]));
        set(form, "18_bis_unter_27_Jahre_divers", String.valueOf(stats[2][3]));
        set(form, "27_Jahre_und_aelter_divers", String.valueOf(stats[2][4]));
    }

    private void set(PDAcroForm form, String field, String value) throws IOException {
        PDField f = form.getField(field);
        if (f != null && value != null) {
            f.setValue(value);
        }
    }

    private String format(LocalDate d) {
        if (d == null) return "";
        return d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private int calcAge(LocalDate birth, LocalDate ref) {
        if (birth == null || ref == null) return 0;
        return Period.between(birth, ref).getYears();
    }
}
