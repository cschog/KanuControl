package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.HomepageStatusMailDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.config.SmtpMailProperties;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service @Transactional @RequiredArgsConstructor
public class HomepageStatusMailService {
    private final HomepageStatusMailRepository mails;
    private final HomepageAnbindungRepository anbindungen;
    private final Validator validator;
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final SmtpMailProperties smtpConfig;
    private record Adresse(@NotBlank @Email String email) {}

    /** Legacy-Einreihung für bestehende Integrationsclients. */
    @Deprecated(forRemoval = false)
    public void vormerken(Buchung buchung, Buchungsstatus neuerStatus) {
        if (!"HOMEPAGE".equals(buchung.getHerkunft())) return;
        if (neuerStatus == null || buchung.getStatus() == neuerStatus) return;
        HomepageStatusMail mail = erstelleMail(buchung, neuerStatus, buchung.getStatus());
        if (mail != null) mails.save(mail);
    }

    /** Versendet die Statusmail synchron über den KC-seitig konfigurierten SMTP-Server. */
    public void senden(Buchung buchung, Buchungsstatus neuerStatus, Buchungsstatus alterStatus) {
        if (neuerStatus == null) return;
        HomepageStatusMail mail = erstelleMail(buchung, neuerStatus, alterStatus);
        if (mail == null) return;
        if (!smtpConfig.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Der direkte SMTP-Mailversand ist in KanuControl nicht aktiviert.");
        }
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Der SMTP-Mailversand ist nicht verfügbar. Bitte die Serverkonfiguration prüfen.");
        }

        String recipient = mail.getEmpfaenger();
        if (smtpConfig.isTestMode()) {
            try {
                recipient = smtpConfig.requireTestRecipient();
            } catch (IllegalStateException ex) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
            }
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(smtpConfig.getFrom().trim());
        message.setTo(recipient);
        message.setSubject((smtpConfig.isTestMode() ? "[TEST] " : "") + mail.getBetreff());
        message.setText(mail.getText());
        if (mail.getReplyTo() != null && !mail.getReplyTo().isBlank()) {
            message.setReplyTo(mail.getReplyTo().trim());
        }
        try {
            sender.send(message);
            log.info("KC-SMTP hat eine Buchungsstatusmail angenommen (Status {}).", neuerStatus);
        } catch (MailException ex) {
            log.warn("SMTP-Versand einer Buchungsstatusmail fehlgeschlagen.", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Die Statusmail konnte nicht an den SMTP-Server übergeben werden. Der Buchungsstatus wurde gespeichert.", ex);
        }
    }

    private HomepageStatusMail erstelleMail(Buchung buchung, Buchungsstatus neuerStatus,
                                             Buchungsstatus alterStatus) {
        String email = buchung.getMieter() == null ? null : buchung.getMieter().getEmail();
        if (email == null || !validator.validate(new Adresse(email)).isEmpty()) {
            throw new IllegalArgumentException("Für die Statusmail ist eine gültige E-Mail-Adresse des Mieters erforderlich.");
        }
        HomepageStatusMail mail = new HomepageStatusMail();
        mail.setMietobjektId(buchung.getMietobjekt().getId()); mail.setBuchungId(buchung.getId());
        mail.setBuchungsnummer(buchung.getBuchungsnummer()); mail.setStatus(neuerStatus.name());
        mail.setEmpfaenger(email.trim());
        anbindungen.findById(buchung.getMietobjekt().getId()).ifPresent(config -> mail.setReplyTo(config.getEmpfaengerEmail()));
        String nummer = buchung.getBuchungsnummer();
        String name = buchung.getMieter().getVorname();
        String anrede = name == null || name.isBlank() ? "Hallo," : "Hallo " + name.trim() + ",";
        DateTimeFormatter datum = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String zeitraum = "vom " + datum.format(buchung.getAnreise()) + (buchung.isUnbefristet()
                ? " auf unbestimmte Zeit" : " bis " + datum.format(buchung.getAbreise()));
        String objekt = buchung.getMietobjekt().getBezeichnung();
        String entscheidung;
        String bezug = "deine Buchung für " + objekt + " " + zeitraum;
        switch (neuerStatus) {
            case ANFRAGE -> {
                mail.setBetreff("Buchung " + nummer + " als Anfrage erfasst");
                entscheidung = "der Status deiner Buchung für " + objekt + " " + zeitraum
                        + " lautet jetzt Anfrage. Wir prüfen deine Angaben und melden uns bei dir.";
            }
            case RESERVIERT -> {
                mail.setBetreff("Buchungsanfrage " + nummer + " vorläufig reserviert");
                entscheidung = "wir haben deinen gewünschten Aufenthalt in " + objekt + " " + zeitraum
                        + " vorläufig reserviert. Dies ist noch keine endgültige Buchungsbestätigung.";
            }
            case BESTAETIGT -> {
                mail.setBetreff("Buchung " + nummer + " bestätigt");
                entscheidung = "wir bestätigen " + bezug + ".";
            }
            case ABGELEHNT -> {
                mail.setBetreff("Buchungsanfrage " + nummer + " abgelehnt");
                entscheidung = "leider können wir deine Anfrage für " + objekt + " " + zeitraum
                        + " nicht bestätigen. Deine Anfrage wurde abgelehnt.";
            }
            case STORNIERT -> {
                mail.setBetreff("Buchung " + nummer + " storniert");
                entscheidung = bezug + " wurde storniert.";
            }
            case ABRECHENBAR -> {
                mail.setBetreff("Buchung " + nummer + " zur Abrechnung vorgesehen");
                entscheidung = alterStatus == Buchungsstatus.ABGESCHLOSSEN
                        ? "die Abrechnung deiner Buchung für " + objekt + " " + zeitraum + " wurde erneut geöffnet."
                        : bezug + " ist jetzt zur Abrechnung vorgesehen. Die Abrechnung ist noch nicht abgeschlossen.";
            }
            case ABGESCHLOSSEN -> {
                mail.setBetreff("Buchung " + nummer + " abgerechnet");
                entscheidung = "die Abrechnung deiner Buchung für " + objekt + " " + zeitraum + " ist abgeschlossen.";
            }
            default -> throw new IllegalArgumentException("Unbekannter Buchungsstatus.");
        }
        mail.setText(anrede + "\n\n" + entscheidung + "\n\nBuchungsnummer: " + nummer + "\n\nViele Grüße\nDie Vermietung\n");
        mail.setCreatedAt(Instant.now());
        return mail;
    }

    private void pruefeZugang(Long objektId, String clientId) {
        HomepageAnbindung config = anbindungen.findById(objektId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Anbindung nicht freigegeben."));
        // Statusnachrichten müssen auch für inzwischen nicht mehr buchbare Objekte abrufbar bleiben.
        if (!config.isAktiv() || clientId == null || !clientId.equals(config.getTechnischerClientId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anbindung nicht freigegeben.");
    }
    @Transactional(readOnly = true)
    public List<HomepageStatusMailDTO> offen(Long objektId, String clientId) {
        pruefeZugang(objektId, clientId);
        return mails.findTop20ByMietobjektIdAndQuittiertAmIsNullOrderByCreatedAtAscIdAsc(objektId).stream()
                .map(m -> new HomepageStatusMailDTO(m.getId(), m.getBuchungsnummer(), m.getStatus(),
                        m.getEmpfaenger(), m.getReplyTo(), m.getBetreff(), m.getText(), m.getCreatedAt())).toList();
    }
    public void quittieren(Long objektId, String clientId, String nachrichtId) {
        pruefeZugang(objektId, clientId);
        HomepageStatusMail mail = mails.findByIdAndMietobjektId(nachrichtId, objektId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Nachricht nicht gefunden."));
        if (mail.getQuittiertAm() == null) mail.setQuittiertAm(Instant.now());
    }
}
