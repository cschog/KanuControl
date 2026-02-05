package com.kcserver.dto.teilnehmer;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.Data;

@Data
public class TeilnehmerDetailDTO {

    private Long id;

    /* =========================
       Beziehungen
       ========================= */

    private Long veranstaltungId;
    private Long personId;

    private PersonRefDTO person;

    /* =========================
       Rolle
       ========================= */

    private TeilnehmerRolle rolle;
}