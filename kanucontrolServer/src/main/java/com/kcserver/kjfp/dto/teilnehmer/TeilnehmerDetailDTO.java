package com.kcserver.kjfp.dto.teilnehmer;

import com.kcserver.core.dto.person.PersonRefDTO;
import com.kcserver.kjfp.enumtype.Sex;
import com.kcserver.kjfp.enumtype.TeilnehmerRolle;
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
    private String countryCode;
    private Sex sex;
}