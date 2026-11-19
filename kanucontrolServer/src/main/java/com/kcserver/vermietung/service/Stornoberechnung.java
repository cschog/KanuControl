package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.FinanzbewegungDTO;
import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.entity.BuchungFinanzbewegung;
import com.kcserver.vermietung.enumtype.Finanzbewegungsart;
import com.kcserver.vermietung.enumtype.Finanzkategorie;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.Bedingung;

/** Announced settlement only; internal offsets are never cash receipts/refunds. */
public record Stornoberechnung(LocalDate datum, boolean volleErstattung, BigDecimal gezahlt,
                              BigDecimal bereitsErstattet, BigDecimal erstattung, BigDecimal einbehalt) {
    public static Stornoberechnung berechnen(LocalDate datum, LocalDate mietbeginn, List<FinanzbewegungDTO> bewegungen) {
        BigDecimal eingang=summe(bewegungen,Finanzbewegungsart.EINGANG);
        BigDecimal rueck=summe(bewegungen,Finanzbewegungsart.RUECKZAHLUNG);
        BigDecimal verbleibend=eingang.subtract(rueck).max(BigDecimal.ZERO).setScale(2);
        boolean voll=ChronoUnit.DAYS.between(datum,mietbeginn)>=56;
        BigDecimal erstattung=voll?verbleibend:verbleibendeKaution(bewegungen);
        return new Stornoberechnung(datum,voll,eingang.setScale(2),rueck.setScale(2),erstattung,verbleibend.subtract(erstattung));
    }
    /** Cap by actual cash remaining, including refunds recorded under a different category. */
    public static BigDecimal verbleibendeKaution(List<FinanzbewegungDTO> bewegungen) {
        BigDecimal kaution=bewegungen.stream().filter(m->m.kategorie()==Finanzkategorie.KAUTION)
            .map(m->m.art()==Finanzbewegungsart.EINGANG?m.betrag():m.art()==Finanzbewegungsart.RUECKZAHLUNG?m.betrag().negate():BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO,BigDecimal::add).max(BigDecimal.ZERO);
        BigDecimal cash=summe(bewegungen,Finanzbewegungsart.EINGANG).subtract(summe(bewegungen,Finanzbewegungsart.RUECKZAHLUNG)).max(BigDecimal.ZERO);
        return kaution.min(cash).setScale(2);
    }
    public Set<Bedingung> bedingungen(){
        var result=EnumSet.noneOf(Bedingung.class);
        if(erstattung.signum()>0)result.add(Bedingung.STORNO_ERSTATTUNG_POSITIV);
        if(einbehalt.signum()>0)result.add(Bedingung.STORNO_EINBEHALT_POSITIV);
        if(result.isEmpty())result.add(Bedingung.STORNO_OHNE_ZAHLUNG);
        return result;
    }
    public static FinanzbewegungDTO dto(BuchungFinanzbewegung m){return new FinanzbewegungDTO(m.getId(),m.getKategorie(),m.getArt(),m.getBetrag(),m.getDatum(),m.getBemerkung());}
    /** Apply the deposit exclusion to legacy cancellations without increasing their stored charge. */
    public static BigDecimal aktuellerEinbehalt(Buchung b,List<FinanzbewegungDTO> bewegungen){
        BigDecimal gespeichert=Optional.ofNullable(b.getStornoEinbehalt()).orElse(BigDecimal.ZERO);
        if(b.getStatus()!=Buchungsstatus.STORNIERT || b.getAnreise()==null || gespeichert.signum()==0)return gespeichert;
        LocalDate datum=b.getStornoDatum()==null?b.getAnreise().toLocalDate():b.getStornoDatum();
        return gespeichert.min(berechnen(datum,b.getAnreise().toLocalDate(),bewegungen).einbehalt());
    }
    private static BigDecimal summe(List<FinanzbewegungDTO> liste,Finanzbewegungsart art) {
        return liste.stream().filter(m->m.art()==art).map(FinanzbewegungDTO::betrag).reduce(BigDecimal.ZERO,BigDecimal::add);
    }
}
