package com.kcserver.vermietung.dto;
import com.kcserver.vermietung.enumtype.Buchungsquelle;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record BuchungFinanzenDTO(Long buchungId, String buchungsnummer, String mieter, String mietobjekt, Buchungsquelle buchungsquelle, String buchungsstatus, LocalDate anreise, LocalDate abreise, boolean unbefristet, BigDecimal anzahlungSoll, BigDecimal kautionSoll, List<MietkostenPositionDTO> mietkosten, BigDecimal mietrabatt, BigDecimal mieteSoll, BigDecimal verbrauchSoll, boolean verbrauchVorschauVollstaendig, List<FinanzbewegungDTO> bewegungen) {}
