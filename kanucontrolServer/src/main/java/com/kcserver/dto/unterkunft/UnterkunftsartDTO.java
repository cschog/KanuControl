package com.kcserver.dto.unterkunft;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UnterkunftsartDTO {

    private Long id;

    private String bezeichnung;

    private BigDecimal preisProPersonUndNacht;

    private String bemerkung;

    private Boolean aktiv;
}