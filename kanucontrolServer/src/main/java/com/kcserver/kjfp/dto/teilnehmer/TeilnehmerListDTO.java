package com.kcserver.kjfp.dto.teilnehmer;

import com.kcserver.core.dto.person.PersonRefDTO;
import com.kcserver.kjfp.enumtype.BeitragsQuelle;
import com.kcserver.kjfp.enumtype.TeilnehmerRolle;
import com.kcserver.kjfp.enumtype.Zahlungsstatus;
import lombok.Data;
import java.math.BigDecimal;

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

    private BeitragsQuelle beitragsQuelle;

    private BigDecimal sollBeitrag;
    private BigDecimal gezahlterBetrag;
    private Zahlungsstatus zahlungsstatus;


}