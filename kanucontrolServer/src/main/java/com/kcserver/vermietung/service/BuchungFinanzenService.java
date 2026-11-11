package com.kcserver.vermietung.service;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.LocalDate;
import java.util.*;
@Service @Transactional
public class BuchungFinanzenService {
 private final BuchungRepository buchungen;
 private final BuchungFinanzbewegungRepository bewegungen;
 private final BuchungVerbrauchskostenRepository verbrauch;
 private final VerbrauchsabrechnungService verbrauchsabrechnungService;
 public BuchungFinanzenService(BuchungRepository b,BuchungFinanzbewegungRepository m,BuchungVerbrauchskostenRepository v,VerbrauchsabrechnungService va){buchungen=b;bewegungen=m;verbrauch=v;verbrauchsabrechnungService=va;}
 @Transactional(readOnly=true) public List<BuchungFinanzenDTO> getAll(){
  return buchungen.findAll().stream()
   .filter(b -> b.getStatus() != Buchungsstatus.ABGELEHNT)
   .map(this::toDto)
   .sorted(Comparator.comparing(BuchungFinanzenDTO::anreise,Comparator.nullsLast(Comparator.naturalOrder())).reversed())
   .toList();
 }
 public BuchungFinanzenDTO add(Long buchungId,FinanzbewegungSaveDTO dto){
  Buchung b=buchungen.findLockedById(buchungId).orElseThrow(()->new IllegalArgumentException("Buchung nicht gefunden: "+buchungId));
  if(b.getStatus()==Buchungsstatus.STORNIERT && b.getStornoEinbehalt()!=null && b.getStornoEinbehalt().signum()>0 && dto.art()==Finanzbewegungsart.RUECKZAHLUNG)
   throw new IllegalArgumentException("Die kurzfristige Stornierung sieht keine Erstattung vor. Der gespeicherte Einbehalt ist verbindlich.");
  if(b.getBuchungsquelle()==Buchungsquelle.AIRBNB && (dto.kategorie()==Finanzkategorie.ANZAHLUNG || dto.kategorie()==Finanzkategorie.KAUTION)) throw new IllegalArgumentException("Airbnb-Buchungen haben keine Anzahlung und Kaution.");
  if(dto.art()==Finanzbewegungsart.VERRECHNUNG && dto.kategorie()!=Finanzkategorie.KAUTION) throw new IllegalArgumentException("Verrechnungen können nur von der Kaution ausgehen.");
  if(dto.art()==Finanzbewegungsart.RUECKZAHLUNG || dto.art()==Finanzbewegungsart.VERRECHNUNG) {
   BigDecimal verfuegbar=bewegungen.findByBuchungIdOrderByDatumAscIdAsc(buchungId).stream()
    .filter(m->m.getKategorie()==dto.kategorie())
    .map(m->m.getArt()==Finanzbewegungsart.EINGANG?m.getBetrag():m.getArt()==Finanzbewegungsart.RUECKZAHLUNG?m.getBetrag().negate():(dto.kategorie()==Finanzkategorie.VERBRAUCHSKOSTEN?m.getBetrag():m.getBetrag().negate()))
    .reduce(BigDecimal.ZERO,BigDecimal::add);
   if(dto.betrag().compareTo(verfuegbar)>0) throw new IllegalArgumentException("Der Betrag übersteigt den verfügbaren Betrag von "+verfuegbar+" €.");
  }
  saveMove(b,dto.kategorie(),dto.art(),dto.betrag(),dto.datum(),dto.bemerkung());
  if(dto.art()==Finanzbewegungsart.VERRECHNUNG && dto.kategorie()==Finanzkategorie.KAUTION)
   saveMove(b,Finanzkategorie.VERBRAUCHSKOSTEN,Finanzbewegungsart.VERRECHNUNG,dto.betrag(),dto.datum(),"Verrechnung aus Kaution"+(dto.bemerkung()==null||dto.bemerkung().isBlank()?"":": "+dto.bemerkung()));
  return toDto(b);
 }
 private void saveMove(Buchung b,Finanzkategorie k,Finanzbewegungsart a,BigDecimal amount,LocalDate date,String note){
  BuchungFinanzbewegung m=new BuchungFinanzbewegung(); m.setBuchung(b);m.setKategorie(k);m.setArt(a);m.setBetrag(amount.setScale(2,RoundingMode.HALF_UP));m.setDatum(date);m.setBemerkung(note);bewegungen.save(m);
 }
 public BuchungFinanzenDTO toDto(Buchung b){return toDto(b,null);}
 public BuchungFinanzenDTO toDto(Buchung b,List<BuchungVerbrauchskosten> berechneteSnapshots){
  List<MietkostenPositionDTO> mietkosten=b.getMietbereiche().stream().map(p->{
   BigDecimal einzelpreis=zero(p.getVereinbarterPreis()).setScale(2,RoundingMode.HALF_UP);
   BigDecimal gesamt=BuchungPreiskalkulation.preis(p);
   return new MietkostenPositionDTO(p.getMietbereich().getBezeichnung(),p.getAnzahl(),einzelpreis,gesamt,BuchungPreiskalkulation.einheiten(p),p.getBerechnungsart()==null?null:p.getBerechnungsart().name());
  }).toList();
  BigDecimal brutto=mietkosten.stream().map(MietkostenPositionDTO::gesamtbetrag).reduce(BigDecimal.ZERO,BigDecimal::add);
  BigDecimal mietrabatt=BuchungPreiskalkulation.rabatt(b).min(brutto).setScale(2,RoundingMode.HALF_UP);
  BigDecimal rent=brutto.subtract(mietrabatt).max(BigDecimal.ZERO).setScale(2,RoundingMode.HALF_UP);
  List<BuchungVerbrauchskosten> snapshots=berechneteSnapshots!=null?berechneteSnapshots:verbrauch.findByBuchungIdOrderByIdAsc(b.getId());
  BigDecimal utilities;
  boolean verbrauchVorschauVollstaendig=true;
  if(berechneteSnapshots!=null || !snapshots.isEmpty()){
   utilities=snapshots.stream().map(BuchungVerbrauchskosten::getKosten).reduce(BigDecimal.ZERO,BigDecimal::add);
  } else if(b.getBuchungsquelle()==Buchungsquelle.DIREKT && b.getStatus()==Buchungsstatus.ABRECHENBAR){
   List<VerbrauchskostenVorschauDTO> vorschau=verbrauchsabrechnungService.berechneVorschau(b);
   verbrauchVorschauVollstaendig=vorschau.stream().allMatch(VerbrauchskostenVorschauDTO::erfolgreich);
   utilities=verbrauchVorschauVollstaendig
    ? vorschau.stream().map(VerbrauchskostenVorschauDTO::kosten).filter(Objects::nonNull).reduce(BigDecimal.ZERO,BigDecimal::add)
    : BigDecimal.ZERO;
  } else {
   utilities=BigDecimal.ZERO;
  }
  List<FinanzbewegungDTO> history=bewegungen.findByBuchungIdOrderByDatumAscIdAsc(b.getId()).stream().map(m->new FinanzbewegungDTO(m.getId(),m.getKategorie(),m.getArt(),m.getBetrag(),m.getDatum(),m.getBemerkung())).toList();
  return new BuchungFinanzenDTO(b.getId(),b.getBuchungsnummer(),(Optional.ofNullable(b.getMieter().getVorname()).orElse("")+" "+Optional.ofNullable(b.getMieter().getName()).orElse("")).trim(),b.getMietobjekt().getBezeichnung(),b.getBuchungsquelle(),b.getStatus().name(),b.getAnreise()==null?null:b.getAnreise().toLocalDate(),b.getAbreise()==null?null:b.getAbreise().toLocalDate(),b.isUnbefristet(),b.getBuchungsquelle()==Buchungsquelle.DIREKT?zero(b.getAnzahlungBetrag()):BigDecimal.ZERO,b.getBuchungsquelle()==Buchungsquelle.DIREKT?zero(b.getKautionBetrag()):BigDecimal.ZERO,mietkosten,mietrabatt,rent,utilities,verbrauchVorschauVollstaendig,history,EntschaedigungPositionService.rows(b),EntschaedigungPositionService.sum(b),b.getStornoEinbehalt());
 }
 private BigDecimal zero(BigDecimal x){return x==null?BigDecimal.ZERO:x;}
}
