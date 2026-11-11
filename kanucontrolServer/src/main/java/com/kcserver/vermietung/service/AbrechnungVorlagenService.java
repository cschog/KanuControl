package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class AbrechnungVorlagenService {
 private final VorlagenService vorlagen;
 private final MandantenBriefpapierRepository papier;
 private final BuchungVerbrauchskostenRepository verbrauch;
 private final BuchungFinanzenService finanzen;
 public byte[] erstellen(Buchung b,int revision,Vorlage entwurf) {
  var snapshots=verbrauch.findByBuchungIdOrderByIdAsc(b.getId());
  return entwurf(b,revision,entwurf,finanzen.toDto(b),snapshots,LocalDate.now(ZoneId.of("Europe/Berlin"))).pdf();
 }
 public VorlagenService.Ergebnis entwurf(Buchung b,int revision,Vorlage entwurf,com.kcserver.vermietung.dto.BuchungFinanzenDTO f,
          List<com.kcserver.vermietung.entity.BuchungVerbrauchskosten> snapshots,LocalDate heute) {
  VorlagenService.tenant();
  var p=papier.findById(1L).orElseThrow(()->fehler("Bitte Mandanten-Briefpapier einrichten."));
  var v=normalisieren(entwurf!=null?entwurf:vorlagen.vorlage(b.getMietobjekt().getId(),Vorgang.ABGERECHNET));
  if(v==null)throw fehler("Keine Abrechnungsvorlage (ABGERECHNET) für dieses Mietobjekt hinterlegt.");
  var person=b.getMieter();var date=DateTimeFormatter.ofPattern("dd.MM.yyyy");
  var d=new HashMap<String,String>();
  d.put("buchungsnummer",b.getBuchungsnummer());d.put("vorname",person.getVorname());d.put("nachname",person.getName());
  d.put("organisation",Objects.toString(person.getOrganisation(),""));
  d.put("anreise",date.format(b.getAnreise()));d.put("abreise",date.format(b.getAbreise()));d.put("mietobjekt",b.getMietobjekt().getBezeichnung());
  d.put("briefdatum",date.format(heute));
  d.put("mietbetrag",geld(f.mieteSoll()));d.put("anzahlung",geld(Zahlungsplan.anzahlung(b)));d.put("restmiete",geld(Zahlungsplan.restmiete(b)));d.put("kaution",geld(f.kautionSoll()));
  d.put("abrechnungssumme",geld(f.abrechnungGesamt()));d.put("saldo",geld(f.saldo()));
  d.put("restzahlung",geld(f.saldo().max(BigDecimal.ZERO)));d.put("erstattung",geld(f.saldo().negate().max(BigDecimal.ZERO)));
  // Final payment deadline is based on issue date, never on a past arrival date.
  d.put("zahlungsfrist",date.format(heute.plusDays(14)));
  var bank=b.getBankverbindungAuswahl()==com.kcserver.vermietung.enumtype.BankverbindungAuswahl.ALTERNATIV?b.getMietobjekt().getBankverbindungAlternativ():b.getMietobjekt().getBankverbindungPrimaer();
  d.put("bank_name",bank==null?"":Objects.toString(bank.getBankName(),""));d.put("bank_iban",bank==null?"":Objects.toString(bank.getIban(),""));d.put("bank_bic",bank==null?"":Objects.toString(bank.getBic(),""));
  d.put("mieter_bank_name",Objects.toString(person.getBankName(),""));d.put("mieter_bank_iban",Objects.toString(person.getIban(),""));d.put("mieter_bank_bic",Objects.toString(person.getBic(),""));
  d.put("mieter_kontoinhaber",person.getKontoinhaber()==null||person.getKontoinhaber().isBlank()?(person.getVorname()+" "+person.getName()).strip():person.getKontoinhaber());
  var wahr=EnumSet.of(Bedingung.IMMER,Bedingung.valueOf("ZAHLUNG_"+b.getZahlungsvariante().name()),f.saldo().signum()<0?Bedingung.ERSTATTUNG_POSITIV:f.saldo().signum()>0?Bedingung.RESTZAHLUNG_POSITIV:Bedingung.AUSGEGLICHEN);
  if(f.kautionSoll().signum()>0)wahr.add(Bedingung.KAUTION_POSITIV);
  if(f.saldo().signum()<0 && d.get("mieter_bank_iban").isBlank())throw fehler("Für eine Rückerstattung bitte die IBAN des Mieters hinterlegen.");
  var felder=new HashMap<String,String>();
  felder.put("empfaenger_name",(d.get("organisation").isBlank()?"":d.get("organisation")+"\n")+person.getVorname()+" "+person.getName());
  felder.put("empfaenger_strasse",Objects.toString(person.getStrasse(),""));felder.put("empfaenger_ort",Objects.toString(person.getPlz(),"")+" "+Objects.toString(person.getOrt(),"")+(person.getCountryCode()!=null&&!person.getCountryCode().name().equals("DE")?"\n"+person.getCountryCode().name():""));
  felder.put("briefdatum",d.get("briefdatum"));felder.put("buchungsnummer",b.getBuchungsnummer());felder.put("anrede",ersetzen(v.anrede(),d));
  // These fields always identify the club account; never overwrite them with the tenant's account.
  for(String key:List.of("bank_name","bank_iban","bank_bic"))felder.put(key,d.get(key));
  byte[] bytes=AbrechnungBriefLayout.render(p.getPdf(),felder,texte(v,d,wahr),ersetzen(p.getGruss(),d),p.getBild(),b.getBuchungsnummer(),Abrechnungsbereich.aus(f,snapshots,revision,b));
  String meta=d.toString()+v.toString()+p.getVersion()+Abrechnungsbereich.aus(f,snapshots,revision,b);
  return new VorlagenService.Ergebnis(bytes,ersetzen(v.betreff(),d),ersetzen(v.mailtext(),d),VorlagenService.hash(meta),meta);
 }
 private static String geld(BigDecimal n){var f=java.text.NumberFormat.getNumberInstance(Locale.GERMANY);f.setMinimumFractionDigits(2);f.setMaximumFractionDigits(2);return f.format(n);}
}
