package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.FinanzbewegungDTO;
import com.kcserver.vermietung.enumtype.Finanzbewegungsart;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Announced settlement only; never books a refund or invents an incoming payment. */
public record Stornoberechnung(LocalDate datum, boolean volleErstattung, BigDecimal gezahlt,
                              BigDecimal bereitsErstattet, BigDecimal erstattung, BigDecimal einbehalt) {
    public static Stornoberechnung berechnen(LocalDate datum, LocalDate mietbeginn, List<FinanzbewegungDTO> bewegungen) {
        BigDecimal eingang=summe(bewegungen,Finanzbewegungsart.EINGANG);
        BigDecimal rueck=summe(bewegungen,Finanzbewegungsart.RUECKZAHLUNG);
        BigDecimal verbleibend=eingang.subtract(rueck).max(BigDecimal.ZERO).setScale(2);
        boolean voll=ChronoUnit.DAYS.between(datum,mietbeginn)>=56;
        return new Stornoberechnung(datum,voll,eingang.setScale(2),rueck.setScale(2),
                voll?verbleibend:BigDecimal.ZERO.setScale(2),voll?BigDecimal.ZERO.setScale(2):verbleibend);
    }
    private static BigDecimal summe(List<FinanzbewegungDTO> liste,Finanzbewegungsart art) {
        return liste.stream().filter(m->m.art()==art).map(FinanzbewegungDTO::betrag).reduce(BigDecimal.ZERO,BigDecimal::add);
    }
}
