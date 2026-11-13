package com.kcserver.vermietung.service;

import java.util.*;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** No expression evaluator: templates can only use this explicit contract. */
public final class VorlagenModell {
    private VorlagenModell() {}
    public enum Vorgang { RESERVIERUNG, BESTAETIGUNG, STORNIERUNG, ABLEHNUNG, ABRECHENBAR, ABGERECHNET }
    public enum Bedingung { IMMER, ANZAHLUNG_POSITIV, KAUTION_POSITIV, RESTMIETE_POSITIV,
        FRUEHERE_BUCHUNGSNUMMER_VORHANDEN, OG_GEBUCHT, OG_NICHT_GEBUCHT, STORNO_UNTER_56_TAGEN, STORNO_AB_56_TAGEN,
        ZAHLUNG_OHNE_ABRECHNUNG, ZAHLUNG_ANZAHLUNG, ZAHLUNG_BEI_ABRECHNUNG, ZAHLUNG_GESAMTMIETE_VORAUS,
        RESTZAHLUNG_POSITIV, ERSTATTUNG_POSITIV, AUSGEGLICHEN, STORNO_ERSTATTUNG_POSITIV, STORNO_EINBEHALT_POSITIV, STORNO_OHNE_ZAHLUNG }
    public record Variante(Bedingung bedingung, String text) {}
    public record Block(String titel, String text, Bedingung bedingung, List<Variante> varianten) {}
    public record Vorlage(Long version, String betreff, String mailtext, List<Block> bloecke, String anrede) {
        public Vorlage { anrede = anrede == null ? "Hallo {{vorname}}," : anrede; }
        public Vorlage(Long version, String betreff, String mailtext, List<Block> bloecke) {
            this(version, betreff, mailtext, bloecke, null);
        }
    }
    public record Briefpapier(Long version, String gruss, boolean pdfVorhanden, boolean bildVorhanden) {}
    public static final Set<String> FELDER = Set.of("buchungsnummer", "fruehere_buchungsnummer", "vorname", "nachname", "anreise", "abreise", "mietbetrag",
        "anzahlung", "kaution", "restmiete", "zahlungsfrist", "briefdatum", "mietobjekt", "bank_name", "bank_iban", "bank_bic", "hausregeln_url", "abrechnungssumme", "restzahlung", "erstattung", "saldo",
        "organisation", "mieter_bank_name", "mieter_bank_iban", "mieter_bank_bic", "mieter_kontoinhaber", "stornodatum", "storno_gezahlter_betrag", "storno_bereits_erstattet", "storno_erstattung", "storno_einbehalt");
    private static final Pattern TOKEN = Pattern.compile("\\{\\{([a-z_]+)}}");
    public static ResponseStatusException fehler(String text) { return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, text); }
    /** Multipart form fields can use CRLF even when the textarea contains LF. */
    public static String normalisieren(String text) {
        return text == null ? null : text.replace("\r\n", "\n").replace('\r', '\n');
    }
    public static Vorlage normalisieren(Vorlage v) {
        if (v == null) return null;
        // Keep single-line fields unchanged so validation still rejects header/title breaks.
        return new Vorlage(v.version(), v.betreff(), normalisieren(v.mailtext()), v.bloecke() == null ? null :
            v.bloecke().stream().map(b -> b == null ? null : new Block(b.titel(), normalisieren(b.text()), b.bedingung(),
                b.varianten() == null ? null : b.varianten().stream().map(x -> x == null ? null : new Variante(x.bedingung(), normalisieren(x.text()))).toList())).toList(), normalisieren(v.anrede()));
    }
    public static void pruefeText(String text, boolean leerErlaubt) {
        text = normalisieren(text);
        if (text == null || (!leerErlaubt && text.isBlank()) || text.length() > 12000) throw fehler("Text fehlt oder überschreitet 12000 Zeichen.");
        var matcher = TOKEN.matcher(text);
        while (matcher.find()) if (!FELDER.contains(matcher.group(1))) throw fehler("Unbekannter Platzhalter: " + matcher.group());
        String rest = matcher.replaceAll("");
        if (rest.contains("{{") || rest.contains("}}")) throw fehler("Ungültiger Platzhalter. Bitte aus dem Feldkatalog auswählen.");
        if (text.codePoints().anyMatch(c -> Character.isISOControl(c) && c != '\n' && c != '\t')) throw fehler("Unerlaubtes Steuerzeichen im Text.");
    }
    public static void pruefe(Vorlage v) {
        if (v == null) throw fehler("Vorlage fehlt.");
        pruefeText(v.betreff(), false); pruefeText(v.mailtext(), false); pruefeText(v.anrede(), true);
        if (v.anrede().contains("\n") || v.anrede().contains("\r") || v.anrede().length() > 250) throw fehler("Die PDF-Anrede muss einzeilig und höchstens 250 Zeichen lang sein.");
        if (v.betreff().contains("\n") || v.betreff().contains("\r") || v.betreff().length() > 250) throw fehler("Der Betreff muss einzeilig und höchstens 250 Zeichen lang sein.");
        if (v.bloecke() == null || v.bloecke().isEmpty() || v.bloecke().size() > 5) throw fehler("Eine Vorlage benötigt ein bis fünf Textblöcke.");
        for (Block b : v.bloecke()) {
            if (b == null || b.bedingung() == null) throw fehler("Block oder Bedingung fehlt.");
            pruefeText(b.titel(), false); pruefeText(b.text(), true);
            if (b.titel().length() > 120 || b.titel().contains("\n") || b.titel().contains("\r")) throw fehler("Blocktitel muss einzeilig und höchstens 120 Zeichen lang sein.");
            if (b.varianten() != null) {
                if (b.varianten().size() > 8) throw fehler("Zu viele Textvarianten.");
                var seen = new HashSet<Bedingung>();
                for (Variante x : b.varianten()) {
                    if (x == null || x.bedingung() == null || !seen.add(x.bedingung())) throw fehler("Ungültige oder doppelte Variantenbedingung.");
                    pruefeText(x.text(), false);
                }
            }
        }
    }
    public static String ersetzen(String text, Map<String,String> daten) {
        text = normalisieren(text);
        pruefeText(text, true);
        String first = daten.get("vorname");
        boolean platzhalterVorname = first != null && first.strip().matches("_+");
        if (platzhalterVorname) {
            text = text.replaceAll("\\{\\{nachname\\}\\}\\s*,\\s*\\{\\{vorname\\}\\}", "{{nachname}}")
                    .replaceAll("\\{\\{vorname\\}\\}\\s*,?\\s*\\{\\{nachname\\}\\}", "{{nachname}}");
        }
        // Optional legacy-number sentences must disappear as a whole when there is no value.
        if (Objects.toString(daten.get("fruehere_buchungsnummer"), "").isBlank() && text.contains("{{fruehere_buchungsnummer}}")) {
            text = text.replaceAll("(?m)[^.!?\\n]*\\{\\{fruehere_buchungsnummer}}[^.!?\\n]*[.!?]?", "").strip();
        }
        var matcher = TOKEN.matcher(text); var out = new StringBuilder();
        while (matcher.find()) {
            String value = platzhalterVorname && matcher.group(1).equals("vorname")
                    ? daten.get("nachname") : daten.get(matcher.group(1));
            if (matcher.group(1).equals("fruehere_buchungsnummer") && (value==null || value.isBlank())) {
                matcher.appendReplacement(out, ""); continue;
            }
            if (value == null || value.isBlank()) throw fehler("Pflichtwert für " + matcher.group() + " fehlt.");
            matcher.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(value));
        }
        matcher.appendTail(out); return out.toString();
    }
    public static List<String> texte(Vorlage v, Map<String,String> daten, Set<Bedingung> wahr) {
        pruefe(v); var result = new ArrayList<String>();
        for (Block b : v.bloecke()) if (wahr.contains(b.bedingung())) {
            String text = ersetzen(b.text(), daten);
            if (b.varianten() != null) for (Variante x : b.varianten()) if (wahr.contains(x.bedingung())) text += "\n" + ersetzen(x.text(), daten);
            // Titel dienen ausschließlich der Orientierung im Vorlageneditor.
            if (!text.isBlank()) result.add(text.strip());
        }
        return result;
    }
}
