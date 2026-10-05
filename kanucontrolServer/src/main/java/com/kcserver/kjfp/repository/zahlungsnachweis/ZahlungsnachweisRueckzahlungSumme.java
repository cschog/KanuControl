package com.kcserver.kjfp.repository.zahlungsnachweis;

import java.math.BigDecimal;

public interface ZahlungsnachweisRueckzahlungSumme {

    Long getZahlungsnachweisId();

    BigDecimal getZurueckgezahlt();
}