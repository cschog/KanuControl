package com.kcserver.dto.teilnehmer;

import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.Data;

@Data
public class RolleDTO {

    private TeilnehmerRolle rolle; // null oder MITARBEITER
}