package com.kcserver.dto.teilnehmer;

import lombok.Data;

import java.util.List;

@Data
public class TeilnehmerBeitraegeResponseDTO {

    private List<TeilnehmerListDTO> teilnehmer;
}