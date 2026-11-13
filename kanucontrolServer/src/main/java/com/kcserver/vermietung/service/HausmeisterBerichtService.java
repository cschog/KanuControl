package com.kcserver.vermietung.service;

import com.kcserver.core.util.PersonName;
import com.kcserver.vermietung.config.SmtpMailProperties;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import com.kcserver.vermietung.repository.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HausmeisterBerichtService {
    static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private final HomepageAnbindungRepository configs;
    private final BuchungRepository buchungen;
    private final ObjectProvider<JavaMailSender> senders;
    private final SmtpMailProperties smtp;
    private final com.kcserver.vermietung.config.HausmeisterBerichtFreigabe freigabe;

    public java.util.Map<String, Object> status() {
        return java.util.Map.of("enabled", freigabe.isEnabled(), "environment", freigabe.getEnvironment(),
                "productionAllowed", freigabe.isProduction(), "smtpEnabled", smtp.isEnabled(), "testMode", smtp.isTestMode());
    }

    private boolean automatisch(HomepageAnbindung config) {
        return freigabe.isProduction() && (config.getHausmeisterAutomatisch() == null
                ? freigabe.isEnabled() : Boolean.TRUE.equals(config.getHausmeisterAutomatisch()));
    }

    @Transactional(readOnly = true)
    public List<Long> objektIds() {
        if (!freigabe.isProduction()) return List.of();
        return configs.findAll().stream().filter(this::automatisch).filter(c -> c.getHausmeisterEmail() != null
                && !c.getHausmeisterEmail().isBlank()).map(HomepageAnbindung::getMietobjektId).toList();
    }

    @Transactional
    public void wocheSenden(Long id, LocalDate woche) {
        if (!freigabe.isProduction() || !smtp.isEnabled()) return;
        var config = configs.lockById(id).orElse(null);
        if (config == null || !automatisch(config) || config.getHausmeisterEmail() == null || config.getHausmeisterEmail().isBlank()
                || woche.equals(config.getHausmeisterLetzteWoche())) return;
        senden(config, woche, false);
        config.setHausmeisterLetzteWoche(woche);
        configs.saveAndFlush(config);
    }

    @Transactional
    public void manuellSenden(Long id) {
        var config = configs.lockById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Bitte die Hausmeister-Empfängeradresse zuerst speichern."));
        if (config.getHausmeisterEmail() == null || config.getHausmeisterEmail().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bitte eine Hausmeister-Empfängeradresse speichern.");
        senden(config, LocalDate.now(ZONE), true);
    }

    private void senden(HomepageAnbindung config, LocalDate von, boolean manuell) {
        var sender = senders.getIfAvailable();
        if (!smtp.isEnabled() || sender == null)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Der KC-SMTP-Versand ist nicht aktiviert.");
        String recipient = smtp.isTestMode() ? smtp.requireTestRecipient() : config.getHausmeisterEmail();
        LocalDate bis = von.plusWeeks(4);
        var rows = buchungen.findByMietobjektIdAndStatusInAndAnreiseLessThanAndAbreiseGreaterThan(
                config.getMietobjektId(), List.of(Buchungsstatus.RESERVIERT, Buchungsstatus.BESTAETIGT,
                        Buchungsstatus.STORNIERT), bis.atStartOfDay(), von.atStartOfDay());
        rows.sort(Comparator.comparing(Buchung::getAnreise).thenComparing(Buchung::getId));
        String objekt = config.getMietobjekt().getBezeichnung();
        try {
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(smtp.getFrom().trim());
            helper.setTo(recipient.trim());
            if (!smtp.isTestMode() && config.getHausmeisterCcEmail() != null && !config.getHausmeisterCcEmail().isBlank())
                helper.setCc(config.getHausmeisterCcEmail().trim());
            helper.setSubject((smtp.isTestMode() ? "[TEST] " : "") + (manuell ? "[MANUELL] " : "") + "Hausmeister-Wochenbericht · " + objekt);
            var body = render(objekt, von, bis, rows);
            helper.setText(body[0], body[1]);
            sender.send(message);
        } catch (MessagingException | org.springframework.mail.MailException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Der Hausmeister-Bericht konnte nicht an den SMTP-Server übergeben werden.", ex);
        }
    }

    static String[] render(String objekt, LocalDate von, LocalDate bis, List<Buchung> rows) {
        var date = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        var time = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String heading = "Hausmeister-Wochenbericht · " + objekt;
        String period = date.format(von) + " bis " + date.format(bis.minusDays(1));
        StringBuilder plain = new StringBuilder(heading + "\n" + period + "\n\n");
        StringBuilder html = new StringBuilder("<!doctype html><html lang=\"de\"><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"></head><body style=\"margin:0;padding:16px;background:#f4f6f6;font:16px Arial,sans-serif;color:#20312f\"><div style=\"max-width:600px;margin:auto\"><h1 style=\"font-size:22px\">" + esc(heading) + "</h1><p>" + esc(period) + " · Europe/Berlin</p>");
        if (rows.isEmpty()) { plain.append("Keine passenden Buchungen.\n"); html.append("<p>Keine passenden Buchungen.</p>"); }
        for (var b : rows) {
            var person = b.getMieter();
            String name = person == null ? "Nicht angegeben" : (PersonName.givenFirst(person.getName(), person.getVorname())).trim();
            String phone = person == null ? "" : value(person.getTelefon());
            if (phone.isBlank() && person != null) phone = value(person.getTelefonFestnetz());
            String label = switch (b.getStatus()) { case RESERVIERT -> "Reserviert"; case BESTAETIGT -> "Bestätigt"; case STORNIERT -> "STORNIERT"; default -> b.getStatus().name(); };
            String zeitraum = time.format(b.getAnreise()) + " bis " + (b.isUnbefristet() ? "unbefristet" : time.format(b.getAbreise()));
            String nummer = value(b.getBuchungsnummer());
            String quelle = b.getBuchungsquelle() == Buchungsquelle.AIRBNB ? " · Airbnb" : "";
            plain.append(label).append(" · ").append(nummer).append(quelle).append('\n').append(zeitraum).append('\n').append(name);
            if (!phone.isBlank()) plain.append("\nTelefon: ").append(phone);
            plain.append("\n\n");
            html.append("<div style=\"background:white;padding:16px;margin:16px 0;border:1px solid #ccd5d3;border-radius:8px;overflow-wrap:anywhere\"><strong>")
                .append(esc(label)).append(" · ").append(esc(nummer)).append(quelle).append("</strong><p>").append(esc(zeitraum)).append("</p><p>").append(esc(name)).append("</p>");
            if (!phone.isBlank()) {
                html.append("<p>Telefon: ");
                String dial = phone.replaceAll("[^+0-9]", "");
                if (dial.isBlank()) html.append(esc(phone));
                else html.append("<a style=\"color:#176a62\" href=\"tel:").append(esc(dial)).append("\">").append(esc(phone)).append("</a>");
                html.append("</p>");
            }
            html.append("</div>");
        }
        String note = "Stand bei Erstellung. Stornierte Buchungen sind zur Information enthalten. Änderungen nach Erstellung sind noch nicht berücksichtigt.";
        plain.append(note); html.append("<p style=\"font-size:14px\">").append(note).append("</p></div></body></html>");
        return new String[]{plain.toString(), html.toString()};
    }
    private static String value(String s) { return s == null ? "" : s.trim(); }
    private static String esc(String s) { return HtmlUtils.htmlEscape(value(s)); }
}
