package com.kcserver.vermietung.service;

import com.kcserver.core.util.PersonName;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.repository.HomepageAnbindungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

/** Common letter context, preserving the existing OG association and renderer. */
@Service @RequiredArgsConstructor
public class BuchungsnachrichtDaten {
    private final HomepageAnbindungRepository anbindungen;
    public static Vorgang vorgang(Buchungsstatus ziel){return switch(ziel){
        case RESERVIERT -> Vorgang.RESERVIERUNG;case BESTAETIGT -> Vorgang.BESTAETIGUNG;
        case ABGELEHNT -> Vorgang.ABLEHNUNG;case STORNIERT -> Vorgang.STORNIERUNG;
        case ABGESCHLOSSEN -> Vorgang.ABGERECHNET;default -> throw fehler("Für diese Aktion gibt es keinen Nachrichtenvorgang.");};}
    public static boolean ohneMail(Buchungsstatus quelle,Buchungsstatus ziel){return quelle==Buchungsstatus.RESERVIERT && ziel==Buchungsstatus.BESTAETIGT;}
    public static void zahlungsplanPruefen(Buchung b,Buchungsstatus ziel,List<FinanzbewegungDTO> zahlungen){
        if (Zahlungsplan.ohneAbrechnung(b)) return;
        if(b.getBuchungsquelle()==Buchungsquelle.DIREKT && b.getStatus()==Buchungsstatus.ANFRAGE && ziel==Buchungsstatus.BESTAETIGT && b.getZahlungsvariante()!=Zahlungsvariante.BEI_ABRECHNUNG)
            throw fehler("Für eine direkte Bestätigung ohne Zahlungsaufforderung bitte „Zahlung bei Abrechnung“ wählen.");
        if(b.getBuchungsquelle()==Buchungsquelle.DIREKT && ziel==Buchungsstatus.RESERVIERT && Zahlungsplan.anzahlung(b).compareTo(BuchungPreiskalkulation.miete(b))>0)throw fehler("Die vereinbarte Anzahlung übersteigt den Mietbetrag.");
        if(b.getBuchungsquelle()==Buchungsquelle.DIREKT && ziel==Buchungsstatus.RESERVIERT && b.getZahlungsvariante()==Zahlungsvariante.BEI_ABRECHNUNG)
            throw fehler("Bei Zahlung nach Abrechnung bitte die Anfrage direkt bestätigen.");
        if(ohneMail(b.getStatus(),ziel) && b.getBuchungsquelle()==Buchungsquelle.DIREKT){
            BigDecimal eingang=bezahlt(zahlungen,Set.of(Finanzkategorie.ANZAHLUNG,Finanzkategorie.MIETE));
            BigDecimal soll=b.getZahlungsvariante()==Zahlungsvariante.GESAMTMIETE_VORAUS?BuchungPreiskalkulation.miete(b):Zahlungsplan.anzahlung(b);
            if(eingang.compareTo(soll)<0)throw fehler("Die geforderte Mietvorauszahlung ist noch nicht vollständig eingegangen.");
            if(b.getZahlungsvariante()==Zahlungsvariante.GESAMTMIETE_VORAUS && bezahlt(zahlungen,Set.of(Finanzkategorie.KAUTION)).compareTo(b.getKautionBetrag())<0)
                throw fehler("Die angeforderte Kaution ist noch nicht vollständig eingegangen.");
        }
    }
    private static BigDecimal bezahlt(List<FinanzbewegungDTO> m,Set<Finanzkategorie> k){return m.stream().filter(x->k.contains(x.kategorie())).map(x->switch(x.art()){case EINGANG->x.betrag();case RUECKZAHLUNG->x.betrag().negate();case VERRECHNUNG->BigDecimal.ZERO;}).reduce(BigDecimal.ZERO,BigDecimal::add);}
    public ReservierungsbestaetigungPdf.Inhalt basis(Buchung b,LocalDate heute){return basis(b,heute,true);}
    public ReservierungsbestaetigungPdf.Inhalt dokumentBasis(Buchung b,LocalDate heute){return basis(b,heute,false);}
    private ReservierungsbestaetigungPdf.Inhalt basis(Buchung b,LocalDate heute,boolean pruefen){
        var p=b.getMieter();var d=new LinkedHashMap<String,String>();
        d.putAll(Buchungsadressat.felder(b));
        d.put("briefdatum",heute.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));d.put("buchungsnummer",b.getBuchungsnummer());d.put("anrede","Hallo "+(PersonName.vorname(p.getVorname()).isEmpty()?p.getName():PersonName.vorname(p.getVorname()))+",");
        var bank=b.getBankverbindungAuswahl()==BankverbindungAuswahl.ALTERNATIV?b.getMietobjekt().getBankverbindungAlternativ():b.getMietobjekt().getBankverbindungPrimaer();
        d.put("bank_name",bank==null?"":"Bank: "+leer(bank.getBankName()));d.put("bank_iban",bank==null?"":"IBAN: "+leer(bank.getIban()));d.put("bank_bic",bank==null?"":"BIC: "+leer(bank.getBic()));
        if(pruefen && b.getStatus()==Buchungsstatus.ANFRAGE && b.getZahlungsvariante()!=Zahlungsvariante.BEI_ABRECHNUNG && b.getBuchungsquelle()==Buchungsquelle.DIREKT && (BuchungPreiskalkulation.miete(b).signum()>0 || b.getKautionBetrag().signum()>0)){
            if(bank==null)throw fehler("Bitte die ausgewählte Bankverbindung am Mietobjekt einrichten.");pflicht(bank.getIban(),"IBAN des Vereins");pflicht(bank.getBankName(),"Bankname des Vereins");
        }
        String url=anbindungen.findById(b.getMietobjekt().getId()).map(HomepageAnbindung::getHausregelnUrl).orElse("");
        boolean og=b.getMietbereiche().stream().anyMatch(x->ReservierungsbestaetigungPdf.enthaeltOg(x.getMietbereich(),new HashSet<>()));
        return new ReservierungsbestaetigungPdf.Inhalt(d,og,url);
    }
    public Map<String,String> stornoDaten(Buchung b,Stornoberechnung s){return stornoDaten(b,s,true);}
    public Map<String,String> stornoDaten(Buchung b,Stornoberechnung s,boolean pruefen){
        var p=b.getMieter();var d=new HashMap<String,String>();
        d.put("stornodatum",s.datum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        d.put("storno_gezahlter_betrag",geld(s.gezahlt()));d.put("storno_bereits_erstattet",geld(s.bereitsErstattet()));d.put("storno_erstattung",geld(s.erstattung()));d.put("storno_einbehalt",geld(s.einbehalt()));
        d.put("mieter_bank_name",leer(p.getBankName()));d.put("mieter_bank_iban",leer(p.getIban()));d.put("mieter_bank_bic",leer(p.getBic()));
        d.put("mieter_kontoinhaber",leer(p.getKontoinhaber()).isBlank()?PersonName.givenFirst(p.getName(), p.getVorname()):p.getKontoinhaber());
        if(pruefen && s.erstattung().signum()>0)pflicht(p.getIban(),"IBAN für die Erstattung");
        return d;
    }
    public Abrechnungsbereich stornoTabelle(Stornoberechnung s,List<FinanzbewegungDTO> zahlungen){
        var rows=new ArrayList<Abrechnungsbereich.Zeile>();
        for(var m:zahlungen)if(m.art()!=Finanzbewegungsart.VERRECHNUNG && m.betrag().signum()!=0)rows.add(new Abrechnungsbereich.Zeile((m.art()==Finanzbewegungsart.EINGANG?"Gezahlt":"Bereits erstattet")+" am "+m.datum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))+" ("+Abrechnungsbereich.kategorie(m.kategorie())+")","","",Abrechnungsbereich.geld(m.betrag())));
        if(s.einbehalt().signum()>0)rows.add(new Abrechnungsbereich.Zeile("Einbehalt nach Stornoregel","","",Abrechnungsbereich.geld(s.einbehalt())));
        if(s.erstattung().signum()>0)rows.add(new Abrechnungsbereich.Zeile("Noch zu erstatten","","",Abrechnungsbereich.geld(s.erstattung())));
        if(rows.isEmpty())rows.add(new Abrechnungsbereich.Zeile("Keine geleisteten Zahlungen","","",""));
        return new Abrechnungsbereich(rows);
    }
    public static String leer(String s){return Objects.toString(s,"");}
    private static String pflicht(String v,String label){if(v==null||v.isBlank())throw fehler(label+" fehlt.");return v.strip();}
    private static String geld(BigDecimal x){var f=java.text.NumberFormat.getNumberInstance(Locale.GERMANY);f.setMinimumFractionDigits(2);f.setMaximumFractionDigits(2);return f.format(x);}
}
