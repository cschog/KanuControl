package com.kcserver.service.pdf;

import com.kcserver.dto.reisekosten.KostenZeile;
import com.kcserver.entity.*;
import com.kcserver.repository.ReisekostenKonfigurationRepository;
import com.kcserver.repository.ReisekostenabrechnungRepository;
import com.kcserver.util.PdfFilenameUtil;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PDFFahrkostenabrechnungService {

    private final ReisekostenabrechnungRepository repository;
    private final ReisekostenKonfigurationRepository configRepository;

    @Transactional(readOnly = true)
    public byte[] generate(Long abrechnungId) {

        try {

            Reisekostenabrechnung abrechnung =
                    repository.findById(abrechnungId)
                            .orElseThrow();

            LocalDate datum = abrechnung.getAbrechnungsdatum();

            ReisekostenKonfiguration konfiguration =
                    configRepository
                            .findFirstByGueltigVonLessThanEqualAndGueltigBisGreaterThanEqual(
                                    datum,
                                    datum
                            )
                            .or(() ->
                                    configRepository
                                            .findFirstByGueltigVonLessThanEqualAndGueltigBisIsNull(
                                                    datum
                                            )
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Keine gültige Reisekostenkonfiguration gefunden"
                                    )
                            );

            try (PDDocument document = Loader.loadPDF(
                    StreamUtils.copyToByteArray(
                            new ClassPathResource(
                                    "pdf/fahrkostenformular_template.pdf")
                                    .getInputStream()
                    ))) {

                PDAcroForm form =
                        document.getDocumentCatalog()
                                .getAcroForm();

                if (form != null) {
                    form.setNeedAppearances(true);
                }

                fillHeader(form, abrechnung);

                fillFahrtabschnitte(form, abrechnung);
                fillKostenUebersicht(
                        form,
                        abrechnung,
                        konfiguration
                );
                fillFooter(form, abrechnung);

                PDDocumentInformation info =
                        document.getDocumentInformation();

                info.setTitle(
                        "Fahrkostenabrechnung_"
                                + abrechnung.getId()
                );

                info.setAuthor("KanuControl");
                info.setCreator("KanuControl");

                try (ByteArrayOutputStream out =
                             new ByteArrayOutputStream()) {

                    document.save(out);
                    return out.toByteArray();
                }
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Fahrkostenabrechnung PDF konnte nicht erzeugt werden",
                    e
            );
        }
    }

    @Transactional(readOnly = true)
    public String buildFilename(Long abrechnungId) {

        Reisekostenabrechnung rk =
                repository.findById(abrechnungId)
                        .orElseThrow();

        return PdfFilenameUtil.buildReisekosten(
                LocalDate.now(),
                rk.getVeranstaltung(),
                rk.getFahrer().getName(),
                rk.getFahrer().getVorname()
        );
    }


    private void fillHeader(
            PDAcroForm form,
            Reisekostenabrechnung abrechnung
    ) throws IOException {

        Person fahrer = abrechnung.getFahrer();

        set(
                form,
                "Name",
                fahrer.getVorname()
                        + " "
                        + fahrer.getName()
        );

        set(
                form,
                "Anschrift",
                buildAnschrift(fahrer)
        );

        set(
                form,
                "Veranstaltung",
                abrechnung.getVeranstaltung().getName()
        );

        if (abrechnung.getVeranstaltung().getVerein() != null) {
            set(
                    form,
                    "Veranstalter",
                    abrechnung.getVeranstaltung()
                            .getVerein()
                            .getName()
            );
        }

        set(
                form,
                "Beginn-der-Veranstaltung",
                formatDate(
                        abrechnung.getVeranstaltung()
                                .getBeginnDatum()
                )
        );

        set(
                form,
                "Ende-der-Veranstaltung",
                formatDate(
                        abrechnung.getVeranstaltung()
                                .getEndeDatum()
                )
        );
    }

    private void fillFahrtabschnitte(
            PDAcroForm form,
            Reisekostenabrechnung abrechnung
    ) throws IOException {

        int row = 1;

        for (Fahrtabschnitt abschnitt :
                abrechnung.getFahrtabschnitte()
                        .stream()
                        .sorted(Comparator.comparingInt(Fahrtabschnitt::getReihenfolge))
                        .toList()) {

            if (row > 15) {
                break;
            }

            set(
                    form,
                    "Fahrabschnitte-von-nachRow" + row,
                    buildVonNach(abschnitt)
            );

            set(
                    form,
                    "BemerkungRow" + row,
                    nullToEmpty(
                            abschnitt.getBeschreibung()
                    )
            );

            set(
                    form,
                    "kmRow" + row,
                    String.valueOf(
                            abschnitt.getKilometer()
                    )
            );

            set(
                    form,
                    "MF" + row,
                    abschnitt.getMitfahrer().isEmpty()
                            ? ""
                            : String.valueOf(abschnitt.getMitfahrer().size())
            );

            set(
                    form,
                    "H" + row,
                    abschnitt.isAnhaenger()
                            ? "x"
                            : ""
            );

            row++;
        }
    }


    private void fillFooter(
            PDAcroForm form,
            Reisekostenabrechnung abrechnung
    ) throws IOException {

        Person fahrer = abrechnung.getFahrer();

        set(
                form,
                "Bank",
                nullToEmpty(
                        fahrer.getBankName()
                )
        );

        set(
                form,
                "IBAN",
                nullToEmpty(
                        fahrer.getIban()
                )
        );

        set(
                form,
                "BIC",
                nullToEmpty(
                        fahrer.getBic()
                )
        );

        set(
                form,
                "Rechnungsbetrag",
                formatEuro(abrechnung.getGesamtBetrag())
        );

        set(
                form,
                "Datum",
                formatDate(
                        abrechnung.getAbrechnungsdatum()
                )
        );

        set(
                form,
                "Unterschrift",
                fahrer.getVorname()
                        + " "
                        + fahrer.getName()
        );
    }
    private String formatEuro(BigDecimal betrag) {

        if (betrag == null) {
            return "";
        }

        NumberFormat nf =
                NumberFormat.getCurrencyInstance(Locale.GERMANY);

        return nf.format(betrag)
                .replace('\u00A0', ' ');
    }

    private String buildVonNach(
            Fahrtabschnitt a
    ) {

        return formatOrt(
                a.getVonPlz(),
                a.getVonOrt()
        ) + " -> " +
                formatOrt(
                        a.getNachPlz(),
                        a.getNachOrt()
                );
    }

    private String formatOrt(
            String plz,
            String ort
    ) {

        StringBuilder sb =
                new StringBuilder();

        if (plz != null && !plz.isBlank()) {
            sb.append(plz).append(" ");
        }

        if (ort != null) {
            sb.append(ort);
        }

        return sb.toString().trim();
    }

    private String buildAnschrift(
            Person person
    ) {

        StringBuilder sb =
                new StringBuilder();

        if (person.getStrasse() != null) {
            sb.append(person.getStrasse());
        }

        sb.append(", ");
        // sb.append(System.lineSeparator());

        if (person.getPlz() != null) {
            sb.append(person.getPlz()).append(" ");
        }

        if (person.getOrt() != null) {
            sb.append(person.getOrt());
        }

        return sb.toString();
    }

    private void set(
            PDAcroForm form,
            String fieldName,
            String value
    ) throws IOException {

        if (form == null) {
            return;
        }

        PDField field =
                form.getField(fieldName);

        if (field != null && value != null) {
            field.setValue(value);
        }
    }

    private String formatDate(
            LocalDate date
    ) {

        if (date == null) {
            return "";
        }

        return date.format(
                DateTimeFormatter.ofPattern(
                        "dd.MM.yyyy"
                )
        );
    }

    private String nullToEmpty(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
    private List<KostenZeile> buildKostenZeilen(
            Reisekostenabrechnung abrechnung,
            ReisekostenKonfiguration cfg
    ) {

        List<KostenZeile> result = new ArrayList<>();

    /* ==========================================
       Fahrer
       ========================================== */

        int fahrerKm =
                abrechnung.getFahrtabschnitte()
                        .stream()
                        .mapToInt(Fahrtabschnitt::getKilometer)
                        .sum();

        Person fahrer = abrechnung.getFahrer();

        result.add(
                new KostenZeile(
                        fahrer.getVorname() + " " + fahrer.getName(),
                        "F",
                        fahrerKm,
                        cfg.getPkwSatz(),
                        berechneBetrag(
                                fahrerKm,
                                cfg.getPkwSatz())
                )
        );

    /* ==========================================
       Mitfahrer-Kilometer aufsummieren
       ========================================== */

        Map<Person, Integer> mitfahrerKm =
                new LinkedHashMap<>();

        int anhaengerKm = 0;

        for (Fahrtabschnitt abschnitt :
                abrechnung.getFahrtabschnitte()) {

            for (FahrtabschnittMitfahrer mf :
                    abschnitt.getMitfahrer()) {

                Person p = mf.getPerson();

                mitfahrerKm.merge(
                        p,
                        abschnitt.getKilometer(),
                        Integer::sum
                );
            }

            if (abschnitt.isAnhaenger()) {
                anhaengerKm += abschnitt.getKilometer();
            }
        }

    /* ==========================================
       Mitfahrer
       ========================================== */

        mitfahrerKm.entrySet()
                .stream()
                .sorted(
                        Comparator.comparing(
                                e -> e.getKey().getName()
                        )
                )
                .forEach(entry -> {

                    Person p = entry.getKey();
                    int km = entry.getValue();

                    result.add(
                            new KostenZeile(
                                    p.getVorname()
                                            + " "
                                            + p.getName(),
                                    "MF",
                                    km,
                                    cfg.getMitfahrerSatz(),
                                    berechneBetrag(
                                            km,
                                            cfg.getMitfahrerSatz()
                                    )
                            )
                    );
                });

    /* ==========================================
       Anhänger
       ========================================== */

        if (anhaengerKm > 0) {

            result.add(
                    new KostenZeile(
                            "Anhänger",
                            "",
                            anhaengerKm,
                            cfg.getAnhaengerSatz(),
                            berechneBetrag(
                                    anhaengerKm,
                                    cfg.getAnhaengerSatz()
                            )
                    )
            );
        }

        return result;
    }
    private void fillKostenUebersicht(
            PDAcroForm form,
            Reisekostenabrechnung abrechnung,
            ReisekostenKonfiguration cfg
    ) throws IOException {

        List<KostenZeile> zeilen =
                buildKostenZeilen(abrechnung, cfg);

        if (zeilen.size() > 9) {
            throw new IllegalStateException(
                    "Zu viele Personen für das Fahrkostenformular"
            );
        }

        BigDecimal gesamt =
                BigDecimal.ZERO;

        int row = 1;

        for (KostenZeile z : zeilen) {

            if (row > 9) {
                break;
            }

            set(
                    form,
                    "Kilometer-PersonRow" + row,
                    z.getName()
            );

            set(
                    form,
                    "FRow" + row,
                    "F".equals(z.getRolle()) ? "x" : ""
            );

            set(
                    form,
                    "MFRow" + row,
                    "MF".equals(z.getRolle()) ? "x" : ""
            );

            set(
                    form,
                    "kmRow" + row + "_2",
                    String.valueOf(
                            z.getKilometer()
                    )
            );

            set(
                    form,
                    "EurkmRow" + row,
                    formatBetrag(z.getSatz())
            );

            set(
                    form,
                    "SummeEurRow" + row,
                    formatBetrag(z.getBetrag())
            );

            gesamt = gesamt.add(
                    z.getBetrag()
            );

            row++;
        }

        set(
                form,
                "SummeEurGesamt",
                formatEuro(gesamt)
        );
    }
    private BigDecimal berechneBetrag(
            int kilometer,
            BigDecimal satz
    ) {
        return BigDecimal.valueOf(kilometer)
                .multiply(satz)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String formatBetrag(BigDecimal wert) {

        if (wert == null) {
            return "";
        }

        return wert.setScale(2, RoundingMode.HALF_UP)
                .toPlainString()
                .replace('.', ',');
    }


}