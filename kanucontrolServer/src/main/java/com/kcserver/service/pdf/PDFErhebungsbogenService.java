package com.kcserver.service.pdf;

import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.Sex;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;

import static com.kcserver.util.StringUtils.formatDate;

@Service
@RequiredArgsConstructor
public class PDFErhebungsbogenService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;

    /* =========================================================
       PUBLIC ENTRY
       ========================================================= */

    public byte[] generate(Long veranstaltungId) {

        Veranstaltung v = veranstaltungRepository
                .findByIdWithRelations(veranstaltungId)
                .orElseThrow();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findByVeranstaltungWithPerson(veranstaltungId);

        try {
            return generatePdf(v, teilnehmer);
        } catch (Exception e) {
            throw new RuntimeException("Erhebungsbogen PDF Fehler", e);
        }
    }

    /* =========================================================
       STATISTIK BERECHNEN
       ========================================================= */

    private Statistik berechneStatistik(
            List<Teilnehmer> teilnehmer,
            LocalDate stichtag
    ) {

        Statistik s = new Statistik();

        for (Teilnehmer t : teilnehmer) {

            Person p = t.getPerson();
            if (p.getGeburtsdatum() == null || p.getSex() == null)
                continue;

            int alter =
                    Period.between(p.getGeburtsdatum(), stichtag).getYears();

            Altersgruppe gruppe = ermittleAltersgruppe(alter);

            s.increment(p.getSex(), gruppe);
        }

        return s;
    }

    private Altersgruppe ermittleAltersgruppe(int alter) {

        if (alter < 10) return Altersgruppe.UNTER_10;
        if (alter <= 13) return Altersgruppe.ZEHN_BIS_13;
        if (alter <= 17) return Altersgruppe.VIERZEHN_BIS_17;
        if (alter <= 26) return Altersgruppe.ACHTZEHN_BIS_26;
        return Altersgruppe.SIEBENUNDZWANZIG_PLUS;
    }

    /* =========================================================
       PDF GENERATION
       ========================================================= */

    private byte[] generatePdf(
            Veranstaltung v,
            List<Teilnehmer> teilnehmer
    ) throws IOException {

        try (PDDocument doc = Loader.loadPDF(
                StreamUtils.copyToByteArray(
                        new ClassPathResource("pdf/erhebungsbogen_template.pdf")
                                .getInputStream()
                )
        )) {

            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            form.setNeedAppearances(true);

            fillVeranstaltung(form, v);
            fillStatistik(form, v, teilnehmer);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    /* =========================================================
       PDF FELDER FÜLLEN
       ========================================================= */

    private void fillVeranstaltung(PDAcroForm form,
                                   Veranstaltung v) throws IOException {

        set(form, "veranstaltungsjahr",
                String.valueOf(v.getBeginnDatum().getYear()));

        set(form, "veranstaltung_name", v.getName());

        /* ================= Leitung ================= */

        if (v.getLeiter() != null) {

            var l = v.getLeiter();

            String name = concat(l.getVorname(), l.getName());
            set(form, "leitung_name", name);

            set(form, "leitung_anschrift", l.getStrasse());

            String plzOrt = concat(l.getPlz(), l.getOrt());
            set(form, "leitung_plz_ort", plzOrt);

            set(form, "leitung_telefon", l.getTelefon());
        }

        /* ================= Träger ================= */

        if (v.getVerein() != null) {

            var verein = v.getVerein();

            set(form, "traeger", verein.getName());
            set(form, "traeger_abk", verein.getAbk());
            set(form, "plz_traeger", verein.getPlz());
        }

        /* ================= Datum ================= */

        set(form, "beginnDatum", formatDate(v.getBeginnDatum()));
        set(form, "endeDatum", formatDate(v.getEndeDatum()));

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
                    //  System.out.println("Radiobutton gesetzt auf: " + value);
                } catch (Exception ex) {
                    // System.out.println("Radiobutton Fehler: " + ex.getMessage());
                }
            }
        }
    }



    private void fillStatistik(
            PDAcroForm form,
            Veranstaltung v,
            List<Teilnehmer> list
    ) throws IOException {

        int[][] stats = new int[3][5];
        int[][] ehrenamt = new int[3][5];

        for (Teilnehmer t : list) {

            Person p = t.getPerson();
            if (p == null || p.getGeburtsdatum() == null || p.getSex() == null)
                continue;

            int sexIndex = switch (p.getSex()) {
                case WEIBLICH -> 0;
                case MAENNLICH -> 1;
                case DIVERS -> 2;
                default -> throw new IllegalStateException(
                        "Unbekannter Wert für Sex: " + p.getSex()
                );
            };

            int age = calcAge(p.getGeburtsdatum(), v.getBeginnDatum());

            int ageGroup;
            if (age < 10) ageGroup = 0;
            else if (age < 14) ageGroup = 1;
            else if (age < 18) ageGroup = 2;
            else if (age < 27) ageGroup = 3;
            else ageGroup = 4;

            stats[sexIndex][ageGroup]++;

            // Ehrenamt nur bei Rolle gesetzt
            if (t.getRolle() != null) {

                int ageGroupEA;
                if (age < 16) ageGroupEA = 0;
                else if (age < 18) ageGroupEA = 1;
                else if (age < 27) ageGroupEA = 2;
                else if (age < 45) ageGroupEA = 3;
                else ageGroupEA = 4;

                ehrenamt[sexIndex][ageGroupEA]++;
            }
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

        /* ================= EHRENAMT ================= */
        set(form, "unter_16_Jahre_ehrenamt_weiblich", String.valueOf(ehrenamt[0][0]));
        set(form, "unter_16_Jahre_ehrenamt_maennlich", String.valueOf(ehrenamt[1][0]));
        set(form, "unter_16_Jahre_ehrenamt_divers", String.valueOf(ehrenamt[2][0]));

        set(form, "16_bis_unter_18_Jahre_ehrenamt_weiblich", String.valueOf(ehrenamt[0][1]));
        set(form, "16_bis_unter_18_Jahre_ehrenamt_maennlich", String.valueOf(ehrenamt[1][1]));
        set(form, "16_bis_unter_18_Jahre_ehrenamt_divers", String.valueOf(ehrenamt[2][1]));

        set(form, "18_bis_unter_27_Jahre_ehrenamt_weiblich", String.valueOf(ehrenamt[0][2]));
        set(form, "18_bis_unter_27_Jahre_ehrenamt_maennlich", String.valueOf(ehrenamt[1][2]));
        set(form, "18_bis_unter_27_Jahre_ehrenamt_divers", String.valueOf(ehrenamt[2][2]));

        set(form, "27_bis_unter_45_Jahre_ehrenamt_weiblich", String.valueOf(ehrenamt[0][3]));
        set(form, "27_bis_unter_45_Jahre_ehrenamt_maennlich", String.valueOf(ehrenamt[1][3]));
        set(form, "27_bis_unter_45_Jahre_ehrenamt_divers", String.valueOf(ehrenamt[2][3]));

        set(form, "45_Jahre_und_aelter_ehrenamt_weiblich", String.valueOf(ehrenamt[0][4]));
        set(form, "45_Jahre_und_aelter_ehrenamt_maennlich", String.valueOf(ehrenamt[1][4]));
        set(form, "45_Jahre_und_aelter_ehrenamt_divers", String.valueOf(ehrenamt[2][4]));
    }

    private void set(PDAcroForm form,
                     String field,
                     String value) throws IOException {

        PDField f = form.getField(field);
        if (f != null) f.setValue(value);
    }

    class Statistik {

        private final EnumMap<Sex, EnumMap<Altersgruppe, Integer>> data =
                new EnumMap<>(Sex.class);

        Statistik() {
            for (Sex s : Sex.values()) {
                EnumMap<Altersgruppe, Integer> map =
                        new EnumMap<>(Altersgruppe.class);
                for (Altersgruppe a : Altersgruppe.values()) {
                    map.put(a, 0);
                }
                data.put(s, map);
            }
        }

        void increment(Sex sex, Altersgruppe gruppe) {
            data.get(sex).put(gruppe,
                    data.get(sex).get(gruppe) + 1);
        }

        String get(Sex sex, Altersgruppe gruppe) {
            return String.valueOf(data.get(sex).get(gruppe));
        }
    }

    enum Altersgruppe {
        UNTER_10,
        ZEHN_BIS_13,
        VIERZEHN_BIS_17,
        ACHTZEHN_BIS_26,
        SIEBENUNDZWANZIG_PLUS
    }

    private String concat(String a, String b) {
        String s1 = a == null ? "" : a.trim();
        String s2 = b == null ? "" : b.trim();
        return (s1 + " " + s2).trim();
    }


    private int calcAge(LocalDate birth, LocalDate ref) {
        if (birth == null || ref == null) return 0;
        return Period.between(birth, ref).getYears();
    }


}