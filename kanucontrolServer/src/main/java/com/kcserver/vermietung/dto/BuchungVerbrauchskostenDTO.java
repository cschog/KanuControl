package com.kcserver.vermietung.dto;

import com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BuchungVerbrauchskostenDTO {

    private Long id;

    private Long verbrauchskostenId;

    private String bezeichnung;

    private Verbrauchsart verbrauchsart;

    private Verbrauchsabrechnungsart abrechnungsart;

    private String mengeneinheit;

    private BigDecimal preis;

    private BigDecimal anfangsstand;

    private BigDecimal endstand;

    private BigDecimal verbrauch;

    private LocalDateTime anfangsablesung;

    private LocalDateTime endablesung;

    private Integer abrechnungstage;

    private BigDecimal kosten;
}