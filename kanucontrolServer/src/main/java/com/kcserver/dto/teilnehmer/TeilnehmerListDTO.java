package com.kcserver.dto.teilnehmer;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TeilnehmerListDTO {

    private Long id;

    private Long personId;

    private PersonRefDTO person;

    private TeilnehmerRolle rolle;

    /* =========================
       Beiträge
       ========================= */

    private BigDecimal individuellerBeitrag;

    private Boolean bezahlt;

    private LocalDate bezahltAm;

    private BigDecimal effektiverBeitrag;
}