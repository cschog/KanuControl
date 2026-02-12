package com.kcserver.dto.teilnehmer;

import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeilnehmerUpdateDTO {

    private TeilnehmerRolle rolle;   // nur optional
}