package com.kcserver.kjfp.dto.beitrag;

import com.kcserver.kjfp.enumtype.TeilnehmerRolle;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BeitragsregelDTO {

    private Long id;
    private Integer alterVon;
    private Integer alterBis;
    private TeilnehmerRolle rolle;
    private BigDecimal beitrag;
}