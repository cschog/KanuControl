package com.kcserver.dto;

import com.kcserver.enumtype.TeilnehmerRolle;
import lombok.Data;

@Data
public class TeilnehmerDTO {

    private Long id;

    private Long veranstaltungId;
    private Long personId;

    private TeilnehmerRolle rolle;
}