package com.kcserver.dto;

import com.kcserver.entity.TeilnehmerRolle;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeilnehmerDTO {

    private Long id;

    private Long veranstaltungId;
    private Long personId;

    private TeilnehmerRolle rolle;   // LEITER, TEILNEHMER, MITARBEITER
}