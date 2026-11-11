package com.kcserver.vermietung.service;

import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.vermietung.exception.BuchungLoeschbestaetigungException;
import com.kcserver.vermietung.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class BuchungLoeschService {
    private final BuchungRepository buchungen;
    private final VermietungAbrechnungRepository abrechnungen;
    private final VorlagenVersandRepository versandarchiv;
    private final HomepageStatusMailRepository statusMails;

    public void delete(Long id, boolean archivierteAbrechnungenBestaetigt) {
        // Dokumenterstellung und Statuswechsel sperren dieselbe Buchung.
        var buchung = buchungen.findLockedById(id).orElseThrow(() ->
                new IllegalArgumentException(String.format(Locale.GERMAN, ErrorMessages.BUCHUNG_NOT_FOUND, id)));
        long anzahl = abrechnungen.countByBuchungId(id)
                + versandarchiv.countByBuchungIdAndVorgang(id, "ABGERECHNET");
        if (anzahl > 0 && !archivierteAbrechnungenBestaetigt) {
            throw new BuchungLoeschbestaetigungException(anzahl);
        }

        // Bulk-Löschungen entfernen auch große PDF-Archive ohne sie zu laden.
        abrechnungen.deleteAllByBuchungId(id);
        versandarchiv.deleteAllByBuchungId(id);
        statusMails.deleteAllByBuchungId(id);
        // Mietbereiche/Entschädigungen per JPA, Verbrauchskosten/Finanzbewegungen/
        // Nachrichtenentwürfe per Datenbank-Cascade; Stammdaten bleiben erhalten.
        buchungen.delete(buchung);
        buchungen.flush();
    }
}
