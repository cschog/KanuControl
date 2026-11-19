package com.kcserver.vermietung.service;

import com.kcserver.vermietung.dto.BuchungFinanzenDTO;
import com.kcserver.vermietung.dto.FinanzbewegungDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Annual rent statement, deliberately independent of the tenancy's final settlement. */
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class JahresabrechnungService {
 private final BuchungRepository buchungen;
 private final VermietungAbrechnungRepository dokumente;
 private final BuchungFinanzenService finanzen;
 private final AbrechnungVorlagenService vorlagen;
 public record Dokument(Long id,int jahr,Instant erstellt,boolean pdfVorhanden) {}
 public record Uebersicht(int aktuellesJahr,List<Dokument> dokumente) {}
 public record Vorschau(int jahr,String fingerprint,String pdfBase64,BigDecimal miete,BigDecimal saldo) {}
 public record Auftrag(String fingerprint) {}
 private record Entwurf(BuchungFinanzenDTO finanzen,byte[] pdf,String fingerprint) {}
 private static LocalDate heute(){return LocalDate.now(ZoneId.of("Europe/Berlin"));}
 private Dokument dto(VermietungAbrechnung e){return new Dokument(e.getId(),e.getAbrechnungsJahr(),e.getErstellt(),e.getPdf().length>0);}
 public Uebersicht list(Long id){
  buchungen.findById(id).orElseThrow(()->new IllegalArgumentException("Buchung nicht gefunden."));
  return new Uebersicht(heute().getYear(),dokumente.findByBuchungIdAndAbrechnungsJahrIsNotNullOrderByAbrechnungsJahrDesc(id).stream().map(this::dto).toList());
 }
 static void pruefe(Buchung b,int jahr,LocalDate heute){
  if(jahr!=heute.getYear())throw new IllegalArgumentException("Eine Jahresabrechnung kann nur für das laufende Kalenderjahr erstellt werden.");
  if(!b.isUnbefristet())throw new IllegalArgumentException("Jahresabrechnungen sind für Buchungen ohne Enddatum vorgesehen.");
  if(b.getAnreise()==null || b.getAnreise().getYear()>jahr)throw new IllegalArgumentException("Die Buchung beginnt erst nach dem Abrechnungsjahr.");
  if(!Set.of(Buchungsstatus.RESERVIERT,Buchungsstatus.BESTAETIGT,Buchungsstatus.ABRECHENBAR,Buchungsstatus.ABGESCHLOSSEN).contains(b.getStatus()))throw new IllegalArgumentException("Für diesen Buchungsstatus ist keine Jahresabrechnung möglich.");
 }
 static BuchungFinanzenDTO jahresfinanzen(BuchungFinanzenDTO f,int jahr){
  var zahlungen=f.bewegungen().stream().filter(m->m.datum()!=null && m.datum().getYear()==jahr)
   .filter(m->m.kategorie()==Finanzkategorie.MIETE || m.kategorie()==Finanzkategorie.ANZAHLUNG).toList();
  var von=f.anreise().isAfter(LocalDate.of(jahr,1,1))?f.anreise():LocalDate.of(jahr,1,1);
  return new BuchungFinanzenDTO(f.buchungId(),f.buchungsnummer(),f.mieter(),f.mietobjekt(),f.buchungsquelle(),f.buchungsstatus(),von,LocalDate.of(jahr,12,31),true,
   BigDecimal.ZERO,BigDecimal.ZERO,f.mietkosten(),f.mietrabatt(),f.mieteSoll(),BigDecimal.ZERO,true,zahlungen,List.of(),BigDecimal.ZERO,BigDecimal.ZERO);
 }
 static BigDecimal saldo(BuchungFinanzenDTO f){
  return f.mieteSoll().subtract(f.bewegungen().stream().map(m->switch(m.art()){
   case EINGANG -> m.betrag();case RUECKZAHLUNG -> m.betrag().negate();case VERRECHNUNG -> BigDecimal.ZERO;
  }).reduce(BigDecimal.ZERO,BigDecimal::add)).setScale(2);
 }
 private Entwurf entwurf(Buchung b,int jahr){
  var datum=heute();pruefe(b,jahr,datum);
  // An empty snapshot list avoids calculating a fictitious end-of-tenancy consumption.
  var f=jahresfinanzen(finanzen.toDto(b,List.of()),jahr);
  var alt=dokumente.findByBuchungIdAndAbrechnungsJahr(b.getId(),jahr);
  byte[] pdf=null;String vorlagenFingerprint="";
  if(b.getBuchungsquelle()!=Buchungsquelle.AIRBNB && !Zahlungsplan.ohneAbrechnung(b)){
   var v=vorlagen.jahresentwurf(b,f,jahr,datum);pdf=v.pdf();vorlagenFingerprint=v.fingerprint();
  }
  String fp=VorlagenService.hash(f.toString()+saldo(f)+datum+vorlagenFingerprint+b.getAbrechnungRevision()+b.getStatus()+b.getAbreise()+alt.map(e->e.getId()+":"+e.getRevision()+":"+e.getErstellt()).orElse("neu"));
  return new Entwurf(f,pdf,fp);
 }
 public Vorschau vorschau(Long id,int jahr){
  var b=buchungen.findById(id).orElseThrow(()->new IllegalArgumentException("Buchung nicht gefunden."));var e=entwurf(b,jahr);
  return new Vorschau(jahr,e.fingerprint(),e.pdf()==null?null:Base64.getEncoder().encodeToString(e.pdf()),e.finanzen().mieteSoll(),saldo(e.finanzen()));
 }
 @Transactional public Dokument abschliessen(Long id,int jahr,Auftrag request){
  var b=buchungen.findLockedById(id).orElseThrow(()->new IllegalArgumentException("Buchung nicht gefunden."));var v=entwurf(b,jahr);
  if(request==null || !Objects.equals(v.fingerprint(),request.fingerprint()))throw new IllegalArgumentException("Die Abrechnungsdaten wurden geändert. Bitte die Jahresabrechnung erneut prüfen.");
  var e=dokumente.findByBuchungIdAndAbrechnungsJahr(id,jahr).orElseGet(VermietungAbrechnung::new);
  e.setBuchung(b);e.setAbrechnungsJahr(jahr);e.setRevision(e.getRevision()+1);e.setErstellt(Instant.now());e.setKorrekturgrund("");e.setPdf(v.pdf()==null?new byte[0]:v.pdf());
  dokumente.saveAndFlush(e);
  // No status, dates, final-settlement counters, deposit movements or calendar occupancy are changed.
  return dto(e);
 }
 public byte[] pdf(Long id,int jahr){
  var e=dokumente.findByBuchungIdAndAbrechnungsJahr(id,jahr).orElseThrow(()->new IllegalArgumentException("Jahresabrechnung nicht gefunden."));
  if(e.getPdf().length==0)throw new IllegalArgumentException("Für diesen Jahresabschluss wird kein PDF erstellt.");
  return e.getPdf();
 }
}
