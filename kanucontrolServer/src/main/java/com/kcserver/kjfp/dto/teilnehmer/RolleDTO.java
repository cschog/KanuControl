package com.kcserver.kjfp.dto.teilnehmer;

import com.kcserver.kjfp.enumtype.TeilnehmerRolle;
import lombok.Data;

@Data
public class RolleDTO {

    private TeilnehmerRolle rolle; // null oder MITARBEITER
}