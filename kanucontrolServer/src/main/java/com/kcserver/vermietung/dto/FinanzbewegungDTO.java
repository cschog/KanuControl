package com.kcserver.vermietung.dto;
import com.kcserver.vermietung.enumtype.Finanzbewegungsart;
import com.kcserver.vermietung.enumtype.Finanzkategorie;
import java.math.BigDecimal;
import java.time.LocalDate;
public record FinanzbewegungDTO(Long id, Finanzkategorie kategorie, Finanzbewegungsart art, BigDecimal betrag, LocalDate datum, String bemerkung) {}
