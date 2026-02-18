package com.kcserver.dto.teilnehmer;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.Data;

import java.time.LocalDate;

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

    private LocalDate geburtsdatum;
    private String plz;
    private Sex sex;
}