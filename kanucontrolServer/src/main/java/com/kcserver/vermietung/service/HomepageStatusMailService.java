package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.HomepageStatusMailDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service @Transactional @RequiredArgsConstructor
public class HomepageStatusMailService {
    private final HomepageStatusMailRepository mails;
    private final HomepageAnbindungRepository anbindungen;
    private final Validator validator;
    private record Adresse(@NotBlank @Email String email) {}

    /** Wird innerhalb derselben Transaktion wie die Buchungsentscheidung aufgerufen. */
    public void vormerken(Buchung buchung, Buchungsstatus neuerStatus) {
        if (!"HOMEPAGE".equals(buchung.getHerkunft()) || neuerStatus == null || buchung.getStatus() == neuerStatus) return;
        String email = buchung.getMieter() == null ? null : buchung.getMieter().getEmail();
        if (email == null || !validator.validate(new Adresse(email)).isEmpty()) {
            throw new IllegalArgumentException("Für die Benachrichtigung zur Homepage-Anfrage ist eine gültige E-Mail-Adresse des Mieters erforderlich.");
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
                entscheidung = buchung.getStatus() == Buchungsstatus.ABGESCHLOSSEN
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
        mails.save(mail);
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
