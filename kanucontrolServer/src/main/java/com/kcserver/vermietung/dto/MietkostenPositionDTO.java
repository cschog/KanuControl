package com.kcserver.vermietung.dto;
import java.math.BigDecimal;
public record MietkostenPositionDTO(String bezeichnung, Integer anzahl, BigDecimal einzelpreis, BigDecimal gesamtbetrag, long abrechnungseinheiten, String berechnungsart) {}
