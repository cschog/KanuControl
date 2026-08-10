package com.kcserver.dto.zahlungsnachweis;

import lombok.Data;

@Data
public class ZahlungsnachweisDokumentDTO {

    private Long id;

    private Integer reihenfolge;

    private String titel;

    private String originalDateiname;

    private String mimeType;

    private Long dateigroesse;
}