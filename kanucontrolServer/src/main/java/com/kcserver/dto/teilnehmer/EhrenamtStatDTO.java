package com.kcserver.dto.teilnehmer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EhrenamtStatDTO {

    private int u16W;
    private int u16M;
    private int u16D;

    private int u18W;
    private int u18M;
    private int u18D;

    private int u27W;
    private int u27M;
    private int u27D;

    private int u45W;
    private int u45M;
    private int u45D;

    private int o45W;
    private int o45M;
    private int o45D;
}