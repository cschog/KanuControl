package com.kcserver.kjfp.repository.abrechnung;

import java.math.BigDecimal;

public interface TeilnehmerZahlungSumme {

    Long getTeilnehmerId();

    BigDecimal getGezahlterBetrag();
}