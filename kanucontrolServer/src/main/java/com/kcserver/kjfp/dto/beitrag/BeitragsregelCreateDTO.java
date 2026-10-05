package com.kcserver.kjfp.dto.beitrag;

import com.kcserver.kjfp.enumtype.TeilnehmerRolle;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BeitragsregelCreateDTO {


    private Integer alterBis;

    private TeilnehmerRolle rolle;

    private BigDecimal beitrag;
}