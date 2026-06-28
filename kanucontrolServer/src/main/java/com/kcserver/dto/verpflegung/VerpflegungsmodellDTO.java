package com.kcserver.dto.verpflegung;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VerpflegungsmodellDTO {

    private Long id;

    private String bezeichnung;

    private BigDecimal preisProPersonUndTag;

    private String bemerkung;

    private boolean aktiv;
}
