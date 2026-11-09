package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.BuchungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;

/** Preserves the committed status; archives the exact attachment before SMTP in a separate transaction. */
@Service @RequiredArgsConstructor
public class ReservierungsversandService {
    private final BuchungRepository buchungen;
    private final HomepageStatusMailService mails;
    @org.springframework.beans.factory.annotation.Autowired private VorlagenArchivService archiv;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String versenden(Long id, HomepageStatusMailService.ReservierungMail vorbereitet) { return versenden(id, vorbereitet, null); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String versenden(Long id, HomepageStatusMailService.ReservierungMail vorbereitet, String fingerprint) {
        Buchung b = buchungen.findLockedById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buchung nicht gefunden."));
        if (b.getStatus() != Buchungsstatus.RESERVIERT)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Buchung ist nicht mehr reserviert. Bitte neu laden.");
        if (b.getReservierungMailGesendetAm() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Die Reservierungsbestätigung wurde bereits an SMTP übergeben.");
        // A stale preview is a conflict, not an SMTP failure. Do not change delivery state.
        var mail = vorbereitet == null ? (fingerprint == null ? mails.reservierungVorbereiten(b) : mails.reservierungVorbereiten(b, fingerprint)) : vorbereitet;
        String archivId = null;
        try {
            if (archiv != null) archivId = archiv.beginnen(b, mail);
            mails.reservierungSenden(mail);
            b.setReservierungMailGesendetAm(Instant.now());
            b.setReservierungMailFehler(null);
            if (archivId != null) {
                try { archiv.ergebnis(archivId, true, null); }
                catch (RuntimeException e) {
                    // The PDF was already archived before SMTP. Never unlock another send
                    // because updating its result metadata failed after confirmed handoff.
                    b.setReservierungMailFehler("SMTP hat den Versand bestätigt. Die Archivstatus-Aktualisierung ist fehlgeschlagen; bitte prüfen, nicht erneut senden.");
                }
            }
            return null;
        } catch (RuntimeException e) {
            if (archivId != null) { try { archiv.ergebnis(archivId, false, e.getMessage()); } catch (RuntimeException archiveError) { e.addSuppressed(archiveError); } }
            String grund = e instanceof ResponseStatusException status ? status.getReason() : "Der SMTP-Mailversand ist fehlgeschlagen.";
            String fehler = "Die Buchung ist reserviert. Die Reservierungsbestätigung wurde nicht erfolgreich an SMTP übergeben. " + grund;
            b.setReservierungMailFehler(fehler.substring(0, Math.min(2000, fehler.length())));
            // Return rather than throw: retain the error in this transaction, report it after commit.
            return b.getReservierungMailFehler();
        }
    }
}
