package com.kcserver.kjfp.dto.teilnehmer;

import com.kcserver.kjfp.enumtype.TeilnehmerRolle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeilnehmerUpdateDTO {

    private TeilnehmerRolle rolle;   // nur optional
}