package com.kcserver.core.dto.teilnehmer;

import com.kcserver.core.enumtype.TeilnehmerRolle;
import lombok.Data;

@Data
public class RolleDTO {

    private TeilnehmerRolle rolle; // null oder MITARBEITER
}