package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.*;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.math.*;
import java.net.URI;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** One generator for the read-only preview and the actual attachment. */
@Service @RequiredArgsConstructor
public class ReservierungsbestaetigungPdf {
    // Fachlich vom Betreiber zugeordnet: EG = 1, OG = 2. Tenant-scoped repository.
    private static final long OG_ID = 2L;
    private final HomepageAnbindungRepository anbindungen;
    private final MietbereichRepository bereiche;
    @org.springframework.beans.factory.annotation.Autowired private VorlagenService vorlagen;
    public VorlagenService.Ergebnis entwurf(Buchung b, VorlagenModell.Vorlage entwurf) {
        return vorlagen.erstellen(b, inhalt(b, LocalDate.now(ZoneId.of("Europe/Berlin"))), entwurf);
    }
    public record Dokument(byte[] pdf, String dateiname) {}
    public record Inhalt(Map<String, String> felder, boolean ogGebucht, String hausregelnUrl) {}

    public Dokument erstellen(Buchung b) {
        Inhalt inhalt = inhalt(b, LocalDate.now(ZoneId.of("Europe/Berlin")));
        return new Dokument(vorlagen == null ? render(inhalt) : vorlagen.erstellen(b, inhalt, null).pdf(), "Reservierungsbestaetigung_"
                + b.getBuchungsnummer().replaceAll("[^a-zA-Z0-9_-]", "_") + ".pdf");
    }

    Inhalt inhalt(Buchung b, LocalDate heute) {
        if (b.getBuchungsquelle() != Buchungsquelle.DIREKT)
            throw invalid("Die Reservierungsbestätigung mit Zahlungsplan ist nur für Direktbuchungen freigegeben.");
        if (b.getAnreise() == null || b.getAbreise() == null || b.isUnbefristet())
            throw invalid("Für diese Reservierungsbestätigung werden Anreise und eine feste Abreise benötigt.");
        if (b.getMieter() == null || b.getMietobjekt() == null) throw invalid("Mieter und Mietobjekt fehlen.");
        var p = b.getMieter();
        var datum = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        var config = anbindungen.findById(b.getMietobjekt().getId()).orElseThrow(() -> invalid("Die Homepage-Integration mit der Hausregeln-URL fehlt."));
        String url = required(config.getHausregelnUrl(), "Hausregeln-URL in der Homepage-Integration");
        try {
            URI uri = URI.create(url);
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) { throw invalid("Die Hausregeln-URL muss eine gültige HTTP- oder HTTPS-Adresse sein."); }
        boolean alternativ = b.getBankverbindungAuswahl() == BankverbindungAuswahl.ALTERNATIV;
        var bank = alternativ ? b.getMietobjekt().getBankverbindungAlternativ() : b.getMietobjekt().getBankverbindungPrimaer();
        String bankLabel = alternativ ? "Alternative Bankverbindung" : "Primärbankverbindung";
        if (bank == null) throw invalid(bankLabel + " fehlt am Mietobjekt.");
        var og = bereiche.findById(OG_ID).orElseThrow(() -> invalid("Der zugeordnete OG-Mietbereich (ID 2) fehlt."));
        if (og.getMietobjekt() == null || !Objects.equals(og.getMietobjekt().getId(), b.getMietobjekt().getId()))
            throw invalid("Für dieses Mietobjekt ist die OG-Zuordnung der Reservierungsvorlage nicht eingerichtet (OG-ID 2).");
        if (b.getMietbereiche().isEmpty()) throw invalid("Es sind keine Mietbereiche gebucht.");
        for (var position : b.getMietbereiche()) {
            if (position.getVereinbarterPreis() == null || position.getAnzahl() == null || position.getAnzahl() < 1
                    || position.getBerechnungsart() == null || position.getMietbereich() == null)
                throw invalid("Ein gebuchter Mietbereich enthält unvollständige Preisangaben.");
        }
        BigDecimal miete = BuchungPreiskalkulation.miete(b);
        BigDecimal anzahlung = amount(b.getAnzahlungBetrag(), "Anzahlung");
        BigDecimal kaution = amount(b.getKautionBetrag(), "Kaution");
        if (anzahlung.compareTo(miete) > 0) throw invalid("Die vereinbarte Anzahlung übersteigt den Mietbetrag. Bitte den Zahlungsplan korrigieren.");
        String tarif = b.getRabattart() == Rabattart.VEREIN ? " für dich als EKC-Mitglied"
                : b.getRabattart() == Rabattart.VERBAND ? " für dich als DKV-Mitglied" : "";
        var f = new LinkedHashMap<String, String>();
        f.put("empfaenger_name", required(p.getVorname(), "Vorname des Mieters") + " " + required(p.getName(), "Nachname des Mieters"));
        f.put("empfaenger_strasse", required(p.getStrasse(), "Straße des Mieters"));
        f.put("empfaenger_ort", required(p.getPlz(), "Postleitzahl des Mieters") + " " + required(p.getOrt(), "Wohnort des Mieters")
                + (p.getCountryCode() == null || "DE".equals(p.getCountryCode().name()) ? "" : "\n" + p.getCountryCode().name()));
        f.put("briefdatum", datum.format(heute));
        f.put("buchungsnummer", required(b.getBuchungsnummer(), "Buchungsnummer"));
        f.put("anrede", "Hallo " + p.getVorname().trim() + ",");
        f.put("aufenthalt", "ich danke für die Anmeldung und freue mich, dass ihr vom " + datum.format(b.getAnreise())
                + " bis " + datum.format(b.getAbreise()) + " unser Bootshaus in Obermaubach nutzen wollt.");
        f.put("mietpreissatz", "Die Miete beträgt" + tarif + " " + money(miete) + " €.");
        f.put("anzahlung_text", "Der gewünschte Termin ist für euch reserviert. Mit dieser Reservierungsbestätigung wird eine Anzahlung von " + money(anzahlung) + " € fällig.");
        f.put("restzahlung_text", "Die Restzahlung von " + money(miete.subtract(anzahlung)) + " € und die Kaution von " + money(kaution)
                + " € müssen spätestens bis zum " + datum.format(b.getAnreise().toLocalDate().minusDays(7)) + " überwiesen werden.");
        f.put("stornierung_text", b.getAnreise().toLocalDate().isBefore(heute.plusWeeks(8))
                ? "Da die Buchung in weniger als 8 Wochen beginnt, können wir geleistete Anzahlungen bei Stornierung nicht mehr zurückzahlen."
                : "Abmeldungen und Änderungen der Anmeldungen müssen mindestens 8 Wochen vor dem angemeldeten Termin schriftlich erfolgen. Andernfalls können wir die geleistete Anzahlung nicht erstatten, da uns Kosten und Mietausfall entstehen.");
        f.put("bank_name", "Bank: " + required(bank.getBankName(), bankLabel + ": Bankname"));
        f.put("bank_iban", "IBAN: " + required(bank.getIban(), bankLabel + ": IBAN"));
        f.put("bank_bic", "BIC: " + required(bank.getBic(), bankLabel + ": BIC"));
        boolean ogGebucht = b.getMietbereiche().stream().anyMatch(pos -> enthaeltOg(pos.getMietbereich(), new HashSet<>()));
        return new Inhalt(Collections.unmodifiableMap(f), ogGebucht, url);
    }

    private static boolean enthaeltOg(Mietbereich bereich, Set<Long> besucht) {
        if (bereich == null || bereich.getId() == null || !besucht.add(bereich.getId())) return false;
        return bereich.getId() == OG_ID || bereich.getBelegteMietbereiche().stream().anyMatch(b -> enthaeltOg(b, besucht));
    }
    private static String required(String value, String label) {
        if (value == null || value.isBlank()) throw invalid(label + " fehlt.");
        return value.trim().replaceAll("[\\p{Cntrl}]", " ");
    }
    private static BigDecimal amount(BigDecimal value, String label) {
        if (value == null || value.signum() < 0) throw invalid(label + " fehlt oder ist negativ.");
        return value;
    }
    private static String money(BigDecimal amount) {
        var format = NumberFormat.getNumberInstance(Locale.GERMANY);
        format.setMinimumFractionDigits(2); format.setMaximumFractionDigits(2);
        format.setRoundingMode(RoundingMode.HALF_UP); return format.format(amount);
    }
    private static ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }

    static byte[] render(Inhalt inhalt) {
        try (var source = ReservierungsbestaetigungPdf.class.getResourceAsStream("/pdf/Reservierungsbestaetigung.pdf");
             var fontSource = PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");
             var doc = Loader.loadPDF(Objects.requireNonNull(source).readAllBytes());
             var out = new ByteArrayOutputStream()) {
            var form = Objects.requireNonNull(doc.getDocumentCatalog().getAcroForm());
            var font = PDType0Font.load(doc, Objects.requireNonNull(fontSource), false);
            form.getDefaultResources().put(COSName.getPDFName("KC"), font);
            form.setNeedAppearances(false);
            if (form.getFields().size() != 16) throw new IOException("Unerwartete Formularfelder in der Vorlage.");
            for (var entry : inhalt.felder().entrySet()) {
                if (!(form.getField(entry.getKey()) instanceof PDTextField field)) throw new IOException("Formularfeld fehlt: " + entry.getKey());
                var rect = field.getWidgets().getFirst().getRectangle();
                float size = entry.getKey().startsWith("bank_") ? 8 : entry.getKey().equals("restzahlung_text") ? 9.5f : 10;
                String text = entry.getValue();
                while (size >= 7) {
                    String fitted = field.isMultiline() ? wrap(text, font, size, rect.getWidth() - 4) : text;
                    float widest = 0; for (String line : fitted.split("\n", -1)) widest = Math.max(widest, font.getStringWidth(line) * size / 1000);
                    if (widest <= rect.getWidth() - 4 && (!field.isMultiline() || fitted.split("\n", -1).length * size * 1.2 <= rect.getHeight() - 1)) {
                        field.setDefaultAppearance("/KC " + size + " Tf 0 g"); field.setValue(fitted); textAppearance(doc, field, font, size, fitted); break;
                    }
                    size -= .5f;
                }
                if (size < 7) throw invalid("Der Inhalt für „" + entry.getKey() + "“ ist zu lang für die PDF-Vorlage.");
            }
            var first = (PDCheckBox) form.getField("og_nicht_genutzt");
            var second = (PDCheckBox) form.getField("og_zusaetzlich");
            if (inhalt.ogGebucht()) { first.check(); second.unCheck(); } else { first.unCheck(); second.check(); }
            checkboxAppearance(doc, first, inhalt.ogGebucht());
            checkboxAppearance(doc, second, !inhalt.ogGebucht());
            form.flatten(); doc.getDocumentCatalog().setAcroForm(null);
            var link = new PDAnnotationLink();
            link.setRectangle(new PDRectangle(194.04f, doc.getPage(0).getMediaBox().getHeight() - 561, 51.13f, 13));
            var border = new PDBorderStyleDictionary(); border.setWidth(0); link.setBorderStyle(border);
            var action = new PDActionURI(); action.setURI(inhalt.hausregelnUrl()); link.setAction(action);
            doc.getPage(0).getAnnotations().add(link);
            doc.save(out); return out.toByteArray();
        } catch (IOException | IllegalArgumentException e) {
            throw invalid("Die Reservierungsbestätigung konnte nicht erstellt werden. Bitte Vorlage und Sonderzeichen in den Angaben prüfen.");
        }
    }
    // Explicit appearances keep line spacing predictable across PDF viewers and the flattened output.
    static void textAppearance(PDDocument doc, PDTextField field, PDType0Font font, float size, String text) throws IOException {
        var widget = field.getWidgets().getFirst();
        var rect = widget.getRectangle();
        var appearance = new PDAppearanceStream(doc);
        appearance.setBBox(new PDRectangle(rect.getWidth(), rect.getHeight()));
        appearance.setResources(new PDResources());
        try (var stream = new PDPageContentStream(doc, appearance)) {
            stream.beginText(); stream.setFont(font, size); stream.setLeading(size * 1.2f);
            stream.newLineAtOffset(1, rect.getHeight() - size);
            boolean first = true;
            for (String line : text.split("\n", -1)) {
                if (!first) stream.newLine();
                stream.showText(line); first = false;
            }
            stream.endText();
        }
        var dictionary = new PDAppearanceDictionary(); dictionary.setNormalAppearance(appearance); widget.setAppearance(dictionary);
    }
    private static void checkboxAppearance(PDDocument doc, PDCheckBox field, boolean checked) throws IOException {
        var widget = field.getWidgets().getFirst(); var rect = widget.getRectangle();
        var appearance = new PDAppearanceStream(doc);
        appearance.setBBox(new PDRectangle(rect.getWidth(), rect.getHeight()));
        appearance.setResources(new PDResources());
        try (var stream = new PDPageContentStream(doc, appearance)) {
            stream.setLineWidth(.6f); stream.addRect(.5f, .5f, rect.getWidth() - 1, rect.getHeight() - 1); stream.stroke();
            if (checked) {
                stream.setLineWidth(1); stream.moveTo(2, 5); stream.lineTo(4, 2); stream.lineTo(7, 8); stream.stroke();
            }
        }
        var dictionary = new PDAppearanceDictionary(); dictionary.setNormalAppearance(appearance); widget.setAppearance(dictionary);
    }
    public static byte[] vorschaubild(byte[] pdf) {
        try (var doc = Loader.loadPDF(pdf); var out = new ByteArrayOutputStream()) {
            var image = new org.apache.pdfbox.rendering.PDFRenderer(doc).renderImageWithDPI(0, 120);
            javax.imageio.ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) { throw invalid("Das Vorschaubild konnte nicht erstellt werden."); }
    }
    private static String wrap(String text, PDType0Font font, float size, float width) throws IOException {
        var lines = new ArrayList<String>();
        for (String paragraph : text.split("\n", -1)) {
            String line = "";
            for (String word : paragraph.split(" +")) {
                String next = line.isEmpty() ? word : line + " " + word;
                if (!line.isEmpty() && font.getStringWidth(next) * size / 1000 > width) { lines.add(line); line = word; }
                else line = next;
            }
            lines.add(line);
        }
        return String.join("\n", lines);
    }
}
