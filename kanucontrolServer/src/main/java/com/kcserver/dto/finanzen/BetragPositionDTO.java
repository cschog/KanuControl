// dto/finanzen/BetragPositionDTO.java

package com.kcserver.dto.finanzen;

import java.math.BigDecimal;

public class BetragPositionDTO {

    private String name;

    private BigDecimal betrag;

    public BetragPositionDTO() {
    }

    public BetragPositionDTO(String name, BigDecimal betrag) {
        this.name = name;
        this.betrag = betrag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBetrag() {
        return betrag;
    }

    public void setBetrag(BigDecimal betrag) {
        this.betrag = betrag;
    }
}