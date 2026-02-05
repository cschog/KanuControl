package com.kcserver.dto.teilnehmer;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.Data;

@Data
public class TeilnehmerListDTO {

    private Long id;

    /* =========================
       Beziehungen
       ========================= */

    private Long personId;
    private PersonRefDTO person;

    /* =========================
       Rolle
       ========================= */

    /**
     * null = normaler Teilnehmer
     * LEITER / MITARBEITER = spezielle Rollen
     */
    private TeilnehmerRolle rolle;
}