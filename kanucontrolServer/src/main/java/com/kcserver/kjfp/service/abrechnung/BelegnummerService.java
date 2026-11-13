package com.kcserver.kjfp.service.abrechnung;

import com.kcserver.kjfp.entity.FinanzGruppe;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Ein gemeinsamer Zähler je Konto für alle drei Dokumenttypen.
 * Die Zeilensperre des UPSERT gilt bis zum Ende der fachlichen Transaktion. */
@Service
@RequiredArgsConstructor
public class BelegnummerService {
    private final EntityManager entityManager;

    public record Nummer(int laufend, String wert) {}

    @Transactional
    public Nummer next(FinanzGruppe konto) {
        if (konto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bitte zuerst ein Konto mit Belegnummernkreis zuordnen.");
        }
        int next = ((Number) entityManager.createNativeQuery("""
                INSERT INTO belegnummer_zaehler (finanz_gruppe_id, letzter_wert)
                VALUES (:konto, 1)
                ON CONFLICT (finanz_gruppe_id) DO UPDATE
                SET letzter_wert = belegnummer_zaehler.letzter_wert + 1
                RETURNING letzter_wert
                """).setParameter("konto", konto.getId())
                .getSingleResult()).intValue();
        String wert = konto.getKuerzel() + "_" + String.format(java.util.Locale.ROOT, "%03d", next);
        entityManager.createNativeQuery("""
                INSERT INTO belegnummer_register (veranstaltung_id, belegnummer)
                VALUES (:veranstaltung, :nummer)
                """).setParameter("veranstaltung", konto.getVeranstaltung().getId())
                .setParameter("nummer", wert).executeUpdate();
        return new Nummer(next, wert);
    }

    public void validateKontowechsel(String nummer, FinanzGruppe bisher, FinanzGruppe neu) {
        if (nummer != null && (bisher == null || neu == null || !bisher.getId().equals(neu.getId()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ein bereits nummerierter Vorgang darf nicht auf ein anderes Konto wechseln.");
        }
    }
}
