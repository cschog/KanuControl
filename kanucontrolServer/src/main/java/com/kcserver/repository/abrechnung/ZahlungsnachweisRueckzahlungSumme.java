package com.kcserver.repository.abrechnung;

import java.math.BigDecimal;

public interface ZahlungsnachweisRueckzahlungSumme {

    Long getZahlungsnachweisId();

    BigDecimal getZurueckgezahlt();
}