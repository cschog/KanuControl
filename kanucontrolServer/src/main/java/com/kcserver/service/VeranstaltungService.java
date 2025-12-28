package com.kcserver.service;

import com.kcserver.dto.VeranstaltungDTO;

import java.util.List;

public interface VeranstaltungService {

    /* =========================================================
       CREATE
       ========================================================= */

    /**
     * Legt eine neue Veranstaltung an.
     * - genau ein veranstaltender Verein
     * - genau ein Leiter (>= 18 Jahre)
     * - Veranstaltung wird automatisch aktiv
     * - Leiter wird automatisch als Teilnehmer (LEITER) angelegt
     */
    VeranstaltungDTO create(VeranstaltungDTO dto);

    /* =========================================================
       READ
       ========================================================= */

    /**
     * Liefert alle Veranstaltungen (Historie).
     */
    List<VeranstaltungDTO> getAll();

    /**
     * Liefert die aktuell aktive Veranstaltung.
     * @throws 404 wenn keine aktiv ist
     */
    VeranstaltungDTO getActive();

    /**
     * Liefert eine Veranstaltung per ID.
     */
    VeranstaltungDTO getById(Long id);

    /* =========================================================
       STATE MANAGEMENT
       ========================================================= */

    /**
     * Beendet die aktuell aktive Veranstaltung.
     */
    void beenden();

    /**
     * Aktiviert eine bestehende Veranstaltung.
     * - es darf keine andere aktive geben
     */
    void aktivieren(Long id);
}