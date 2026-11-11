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
    @org.springframework.beans.factory.annotation.Autowired
    private ReservierungsbestaetigungPdf reservierungsPdf;
    @org.springframework.beans.factory.annotation.Autowired private VorlagenService vorlagen;
    public static final String RESERVIERUNG_TEXT = """
            Ich danke für die Anmeldung und freue mich, dass Ihr unser Bootshaus in Obermaubach nutzen wollt.

            Die Reservierungsbestätigung mit weiteren Informationen findet Ihr im Anhang.

            Mit freundlichen Grüßen
            Hildegard Schog
            Bereichsleitung Bootshaus""";
    public record ReservierungVorschau(byte[] pdf, byte[] bild, String dateiname, String betreff, String text,
                                       String empfaenger, String cc, boolean testmodus, String fingerprint, @com.fasterxml.jackson.annotation.JsonIgnore String metadaten) {}
    public record ReservierungMail(JavaMailSender sender, jakarta.mail.internet.MimeMessage message, byte[] pdf, String metadaten) {}
    private record Routing(String empfaenger, String cc, boolean test) {}

    private Routing routing(Buchung buchung, String email) {
        var config = anbindungen.findById(buchung.getMietobjekt().getId()).orElse(null);
        String objectTest = config == null ? null : config.getAntwortTestEmail();
        boolean test = smtpConfig.isTestMode() || objectTest != null && !objectTest.isBlank();
        String recipient = objectTest == null || objectTest.isBlank() ? email : objectTest.trim();
        if (smtpConfig.isTestMode()) {
            try { recipient = smtpConfig.requireTestRecipient(); }
            catch (IllegalStateException ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage()); }
        }
        String cc = !smtpConfig.isTestMode() && config != null && config.getAntwortCcEmail() != null
                && !config.getAntwortCcEmail().isBlank() ? config.getAntwortCcEmail().trim() : null;
        return new Routing(recipient, cc, test);
    }

    @Transactional(readOnly = true, noRollbackFor = RuntimeException.class)
    public ReservierungVorschau reservierungVorschau(Buchung buchung) {
        var basis = erstelleMail(buchung, Buchungsstatus.RESERVIERT, Buchungsstatus.ANFRAGE);
        var modular = vorlagen == null ? null : reservierungsPdf.entwurf(buchung, null);
        var dokument = modular == null ? reservierungsPdf.erstellen(buchung) : new ReservierungsbestaetigungPdf.Dokument(modular.pdf(), "Reservierungsbestaetigung_" + buchung.getId() + ".pdf");
        var ziel = routing(buchung, basis.getEmpfaenger());
        return new ReservierungVorschau(dokument.pdf(), ReservierungsbestaetigungPdf.vorschaubild(dokument.pdf()), dokument.dateiname(),
                (ziel.test() ? "[TEST] " : "") + (modular == null ? buchung.getBuchungsnummer() + " Reservierungsbestätigung" : modular.betreff()),
                modular == null ? RESERVIERUNG_TEXT : modular.mailtext(), ziel.empfaenger(), ziel.cc(), ziel.test(),
                modular == null ? null : VorlagenService.hash(modular.fingerprint() + "|" + ziel + "|" + smtpConfig.getFrom() + "|" + basis.getReplyTo()), modular == null ? "" : modular.metadaten());
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public ReservierungMail reservierungVorbereiten(Buchung buchung) { return reservierungVorbereiten(buchung, null); }

    @Transactional(noRollbackFor = RuntimeException.class)
    public ReservierungMail reservierungVorbereiten(Buchung buchung, String fingerprint) {
        if (!smtpConfig.isEnabled()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Der direkte SMTP-Mailversand ist in KanuControl nicht aktiviert.");
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Der SMTP-Mailversand ist nicht verfügbar.");
        var v = reservierungVorschau(buchung);
        if (vorlagen != null && (fingerprint == null || !fingerprint.equals(v.fingerprint())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Buchung, Vorlage oder Empfänger wurden geändert. Bitte Vorschau neu laden und prüfen.");
        try {
            var message = sender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(smtpConfig.getFrom().trim()); helper.setTo(v.empfaenger());
            if (v.cc() != null) helper.setCc(v.cc());
            var config = anbindungen.findById(buchung.getMietobjekt().getId()).orElse(null);
            if (config != null && config.getEmpfaengerEmail() != null && !config.getEmpfaengerEmail().isBlank()) helper.setReplyTo(config.getEmpfaengerEmail().trim());
            helper.setSubject(v.betreff()); helper.setText(v.text(), false);
            helper.addAttachment(v.dateiname(), new org.springframework.core.io.ByteArrayResource(v.pdf()), "application/pdf");
            return new ReservierungMail(sender, message, v.pdf(),
                v.metadaten() + "; fingerprint=" + v.fingerprint() + "; betreff=" + v.betreff() + "; empfaenger=" + v.empfaenger() + "; cc=" + v.cc() + "; testmodus=" + v.testmodus());
        } catch (jakarta.mail.MessagingException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Die Reservierungsmail konnte nicht vorbereitet werden. Bitte Mailadressen prüfen.", e);
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public void reservierungSenden(ReservierungMail mail) {
        try { mail.sender().send(mail.message()); }
        catch (MailException e) {
            log.warn("SMTP-Versand der Reservierungsbestätigung fehlgeschlagen.", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Der SMTP-Server hat den Versand nicht bestätigt. Vor einer Wiederholung bitte den Postausgang prüfen.", e);
        }
    }

    public record StatusVorschau(String absender,String empfaenger,String cc,String replyTo,String betreff,String text,
                                 boolean testmodus,String fingerprint) {}
    public record StatusMail(JavaMailSender sender,SimpleMailMessage message) {}

    @Transactional(readOnly=true)
    public StatusVorschau statusVorschau(Buchung buchung,Buchungsstatus neuerStatus,Buchungsstatus alterStatus) {
        var mail=erstelleMail(buchung,neuerStatus,alterStatus);
        var ziel=routing(buchung,mail.getEmpfaenger());
        String from=smtpConfig.getFrom()==null?"":smtpConfig.getFrom().trim();
        String reply=mail.getReplyTo()==null?null:mail.getReplyTo().trim();
        String betreff=(ziel.test()?"[TEST] ":"")+mail.getBetreff();
        String fingerprint;
        try {
            var inhalt=java.util.Arrays.asList(com.kcserver.core.tenancy.TenantContext.getCurrentTenant(),buchung.getId(),
                    alterStatus,neuerStatus,from,ziel.empfaenger(),ziel.cc(),reply,betreff,mail.getText(),ziel.test());
            fingerprint=VorlagenService.hash(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(inhalt));
        } catch(com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException(e); }
        return new StatusVorschau(from,ziel.empfaenger(),ziel.cc(),reply,betreff,mail.getText(),ziel.test(),fingerprint);
    }

    /** Shared routing and MIME construction for all modular booking messages. */
    public StatusVorschau nachrichtVorschau(Buchung b,String betreff,String text){
        String email=b.getMieter()==null?null:b.getMieter().getEmail();
        if(email==null || !validator.validate(new Adresse(email)).isEmpty())throw new IllegalArgumentException("Eine gültige E-Mail-Adresse des Mieters ist erforderlich.");
        var ziel=routing(b,email.strip());var config=anbindungen.findById(b.getMietobjekt().getId()).orElse(null);
        String from=smtpConfig.getFrom()==null?"":smtpConfig.getFrom().strip();
        String reply=config==null?null:config.getEmpfaengerEmail();
        return new StatusVorschau(from,ziel.empfaenger(),ziel.cc(),reply,(ziel.test()&&!betreff.startsWith("[TEST] ")?"[TEST] ":"")+betreff,text,ziel.test(),null);
    }
    public ReservierungMail nachrichtVorbereiten(StatusVorschau v,byte[] pdf,String dateiname,String metadaten){
        if(!smtpConfig.isEnabled())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Der SMTP-Mailversand ist nicht aktiviert.");
        var sender=mailSenders.getIfAvailable();if(sender==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"SMTP ist nicht verfügbar.");
        try{
            var message=sender.createMimeMessage();var helper=new org.springframework.mail.javamail.MimeMessageHelper(message,true,"UTF-8");
            helper.setFrom(v.absender());helper.setTo(v.empfaenger());if(v.cc()!=null)helper.setCc(v.cc());
            if(v.replyTo()!=null && !v.replyTo().isBlank())helper.setReplyTo(v.replyTo());
            helper.setSubject(v.betreff());helper.setText(v.text(),false);
            if(pdf!=null)helper.addAttachment(dateiname,new org.springframework.core.io.ByteArrayResource(pdf),"application/pdf");
            return new ReservierungMail(sender,message,pdf,metadaten);
        }catch(jakarta.mail.MessagingException e){throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Die Nachricht konnte nicht vorbereitet werden. Bitte Mailadressen prüfen.",e);}
    }

    /** Build once while the booking is locked; send these exact contents after commit. */
    public StatusMail statusVorbereiten(Buchung buchung,Buchungsstatus neuerStatus,Buchungsstatus alterStatus,String fingerprint) {
        var v=statusVorschau(buchung,neuerStatus,alterStatus);
        if(fingerprint==null || !fingerprint.equals(v.fingerprint()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Buchung oder Mailinhalt wurden geändert. Bitte die E-Mail-Vorschau neu laden und prüfen.");
        if(!smtpConfig.isEnabled())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Der direkte SMTP-Mailversand ist in KanuControl nicht aktiviert.");
        var sender=mailSenders.getIfAvailable();
        if(sender==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Der SMTP-Mailversand ist nicht verfügbar.");
        var message=new SimpleMailMessage();message.setFrom(v.absender());message.setTo(v.empfaenger());
        if(v.cc()!=null)message.setCc(v.cc());if(v.replyTo()!=null && !v.replyTo().isBlank())message.setReplyTo(v.replyTo());
        message.setSubject(v.betreff());message.setText(v.text());
        return new StatusMail(sender,message);
    }

    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public void statusSenden(StatusMail mail) {
        try { mail.sender().send(mail.message()); }
        catch(MailException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Die Statusmail konnte nicht an den SMTP-Server übergeben werden. Der Buchungsstatus wurde gespeichert. Vor einer Wiederholung bitte den Postausgang prüfen.",e);
        }
    }

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

        var ziel = routing(buchung, mail.getEmpfaenger());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(smtpConfig.getFrom().trim());
        message.setTo(ziel.empfaenger());
        if (ziel.cc() != null) message.setCc(ziel.cc());
        message.setSubject((ziel.test() ? "[TEST] " : "") + mail.getBetreff());
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
