package com.kcserver.dto.teilnehmer;

import com.kcserver.dto.person.PersonRefDTO;
import com.kcserver.enumtype.BeitragsQuelle;
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

    private Integer alterBeiBeginn;

    /* =========================
       Beiträge
       ========================= */

    private BigDecimal individuellerBeitrag;

    private Boolean bezahlt;

    private LocalDate bezahltAm;

    private BigDecimal effektiverBeitrag;

    private BeitragsQuelle beitragsQuelle;
}