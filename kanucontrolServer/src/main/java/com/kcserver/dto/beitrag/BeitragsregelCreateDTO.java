package com.kcserver.dto.beitrag;

import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BeitragsregelCreateDTO {

    private Integer alterVon;
    private Integer alterBis;

    private TeilnehmerRolle rolle;

    private BigDecimal beitrag;
}