package com.kcserver.kjfp.service.veranstaltung;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;

/** Einzige Positivliste für Speicherung und PDF. Keine Attribute oder externen Ressourcen. */
public final class Programmentwurf {
    public static final int MAX_TEXT = 50_000;
    public static final int MAX_BYTES = 500_000;
    private Programmentwurf() {}

    public static String titel(String value) {
        if (value == null) return null;
        if (value.codePointCount(0, value.length()) > 200 || value.contains("\n") || value.contains("\r"))
            throw invalid("Die Überschrift des Programmentwurfs muss einzeilig sein und darf höchstens 200 Zeichen enthalten.");
        return value.isBlank() ? null : value;
    }

    public static String inhalt(String value) {
        if (value == null) return null;
        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
            throw invalid("Der Programmentwurf ist technisch zu groß (maximal 500.000 UTF-8-Bytes).");
        Document source = Jsoup.parseBodyFragment(value);
        source.select("script,style,iframe,object,embed,svg,math,template").remove();
        source.select("b").tagName("strong");
        source.select("i").tagName("em");
        // Blockstruktur von Fremdinhalten erhalten, Attribute werden anschließend verworfen.
        source.select("div,h1,h2,h3,h4,h5,h6,blockquote,pre,tr,td,th").tagName("p");
        Document clean = new Cleaner(Safelist.none().addTags("p","br","strong","em","ul","ol","li")).clean(source);
        clean.outputSettings().prettyPrint(false);
        // Verschachtelte Listen auf eine Ebene bringen, ohne Einträge zu verlieren.
        for (Element nested : clean.select("li ul,li ol")) {
            Element item = nested.parent();
            Element after = item;
            for (Element child : nested.children().stream().toList()) { after.after(child); after = child; }
            nested.remove();
        }
        Document normalized = Jsoup.parseBodyFragment(clean.body().html());
        normalized.outputSettings().prettyPrint(false);
        String html = normalized.body().html();
        String text = sichtbarerText(html);
        if (text.codePointCount(0, text.length()) > MAX_TEXT)
            throw invalid("Der Programmtext darf höchstens 50.000 sichtbare Zeichen einschließlich Leerzeichen und Zeilenumbrüchen enthalten.");
        if (html.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
            throw invalid("Der bereinigte Programmentwurf ist technisch zu groß (maximal 500.000 UTF-8-Bytes).");
        return hatText(text) ? html : null;
    }

    public static String sichtbarerText(String html) {
        StringBuilder out = new StringBuilder();
        for (Node node : Jsoup.parseBodyFragment(html == null ? "" : html).body().childNodes()) text(node, out);
        if (!out.isEmpty() && out.charAt(out.length()-1) == '\n') out.setLength(out.length()-1);
        return out.toString();
    }
    private static void text(Node node, StringBuilder out) {
        if (node instanceof TextNode t) out.append(t.getWholeText().replace('\u00a0',' '));
        else if (node instanceof Element e) {
            if (e.tagName().equals("br")) out.append('\n');
            else {
                for (Node child : e.childNodes()) text(child, out);
                if (e.tagName().equals("p") || e.tagName().equals("li")) out.append('\n');
            }
        }
    }
    public static boolean hatText(String text) {
        return text.codePoints().anyMatch(c -> !Character.isWhitespace(c) && !Character.isSpaceChar(c) && Character.getType(c) != Character.FORMAT);
    }
    private static ResponseStatusException invalid(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
}
