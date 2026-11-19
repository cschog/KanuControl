package com.kcserver.vermietung.service;

import com.kcserver.core.util.PersonName;
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
  return entwurf(b,revision,entwurf,f,snapshots,heute,true);
 }
 public VorlagenService.Ergebnis entwurf(Buchung b,int revision,Vorlage entwurf,com.kcserver.vermietung.dto.BuchungFinanzenDTO f,
          List<com.kcserver.vermietung.entity.BuchungVerbrauchskosten> snapshots,LocalDate heute,boolean pruefeErstattungskonto) {
  return entwurf(b,revision,entwurf,f,snapshots,heute,pruefeErstattungskonto,null);
 }
 public VorlagenService.Ergebnis jahresentwurf(Buchung b,com.kcserver.vermietung.dto.BuchungFinanzenDTO f,int jahr,LocalDate heute) {
  return entwurf(b,1,null,f,List.of(),heute,false,jahr);
 }
 private VorlagenService.Ergebnis entwurf(Buchung b,int revision,Vorlage entwurf,com.kcserver.vermietung.dto.BuchungFinanzenDTO f,
          List<com.kcserver.vermietung.entity.BuchungVerbrauchskosten> snapshots,LocalDate heute,boolean pruefeErstattungskonto,Integer jahr) {
  var saldo=jahr==null?f.saldo():JahresabrechnungService.saldo(f);
  if(b.getBuchungsquelle()==com.kcserver.vermietung.enumtype.Buchungsquelle.AIRBNB)throw fehler("Für Airbnb-Buchungen werden keine PDFs erstellt.");
  if(Zahlungsplan.ohneAbrechnung(b))throw fehler("Für Ohne Abrechnung wird keine Rechnung erstellt.");
  VorlagenService.tenant();
  var p=papier.findById(1L).orElseThrow(()->fehler("Bitte Mandanten-Briefpapier einrichten."));
  var v=normalisieren(entwurf!=null?entwurf:vorlagen.vorlage(b.getMietobjekt().getId(),Vorgang.ABGERECHNET));
  if(v==null)throw fehler("Keine Abrechnungsvorlage (ABGERECHNET) für dieses Mietobjekt hinterlegt.");
  var person=b.getMieter();var date=DateTimeFormatter.ofPattern("dd.MM.yyyy");
  var d=new HashMap<String,String>();
  d.put("fruehere_buchungsnummer",Objects.toString(b.getFruehereBuchungsnummer(),""));
        d.put("buchungsnummer",b.getBuchungsnummer());d.put("vorname",person.getVorname());d.put("nachname",person.getName());
  d.put("organisation",Buchungsadressat.organisation(b));
  d.put("anreise",date.format(b.getAnreise()));d.put("abreise",date.format(b.getAbreise()));d.put("mietobjekt",b.getMietobjekt().getBezeichnung());

  if(jahr!=null){d.put("anreise",date.format(f.anreise()));d.put("abreise",date.format(f.abreise()));}
  d.put("briefdatum",date.format(heute));
  d.put("mietbetrag",geld(f.mieteSoll()));d.put("anzahlung",geld(Zahlungsplan.anzahlung(b)));d.put("restmiete",geld(Zahlungsplan.restmiete(b)));d.put("kaution",geld(f.kautionSoll()));
  if(jahr!=null){d.put("anzahlung",geld(BigDecimal.ZERO));d.put("restmiete",geld(saldo.max(BigDecimal.ZERO)));}
  d.put("abrechnungssumme",geld(f.abrechnungGesamt()));d.put("saldo",geld(saldo));
  d.put("restzahlung",geld(saldo.max(BigDecimal.ZERO)));d.put("erstattung",geld(saldo.negate().max(BigDecimal.ZERO)));
  // Final payment deadline is based on issue date, never on a past arrival date.
  d.put("zahlungsfrist",date.format(heute.plusDays(14)));
  var bank=b.getBankverbindungAuswahl()==com.kcserver.vermietung.enumtype.BankverbindungAuswahl.ALTERNATIV?b.getMietobjekt().getBankverbindungAlternativ():b.getMietobjekt().getBankverbindungPrimaer();
  d.put("bank_name",bank==null?"":Objects.toString(bank.getBankName(),""));d.put("bank_iban",bank==null?"":Objects.toString(bank.getIban(),""));d.put("bank_bic",bank==null?"":Objects.toString(bank.getBic(),""));
  d.put("mieter_bank_name",Objects.toString(person.getBankName(),""));d.put("mieter_bank_iban",Objects.toString(person.getIban(),""));d.put("mieter_bank_bic",Objects.toString(person.getBic(),""));
  d.put("mieter_kontoinhaber",person.getKontoinhaber()==null||person.getKontoinhaber().isBlank()?(PersonName.givenFirst(person.getName(), person.getVorname())).strip():person.getKontoinhaber());
  d.putAll(Buchungsadressat.erstattungskonto(b));
  var wahr=EnumSet.of(Bedingung.IMMER,Bedingung.valueOf("ZAHLUNG_"+b.getZahlungsvariante().name()),saldo.signum()<0?Bedingung.ERSTATTUNG_POSITIV:saldo.signum()>0?Bedingung.RESTZAHLUNG_POSITIV:Bedingung.AUSGEGLICHEN);
  if(b.getFruehereBuchungsnummer()!=null && !b.getFruehereBuchungsnummer().isBlank())wahr.add(Bedingung.FRUEHERE_BUCHUNGSNUMMER_VORHANDEN);
  if(f.kautionSoll().signum()>0)wahr.add(Bedingung.KAUTION_POSITIV);
  if(pruefeErstattungskonto && saldo.signum()<0 && d.get("mieter_bank_iban").isBlank())throw fehler("Für eine Rückerstattung bitte die IBAN des Rechnungsempfängers hinterlegen.");
  var felder=new HashMap<String,String>();
  felder.putAll(Buchungsadressat.felder(b));
  felder.put("briefdatum",d.get("briefdatum"));felder.put("buchungsnummer",b.getBuchungsnummer());felder.put("anrede",ersetzen(v.anrede(),d));
  // These fields always identify the club account; never overwrite them with the tenant's account.
  for(String key:List.of("bank_name","bank_iban","bank_bic"))felder.put(key,d.get(key));
  var texte=new ArrayList<>(texte(v,d,wahr));
  var bereich=Abrechnungsbereich.aus(f,snapshots,revision,b);
  if(jahr!=null){
   texte.addFirst("Jahresabrechnung "+jahr+" – Mietverhältnis läuft unbefristet weiter.\nAbgerechnet wird die vereinbarte Miete für das Kalenderjahr; berücksichtigt sind Mietzahlungen mit Zahlungsdatum in diesem Jahr. Kaution und Verbrauchskosten sind nicht Gegenstand dieser Jahresabrechnung.");
   var zeilen=new ArrayList<>(bereich.zeilen());
   zeilen.removeIf(z->z.beschreibung().equals("Kautionsanforderung / Kategorieausgleich"));
   zeilen.removeLast();
   zeilen.add(new Abrechnungsbereich.Zeile(saldo.signum()<0?"Guthaben (Miete)":saldo.signum()>0?"Restzahlung":"Ausgeglichen","","",java.text.NumberFormat.getCurrencyInstance(Locale.GERMANY).format(saldo.abs())));
   bereich=new Abrechnungsbereich(zeilen);
  }
  byte[] bytes=AbrechnungBriefLayout.render(p.getPdf(),felder,texte,ersetzen(p.getGruss(),d),p.getBild(),b.getBuchungsnummer(),bereich);
  String meta=felder.toString()+d.toString()+v.toString()+p.getVersion()+bereich+(jahr==null?"":jahr);
  return new VorlagenService.Ergebnis(bytes,jahr==null?ersetzen(v.betreff(),d):"Jahresabrechnung "+jahr+" – "+b.getBuchungsnummer(),ersetzen(v.mailtext(),d),VorlagenService.hash(meta),meta);
 }
 private static String geld(BigDecimal n){var f=java.text.NumberFormat.getNumberInstance(Locale.GERMANY);f.setMinimumFractionDigits(2);f.setMaximumFractionDigits(2);return f.format(n);}
}
