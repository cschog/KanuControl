package com.kcserver.dto.teilnehmer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeilnehmerRefDTO {

    private Long personId;
    private String vorname;
    private String name;
    private String hauptvereinAbk;
}