package com.kcserver.core.dto.teilnehmer;

import com.kcserver.core.enumtype.TeilnehmerRolle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeilnehmerUpdateDTO {

    private TeilnehmerRolle rolle;   // nur optional
}