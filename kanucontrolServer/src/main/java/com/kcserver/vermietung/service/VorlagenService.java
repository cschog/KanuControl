package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.core.tenancy.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

@Service @RequiredArgsConstructor @Transactional
public class VorlagenService {
    private final MandantenBriefpapierRepository briefpapier;
    private final MietobjektVorlageRepository vorlagen;
    private final MietobjektRepository objekte;
    private final ObjectMapper mapper;
    public record Ergebnis(byte[] pdf, String betreff, String mailtext, String fingerprint, String metadaten) {}
    public static void tenant() {
        String t=TenantContext.getCurrentTenant();
        if(t==null || t.isBlank()) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Mandantenkontext fehlt.");
    }
    public static String hash(String text) {
        try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    private String json(Object value){try{return mapper.writeValueAsString(value);}catch(Exception e){throw fehler("Konfiguration konnte nicht verarbeitet werden.");}}
    private Vorlage lesen(MietobjektVorlage v){try{var x=mapper.readValue(v.getInhalt(),Vorlage.class);return normalisieren(new Vorlage(v.getVersion(),x.betreff(),x.mailtext(),x.bloecke(),x.anrede()));}catch(Exception e){throw fehler("Gespeicherte Vorlage ist ungültig.");}}
    @Transactional(readOnly=true)
    public Briefpapier briefpapier(){tenant();return briefpapier.findById(1L).map(b->new Briefpapier(b.getVersion(),b.getGruss(),true,b.getBild()!=null)).orElse(new Briefpapier(null,"",false,false));}
    public record Probe(byte[] pdf,String fingerprint) {}
    @Transactional(readOnly=true)
    public Probe probe(Long version,String gruss,byte[] pdf,byte[] bild,boolean bildEntfernen){
        var papier=papierEntwurf(version,gruss,pdf,bild,bildEntfernen);
        gruss=papier.getGruss();byte[] asset=papier.getPdf();byte[] image=papier.getBild();
        var d=new HashMap<String,String>();for(String k:FELDER)d.put(k,"Beispiel");
        d.put("vorname","Erika");d.put("nachname","Mustermann");
        var f=Map.of("empfaenger_name","Erika Mustermann","empfaenger_strasse","Musterstraße 12","empfaenger_ort","52076 Aachen","briefdatum","09.11.2026","buchungsnummer","2026-001","anrede","Hallo Erika,","bank_name","Bank: Musterbank","bank_iban","IBAN: DE00000000000000000000","bank_bic","BIC: MUSTER00");
        byte[] preview=BriefpapierPdf.render(asset,f,List.of("Beispielüberschrift","Hier erscheinen die konfigurierten Textblöcke. Bitte Anschrift, Datum, Bankfelder und den verfügbaren Textbereich prüfen."),ersetzen(gruss,d),image,List.of(),"2026-001");
        return new Probe(preview,hash(json(Arrays.asList(version,gruss,Base64.getEncoder().encodeToString(asset),image==null?null:Base64.getEncoder().encodeToString(image)))));
    }
    private MandantenBriefpapier papierEntwurf(Long version,String gruss,byte[] pdf,byte[] bild,boolean bildEntfernen) {
        tenant();gruss=normalisieren(gruss);pruefeText(gruss,true);
        var gespeichert=briefpapier.findById(1L).orElse(null);
        if(!Objects.equals(gespeichert==null?null:gespeichert.getVersion(),version))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Briefpapier wurde inzwischen geändert. Bitte neu laden.");
        var entwurf=new MandantenBriefpapier();entwurf.setVersion(version);entwurf.setGruss(gruss);
        entwurf.setPdf(pdf!=null?pdf:gespeichert==null?null:gespeichert.getPdf());
        entwurf.setBild(bild!=null?bild:bildEntfernen||gespeichert==null?null:gespeichert.getBild());
        if(entwurf.getPdf()==null)throw fehler("Bitte ein PDF-Briefpapier auswählen oder ein gespeichertes Briefpapier verwenden.");
        BriefpapierPdf.pruefen(entwurf.getPdf());BriefpapierPdf.pruefeBild(entwurf.getBild());
        if(gruss.isBlank() && entwurf.getBild()==null)throw fehler("Grußformel oder Abschlussbild ist erforderlich.");
        return entwurf;
    }
    public Briefpapier speichern(Long version,String gruss,byte[] pdf,byte[] bild,boolean bildEntfernen,String pruefung){
        gruss=normalisieren(gruss);
        var probe=probe(version,gruss,pdf,bild,bildEntfernen);
        if(pruefung==null || !pruefung.equals(probe.fingerprint()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Bitte das geänderte Briefpapier zuerst in der Vorschau prüfen.");
        tenant();pruefeText(gruss,true);
        var b=briefpapier.findById(1L).orElseGet(MandantenBriefpapier::new);
        if(!Objects.equals(b.getVersion(),version))throw new ResponseStatusException(HttpStatus.CONFLICT,"Briefpapier wurde inzwischen geändert. Bitte neu laden.");
        if(pdf!=null){BriefpapierPdf.pruefen(pdf);b.setPdf(pdf);}
        if(b.getPdf()==null)throw fehler("Bitte zuerst ein gültiges PDF-Gerüst hochladen.");
        if(bild!=null){BriefpapierPdf.pruefeBild(bild);b.setBild(bild);}else if(bildEntfernen)b.setBild(null);
        b.setGruss(gruss);briefpapier.saveAndFlush(b);return new Briefpapier(b.getVersion(),b.getGruss(),true,b.getBild()!=null);
    }
    @Transactional(readOnly=true)
    public Vorlage vorlage(Long objekt,Vorgang vorgang){tenant();objekt(objekt);return vorlagen.findByMietobjektIdAndVorgang(objekt,vorgang).map(this::lesen).orElse(null);}
    private void objekt(Long id){if(!objekte.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Mietobjekt nicht gefunden.");}
    public Vorlage speichern(Long objekt,Vorgang vorgang,Vorlage input){
        tenant();objekt(objekt);input=normalisieren(input);pruefe(input);var v=vorlagen.findByMietobjektIdAndVorgang(objekt,vorgang).orElseGet(MietobjektVorlage::new);
        if(!Objects.equals(v.getVersion(),input.version()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Vorlage wurde inzwischen geändert. Bitte neu laden.");
        v.setMietobjektId(objekt);v.setVorgang(vorgang);v.setInhalt(json(input));vorlagen.saveAndFlush(v);return lesen(v);
    }
    @Transactional(readOnly=true)
    public Ergebnis erstellen(Buchung b,ReservierungsbestaetigungPdf.Inhalt basis,Vorlage entwurf){
        return erstellenVorgang(b,basis,entwurf,Vorgang.RESERVIERUNG,Map.of(),Set.of(),null);
    }
    public Ergebnis erstellenVorgang(Buchung b,ReservierungsbestaetigungPdf.Inhalt basis,Vorlage entwurf,Vorgang vorgang,
            Map<String,String> extra,Set<Bedingung> bedingungen,Abrechnungsbereich tabelle){
        tenant();var papier=briefpapier.findById(1L).orElseThrow(()->fehler("Bitte Mandanten-Briefpapier einrichten."));
        var gespeichert=vorlagen.findByMietobjektIdAndVorgang(b.getMietobjekt().getId(),vorgang).orElse(null);
        Vorlage v=normalisieren(entwurf!=null?entwurf:gespeichert==null?null:lesen(gespeichert));
        if(v==null)throw fehler("Keine Vorlage für den Vorgang "+vorgang+" an diesem Mietobjekt hinterlegt.");pruefe(v);
        var f=new LinkedHashMap<>(basis.felder());var d=new TreeMap<String,String>();var datum=DateTimeFormatter.ofPattern("dd.MM.yyyy");
        var miete=BuchungPreiskalkulation.miete(b);var rest=Zahlungsplan.restmiete(b);
        d.put("buchungsnummer",b.getBuchungsnummer());d.put("vorname",b.getMieter().getVorname());d.put("nachname",b.getMieter().getName());
        d.put("anreise",datum.format(b.getAnreise()));d.put("abreise",datum.format(b.getAbreise()));d.put("briefdatum",f.get("briefdatum"));
        d.put("zahlungsfrist",datum.format(Zahlungsplan.frist(b.getAnreise().toLocalDate(), LocalDate.parse(f.get("briefdatum"),datum))));d.put("mietobjekt",b.getMietobjekt().getBezeichnung());
        d.put("mietbetrag",geld(miete));d.put("anzahlung",geld(Zahlungsplan.anzahlung(b)));d.put("kaution",geld(b.getKautionBetrag()));d.put("restmiete",geld(rest));
        d.put("bank_name",f.get("bank_name").replaceFirst("^Bank: ",""));d.put("bank_iban",f.get("bank_iban").replaceFirst("^IBAN: ",""));d.put("bank_bic",f.get("bank_bic").replaceFirst("^BIC: ",""));d.put("hausregeln_url",basis.hausregelnUrl());
        var wahr=EnumSet.of(Bedingung.IMMER,basis.ogGebucht()?Bedingung.OG_GEBUCHT:Bedingung.OG_NICHT_GEBUCHT);
        if(Zahlungsplan.anzahlung(b).signum()>0)wahr.add(Bedingung.ANZAHLUNG_POSITIV);
        if(b.getKautionBetrag().signum()>0)wahr.add(Bedingung.KAUTION_POSITIV);
        if(rest.signum()>0)wahr.add(Bedingung.RESTMIETE_POSITIV);
        LocalDate heute=LocalDate.parse(f.get("briefdatum"),datum);
        wahr.add(b.getAnreise().toLocalDate().isBefore(heute.plusWeeks(8))?Bedingung.STORNO_UNTER_56_TAGEN:Bedingung.STORNO_AB_56_TAGEN);
        wahr.add(Bedingung.valueOf("ZAHLUNG_"+b.getZahlungsvariante().name()));
        d.put("organisation",Objects.toString(b.getMieter().getOrganisation(), ""));
        if(vorgang==Vorgang.BESTAETIGUNG || b.getBuchungsquelle()==com.kcserver.vermietung.enumtype.Buchungsquelle.AIRBNB){wahr.remove(Bedingung.ANZAHLUNG_POSITIV);wahr.remove(Bedingung.RESTMIETE_POSITIV);wahr.remove(Bedingung.KAUTION_POSITIV);wahr.remove(Bedingung.ZAHLUNG_GESAMTMIETE_VORAUS);}
        if(miete.signum()==0)wahr.remove(Bedingung.ZAHLUNG_GESAMTMIETE_VORAUS);
        d.putAll(extra);wahr.addAll(bedingungen);
        f.put("anrede",ersetzen(v.anrede(),d));
        var absaetze=texte(v,d,wahr);String gruss=ersetzen(papier.getGruss(),d);
        var rows=new ArrayList<String>();
        for(var e:EntschaedigungPositionService.rows(b))rows.add(e.anzahl().toPlainString()+" · "+e.bezeichnung()+" · "+e.anlass().name()+" · "+geld(e.einzelpreis())+" € · "+geld(e.gesamtbetrag())+" €");
        var values=new ArrayList<Object>();Collections.addAll(values,TenantContext.getCurrentTenant(),b.getId(),b.getMietobjekt().getId(),papier.getVersion(),v,f,d,absaetze,gruss,wahr,rows);
        String metadata=json(values);String fingerprint=hash(metadata);
        byte[] pdf=tabelle==null?BriefpapierPdf.render(papier.getPdf(),f,absaetze,gruss,papier.getBild(),rows,b.getBuchungsnummer())
            :AbrechnungBriefLayout.render(papier.getPdf(),f,absaetze,gruss,papier.getBild(),b.getBuchungsnummer(),tabelle);
        return new Ergebnis(pdf,ersetzen(v.betreff(),d),ersetzen(v.mailtext(),d),fingerprint,metadata);
    }
    /** Vollständige Vorlage mit synthetischen Daten, ohne Buchungszugriff oder Versand. */
    // Import staging collects expected layout errors without poisoning its transaction.
    @Transactional(readOnly=true, noRollbackFor=ResponseStatusException.class)
    public Ergebnis beispielVorschau(Long objektId,Vorgang vorgang,Vorlage entwurf,boolean ogGebucht,boolean kurzfristig) {
        tenant();
        var papier=briefpapier.findById(1L).orElseThrow(()->fehler("Bitte Mandanten-Briefpapier einrichten und speichern."));
        return beispielVorschau(objektId,vorgang,entwurf,ogGebucht,kurzfristig,papier);
    }
    @Transactional(readOnly=true, noRollbackFor=ResponseStatusException.class)
    public Ergebnis beispielVorschauMitBriefpapier(Long objektId,Vorgang vorgang,Vorlage entwurf,boolean ogGebucht,boolean kurzfristig,
            Long version,String gruss,byte[] pdf,byte[] bild,boolean bildEntfernen) {
        return beispielVorschau(objektId,vorgang,entwurf,ogGebucht,kurzfristig,papierEntwurf(version,gruss,pdf,bild,bildEntfernen));
    }
    public static List<String> szenarien(Vorgang vorgang){return switch(vorgang){
        case RESERVIERUNG -> List.of("ANZAHLUNG","GESAMTMIETE_VORAUS","BEI_ABRECHNUNG");
        case ABGERECHNET -> List.of("RESTZAHLUNG","ERSTATTUNG","AUSGEGLICHEN");
        case STORNIERUNG -> List.of("STORNO_FRUEH","STORNO_KURZ","STORNO_OHNE_ZAHLUNG");
        default -> List.of("STANDARD");};}
    @Transactional(readOnly=true,noRollbackFor=ResponseStatusException.class)
    public Ergebnis beispielVorschauSzenario(Long objekt,Vorgang vorgang,Vorlage entwurf,boolean og,boolean kurz,String szenario,
        boolean neuesPapier,Long version,String gruss,byte[] pdf,byte[] bild,boolean bildEntfernen){
        tenant();if(!szenarien(vorgang).contains(szenario))throw fehler("Unbekanntes Beispielszenario für diesen Vorgang.");
        var p=neuesPapier?papierEntwurf(version,gruss,pdf,bild,bildEntfernen):briefpapier.findById(1L).orElseThrow(()->fehler("Bitte Briefpapier einrichten."));
        return beispielVorschau(objekt,vorgang,entwurf,og,kurz,p,szenario);
    }
    private Ergebnis beispielVorschau(Long objektId,Vorgang vorgang,Vorlage entwurf,boolean ogGebucht,boolean kurzfristig,MandantenBriefpapier papier){
        return beispielVorschau(objektId,vorgang,entwurf,ogGebucht,kurzfristig,papier,
            vorgang==Vorgang.RESERVIERUNG?(kurzfristig?"GESAMTMIETE_VORAUS":"ANZAHLUNG"):vorgang==Vorgang.ABGERECHNET?"ERSTATTUNG":vorgang==Vorgang.STORNIERUNG?(kurzfristig?"STORNO_KURZ":"STORNO_FRUEH"):"STANDARD");
    }
    private Ergebnis beispielVorschau(Long objektId,Vorgang vorgang,Vorlage entwurf,boolean ogGebucht,boolean kurzfristig,MandantenBriefpapier papier,String szenario) {
        var objekt=objekte.findById(objektId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Mietobjekt nicht gefunden."));
        Vorlage v=normalisieren(entwurf!=null?entwurf:vorlage(objektId,vorgang));
        if(v==null)throw fehler("Keine Vorlage für diesen Dokumentvorgang hinterlegt.");
        pruefe(v);
        var datum=DateTimeFormatter.ofPattern("dd.MM.yyyy");
        var heute=LocalDate.now(ZoneId.of("Europe/Berlin"));
        var anreise=heute.plusDays(szenario.equals("STORNO_FRUEH")?90:szenario.equals("STORNO_KURZ")?30:kurzfristig?30:90);
        var d=new TreeMap<String,String>();
        d.put("buchungsnummer",heute.getYear()+"-BEISPIEL");
        d.put("vorname","Erika");d.put("nachname","Mustermann");
        d.put("anreise",datum.format(anreise));d.put("abreise",datum.format(anreise.plusDays(3)));
        d.put("briefdatum",datum.format(heute));d.put("zahlungsfrist",datum.format(anreise.minusDays(7)));
        d.put("mietobjekt",objekt.getBezeichnung());
        d.put("mietbetrag","500,00");d.put("anzahlung",kurzfristig?"0,00":"150,00");d.put("restmiete",kurzfristig?"0,00":"350,00");d.put("kaution","200,00");
        d.put("bank_name","Musterbank");d.put("bank_iban","DE00000000000000000000");d.put("bank_bic","MUSTER00");
        d.put("hausregeln_url","https://example.org/hausregeln");
        var f=new HashMap<String,String>();
        f.put("empfaenger_name","Erika Mustermann");f.put("empfaenger_strasse","Musterstraße 12");f.put("empfaenger_ort","52076 Aachen");
        f.put("briefdatum",d.get("briefdatum"));f.put("buchungsnummer",d.get("buchungsnummer"));f.put("anrede",ersetzen(v.anrede(),d));
        f.put("bank_name","Bank: "+d.get("bank_name"));f.put("bank_iban","IBAN: "+d.get("bank_iban"));f.put("bank_bic","BIC: "+d.get("bank_bic"));
        var wahr=EnumSet.of(Bedingung.IMMER,Bedingung.ANZAHLUNG_POSITIV,Bedingung.KAUTION_POSITIV,Bedingung.RESTMIETE_POSITIV,
                ogGebucht?Bedingung.OG_GEBUCHT:Bedingung.OG_NICHT_GEBUCHT,
                kurzfristig?Bedingung.STORNO_UNTER_56_TAGEN:Bedingung.STORNO_AB_56_TAGEN);
        d.put("organisation","Beispielverein e.V.");d.put("abrechnungssumme",ogGebucht?"901,00":"651,00");d.put("restzahlung","0,00");d.put("erstattung",ogGebucht?"99,00":"349,00");d.put("saldo",ogGebucht?"-99,00":"-349,00");
        d.put("mieter_bank_name","Musterbank");d.put("mieter_bank_iban","DE00000000000000000000");d.put("mieter_bank_bic","MUSTER00");d.put("mieter_kontoinhaber","Beispielverein e.V.");
        wahr.remove(Bedingung.ANZAHLUNG_POSITIV);wahr.remove(Bedingung.RESTMIETE_POSITIV);
        if(szenario.equals("ANZAHLUNG")){wahr.add(Bedingung.ZAHLUNG_ANZAHLUNG);wahr.add(Bedingung.ANZAHLUNG_POSITIV);wahr.add(Bedingung.RESTMIETE_POSITIV);d.put("anzahlung","150,00");d.put("restmiete","350,00");}
        else {d.put("anzahlung","0,00");d.put("restmiete","0,00");}
        if(szenario.equals("GESAMTMIETE_VORAUS"))wahr.add(Bedingung.ZAHLUNG_GESAMTMIETE_VORAUS);
        if(szenario.equals("BEI_ABRECHNUNG")){wahr.add(Bedingung.ZAHLUNG_BEI_ABRECHNUNG);wahr.remove(Bedingung.KAUTION_POSITIV);}
        if(vorgang==Vorgang.BESTAETIGUNG)wahr.remove(Bedingung.KAUTION_POSITIV);
        BigDecimal saldo=szenario.equals("RESTZAHLUNG")?new BigDecimal("100.00"):szenario.equals("AUSGEGLICHEN")?BigDecimal.ZERO:ogGebucht?new BigDecimal("-99.00"):new BigDecimal("-349.00");
        if(vorgang==Vorgang.ABGERECHNET){d.put("mietbetrag",ogGebucht?"765,00":"515,00");d.put("kaution","0,00");wahr.remove(Bedingung.KAUTION_POSITIV);d.put("zahlungsfrist",datum.format(heute.plusDays(14)));d.put("saldo",geld(saldo));d.put("erstattung",geld(saldo.negate().max(BigDecimal.ZERO)));d.put("restzahlung",geld(saldo.max(BigDecimal.ZERO)));wahr.add(saldo.signum()<0?Bedingung.ERSTATTUNG_POSITIV:saldo.signum()>0?Bedingung.RESTZAHLUNG_POSITIV:Bedingung.AUSGEGLICHEN);}
        d.put("stornodatum",datum.format(heute));d.put("storno_gezahlter_betrag",szenario.equals("STORNO_OHNE_ZAHLUNG")?"0,00":"1000,00");d.put("storno_bereits_erstattet","0,00");d.put("storno_erstattung",szenario.equals("STORNO_FRUEH")?"1000,00":"0,00");d.put("storno_einbehalt",szenario.equals("STORNO_KURZ")?"1000,00":"0,00");
        if(vorgang==Vorgang.STORNIERUNG){
            wahr.remove(Bedingung.STORNO_UNTER_56_TAGEN);wahr.remove(Bedingung.STORNO_AB_56_TAGEN);
            wahr.add(szenario.equals("STORNO_KURZ")?Bedingung.STORNO_UNTER_56_TAGEN:Bedingung.STORNO_AB_56_TAGEN);
            wahr.add(szenario.equals("STORNO_FRUEH")?Bedingung.STORNO_ERSTATTUNG_POSITIV:szenario.equals("STORNO_KURZ")?Bedingung.STORNO_EINBEHALT_POSITIV:Bedingung.STORNO_OHNE_ZAHLUNG);
        }
        var absaetze=texte(v,d,wahr);
        var gruss=ersetzen(papier.getGruss(),d);
        var metadata=json(Arrays.asList(vorgang,v,d,wahr,papier.getVersion()));
        var pdf=vorgang==Vorgang.ABGERECHNET ? AbrechnungBriefLayout.render(papier.getPdf(),f,absaetze,gruss,papier.getBild(),d.get("buchungsnummer"),
            beispielAbrechnung(ogGebucht,saldo))
            : vorgang==Vorgang.STORNIERUNG?AbrechnungBriefLayout.render(papier.getPdf(),f,absaetze,gruss,papier.getBild(),d.get("buchungsnummer"),beispielStorno(szenario))
            : BriefpapierPdf.render(papier.getPdf(),f,absaetze,gruss,papier.getBild(),List.of(),d.get("buchungsnummer"));
        return new Ergebnis(pdf,ersetzen(v.betreff(),d),ersetzen(v.mailtext(),d),hash(metadata),metadata);
    }

    private static Abrechnungsbereich beispielStorno(String szenario){
        var rows=new ArrayList<Abrechnungsbereich.Zeile>();
        if(szenario.equals("STORNO_OHNE_ZAHLUNG"))rows.add(new Abrechnungsbereich.Zeile("Keine geleisteten Zahlungen","","",""));
        else {rows.add(new Abrechnungsbereich.Zeile("Gezahlt am 24.02.2025 (Anzahlung)","","","50,00 €"));rows.add(new Abrechnungsbereich.Zeile("Gezahlt am 01.09.2025 (Miete)","","","950,00 €"));rows.add(new Abrechnungsbereich.Zeile(szenario.equals("STORNO_FRUEH")?"Noch zu erstatten":"Einbehalt nach Stornoregel","","","1000,00 €"));}
        return new Abrechnungsbereich(rows);
    }
    private static Abrechnungsbereich beispielAbrechnung(boolean og,BigDecimal saldo) {
        var rows=new ArrayList<Abrechnungsbereich.Zeile>();int nr=0;
        rows.add(new Abrechnungsbereich.Zeile((++nr)+". Bootshaus EG","2 Tage","250,00 €","500,00 €"));
        if(og)rows.add(new Abrechnungsbereich.Zeile((++nr)+". Schlafräume OG","2 Tage","125,00 €","250,00 €"));
        rows.add(new Abrechnungsbereich.Zeile((++nr)+". Grill","1","15,00 €","15,00 €"));
        rows.add(new Abrechnungsbereich.Zeile((++nr)+". Strom","27 kWh","1,00 €","27,00 €"));
        rows.add(new Abrechnungsbereich.Zeile((++nr)+". Gas","35,6 m³","2,50 €","89,00 €"));
        rows.add(new Abrechnungsbereich.Zeile((++nr)+". Wasser","2 Tage","10,00 €","20,00 €"));
        rows.add(new Abrechnungsbereich.Zeile("Abrechnungssumme","","",og?"901,00 €":"651,00 €"));
        rows.add(new Abrechnungsbereich.Zeile("Bereits gezahlt am 24.02.2025","","","-50,00 €"));
        rows.add(new Abrechnungsbereich.Zeile("Bereits gezahlt am 01.09.2025","","",Abrechnungsbereich.geld((og?new BigDecimal("901"):new BigDecimal("651")).subtract(saldo).subtract(new BigDecimal("50")).negate())));
        rows.add(new Abrechnungsbereich.Zeile(saldo.signum()<0?"Rückerstattung":saldo.signum()>0?"Restzahlung":"Ausgeglichen","","",Abrechnungsbereich.geld(saldo.abs())));
        return new Abrechnungsbereich(List.copyOf(rows));
    }
    private static String geld(BigDecimal n){var f=java.text.NumberFormat.getNumberInstance(Locale.GERMANY);f.setMinimumFractionDigits(2);f.setMaximumFractionDigits(2);return f.format(n);}
}
