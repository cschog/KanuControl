package com.kcserver.dto.reisekosten;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KostenZeile {

    private String name;
    private String rolle;   // F oder MF
    private int kilometer;
    private BigDecimal satz;
    private BigDecimal betrag;
}
