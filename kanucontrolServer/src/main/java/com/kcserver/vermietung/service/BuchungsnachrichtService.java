package com.kcserver.vermietung.service;

import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

/** Persistent, user-bound drafts. Business commit and immutable archive precede SMTP. */
@Service
public class BuchungsnachrichtService {
    private final BuchungsnachrichtEntwurfRepository entwuerfe;
    private final BuchungRepository buchungen;
    private final BuchungService buchungsservice;
    private final BuchungFinanzenService finanzen;
    private final VerbrauchsabrechnungService verbrauch;
    private final VorlagenService vorlagen;
    private final MandantenBriefpapierRepository papier;
    private final AbrechnungVorlagenService rechnung;
    private final VermietungAbrechnungRepository rechnungen;
    private final HomepageStatusMailService mails;
    private final VorlagenVersandRepository archiv;
    private final VorlagenArchivService archivstatus;
    private final BuchungsnachrichtDaten daten;
    private final ObjectMapper mapper;
    private final TransactionTemplate tx;
    public BuchungsnachrichtService(BuchungsnachrichtEntwurfRepository e,BuchungRepository b,BuchungService bs,
        BuchungFinanzenService f,VerbrauchsabrechnungService v,VorlagenService vs,MandantenBriefpapierRepository p,
        AbrechnungVorlagenService r,VermietungAbrechnungRepository rr,HomepageStatusMailService m,
        VorlagenVersandRepository a,VorlagenArchivService as,BuchungsnachrichtDaten d,ObjectMapper om,PlatformTransactionManager tm){
        entwuerfe=e;buchungen=b;buchungsservice=bs;finanzen=f;verbrauch=v;vorlagen=vs;papier=p;rechnung=r;rechnungen=rr;
        mails=m;archiv=a;archivstatus=as;daten=d;mapper=om;tx=new TransactionTemplate(tm);
    }
    public record Start(@jakarta.validation.constraints.NotNull Buchungsstatus ziel,List<@jakarta.validation.Valid @jakarta.validation.constraints.NotNull AblesungBestaetigungDTO> ablesungen,String korrekturgrund) {}
    public record Aenderung(Long version,String betreff,String mailtext,boolean originalWiederherstellen) {}
    public record Freigabe(String fingerprint,boolean wiederholung) {}
    public record Ansicht(String id,Long version,Buchungsstatus quelle,Buchungsstatus ziel,String vorgang,boolean ohneMail,
        String betreff,String mailtext,HomepageStatusMailService.StatusVorschau routing,byte[] pdf,String dateiname,
        Instant externAm,String pdfHash,String fingerprint,boolean ausgefuehrt,String versuchId,String ergebnis,String fehler,Stornoberechnung storno) {}
    public record Kurz(String id,Buchungsstatus ziel,boolean ausgefuehrt,String ergebnis,Instant ablauf) {}
    private record Ausgang(Ansicht ansicht,String archivId,HomepageStatusMailService.ReservierungMail mail) {}
    private static LocalDate heute(){return LocalDate.now(ZoneId.of("Europe/Berlin"));}
    private static String user(){VorlagenService.tenant();var a=SecurityContextHolder.getContext().getAuthentication();
        if(a==null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return a.getName();}
    private String json(Object v){try{return mapper.writeValueAsString(v);}catch(Exception e){throw new IllegalStateException(e);}}
    private <T>T lesen(String s,Class<T> c){try{return mapper.readValue(s,c);}catch(Exception e){throw new IllegalStateException(e);}}
    private String hash(byte[] bytes){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}catch(Exception e){throw new IllegalStateException(e);}}
    private Buchung buchung(Long id){return buchungen.findLockedById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Buchung fehlt."));}
    private BuchungsnachrichtEntwurf entwurf(Long buchung,String id){return entwuerfe.gesperrt(id,user(),TenantContext.getCurrentTenant(),buchung).orElseThrow(()->new ResponseStatusException(HttpStatus.GONE,"Entwurf nicht verfügbar oder einem anderen Benutzer/Mandanten zugeordnet."));}
    private List<AblesungBestaetigungDTO> ablesungen(BuchungsnachrichtEntwurf e){return Arrays.asList(lesen(e.getAblesungen(),AblesungBestaetigungDTO[].class));}
    private List<BuchungVerbrauchskosten> snapshots(Buchung b,BuchungsnachrichtEntwurf e){return e.getZiel()==Buchungsstatus.ABGESCHLOSSEN?verbrauch.berechneSnapshots(b,ablesungen(e)):null;}
    private void offen(BuchungsnachrichtEntwurf e){if(e.isAusgefuehrt())throw konflikt("Diese Aktion wurde bereits ausgeführt. Für erneuten Versand den gespeicherten Versuch prüfen.");if(e.getAblauf().isBefore(Instant.now()))throw konflikt("Entwurf abgelaufen. Bitte neu erstellen.");}
    private static ResponseStatusException konflikt(String s){return new ResponseStatusException(HttpStatus.CONFLICT,s);}
    /** Includes addresses, routing, raw snapshots/confirmations, exact finance and template revisions. No JPA recursion. */
    private String zustand(Buchung b,BuchungsnachrichtEntwurf e,List<BuchungVerbrauchskosten> snapshots){
        var f=snapshots==null?finanzen.toDto(b):finanzen.toDto(b,snapshots);var p=b.getMieter();
        var values=new ArrayList<Object>();Collections.addAll(values,TenantContext.getCurrentTenant(),b.getId(),b.getStatus(),b.getAbrechnungRevision(),b.getAbschlussRevision(),
            b.getZahlungsvariante(),b.getBankverbindungAuswahl(),b.getAnreise(),b.getAbreise(),b.getStornoDatum(),b.getStornoErstattung(),b.getStornoEinbehalt(),f,
            Arrays.asList(p.getId(),p.getVorname(),p.getName(),p.getOrganisation(),p.getStrasse(),p.getPlz(),p.getOrt(),p.getCountryCode(),p.getEmail(),p.getIban(),p.getBankName(),p.getBic(),p.getKontoinhaber()),
            b.getMietobjekt().getBezeichnung(),b.getMietobjekt().getBankverbindungPrimaer(),b.getMietobjekt().getBankverbindungAlternativ());
        if(!e.isOhneMail()){
            values.add(vorlagen.vorlage(b.getMietobjekt().getId(),BuchungsnachrichtDaten.vorgang(e.getZiel())));
            var papierstand=papier.findById(1L).orElseThrow(()->fehler("Bitte Briefpapier einrichten."));
            values.add(Arrays.asList(papierstand.getVersion(),hash(papierstand.getPdf()),papierstand.getBild()==null?null:hash(papierstand.getBild()),papierstand.getGruss()));
            values.add(mails.nachrichtVorschau(b,"", ""));values.add(daten.basis(b,e.getDatum()));
        }
        if(snapshots!=null)for(var v:snapshots)values.add(Arrays.asList(v.getVerbrauchskostenId(),v.getZaehlerId(),v.getBezeichnung(),v.getPreis(),v.getKosten(),v.getAnfangsstand(),v.getEndstand(),v.getAnfangsablesung(),v.getEndablesung(),v.getMengeneinheit(),v.getAbrechnungstage()));
        return VorlagenService.hash(json(values));
    }
    private void aktuell(Buchung b,BuchungsnachrichtEntwurf e,List<BuchungVerbrauchskosten> snapshots){
        if(!e.isAusgefuehrt() && e.getZiel()==Buchungsstatus.STORNIERT && !e.getDatum().equals(heute()))throw konflikt("Das Stornodatum hat sich geändert. Bitte neuen Entwurf prüfen.");
        if(!Objects.equals(e.getDatenHash(),zustand(b,e,snapshots)))throw konflikt("Buchung, Zahlungen, Verbrauch, Vorlage oder Empfänger wurden geändert. Bitte neuen Entwurf erstellen und prüfen.");
    }
    public Ansicht erstellen(Long id,Start request){user();return tx.execute(status->{
        if(request==null || request.ziel()==null)throw fehler("Zielstatus fehlt.");
        var b=buchung(id);buchungsservice.pruefeStatuswechsel(b,request.ziel());
        var f=finanzen.toDto(b);BuchungsnachrichtDaten.zahlungsplanPruefen(b,request.ziel(),f.bewegungen());
        var e=new BuchungsnachrichtEntwurf();e.setBenutzer(user());e.setTenant(TenantContext.getCurrentTenant());e.setBuchungId(id);
        e.setQuelle(b.getStatus());e.setZiel(request.ziel());e.setDatum(heute());e.setAblauf(Instant.now().plusSeconds(1800));
        e.setOhneMail(BuchungsnachrichtDaten.ohneMail(b.getStatus(),request.ziel()));e.setAblesungen(json(request.ablesungen()==null?List.of():request.ablesungen()));
        e.setDateiname("Buchung_"+id+"_"+BuchungsnachrichtDaten.vorgang(request.ziel())+".pdf");
        var sn=snapshots(b,e);
        if(request.ziel()==Buchungsstatus.ABGESCHLOSSEN){
            buchungsservice.pruefeKautionsabschluss(b);
            var old=rechnungen.findByBuchungIdOrderByRevisionDesc(id);e.setRechnungRevision(old.isEmpty()?1:old.getFirst().getRevision()+1);
            e.setErsetztDokumentId(old.isEmpty()?null:old.getFirst().getId());String grund=Objects.toString(request.korrekturgrund(),"").strip();
            if(grund.length()>1000 || !old.isEmpty() && grund.isBlank())throw fehler("Eine Rechnungskorrektur benötigt eine Begründung (maximal 1000 Zeichen).");e.setKorrekturgrund(grund);
        }
        e.setRouting(json(e.isOhneMail()?null:mails.nachrichtVorschau(b,"","")));
        if(!e.isOhneMail()){
            VorlagenService.Ergebnis result;
            if(e.getZiel()==Buchungsstatus.ABGESCHLOSSEN)result=rechnung.entwurf(b,e.getRechnungRevision(),null,finanzen.toDto(b,sn),sn,e.getDatum());
            else {
                var extra=new HashMap<String,String>();var wahr=EnumSet.noneOf(Bedingung.class);Abrechnungsbereich tabelle=null;
                if(e.getZiel()==Buchungsstatus.STORNIERT){var st=Stornoberechnung.berechnen(e.getDatum(),b.getAnreise().toLocalDate(),f.bewegungen());e.setStorno(json(st));extra.putAll(daten.stornoDaten(b,st));
                    wahr.add(st.erstattung().signum()>0?Bedingung.STORNO_ERSTATTUNG_POSITIV:st.einbehalt().signum()>0?Bedingung.STORNO_EINBEHALT_POSITIV:Bedingung.STORNO_OHNE_ZAHLUNG);tabelle=daten.stornoTabelle(st,f.bewegungen());}
                result=vorlagen.erstellenVorgang(b,daten.basis(b,e.getDatum()),null,BuchungsnachrichtDaten.vorgang(e.getZiel()),extra,wahr,tabelle);
            }
            e.setBetreff(result.betreff());e.setMailtext(result.mailtext());e.setOriginalPdf(result.pdf().clone());e.setPdf(result.pdf().clone());
        }
        e.setDatenHash(zustand(b,e,sn));entwuerfe.saveAndFlush(e);return ansicht(e);
    });}
    private String fingerprint(BuchungsnachrichtEntwurf e){return VorlagenService.hash(json(Arrays.asList(e.getId(),e.getVersion(),e.getDatenHash(),e.getBetreff(),e.getMailtext(),e.getRouting(),e.getDateiname(),e.getPdf()==null?null:hash(e.getPdf()),e.getExternAm(),e.isAusgefuehrt(),e.getVersuchId())));}
    private Ansicht ansicht(BuchungsnachrichtEntwurf e){var a=e.getVersuchId()==null?null:archiv.findById(e.getVersuchId()).orElseThrow();
        var route=e.isOhneMail()?null:lesen(e.getRouting(),HomepageStatusMailService.StatusVorschau.class);
        if(route!=null)route=new HomepageStatusMailService.StatusVorschau(route.absender(),route.empfaenger(),route.cc(),route.replyTo(),(route.testmodus()&&!e.getBetreff().startsWith("[TEST] ")?"[TEST] ":"")+e.getBetreff(),e.getMailtext(),route.testmodus(),null);
        return new Ansicht(e.getId(),e.getVersion(),e.getQuelle(),e.getZiel(),BuchungsnachrichtDaten.vorgang(e.getZiel()).name(),e.isOhneMail(),e.getBetreff(),e.getMailtext(),route,e.getPdf(),e.getDateiname(),e.getExternAm(),e.getPdf()==null?null:hash(e.getPdf()),fingerprint(e),e.isAusgefuehrt(),e.getVersuchId(),a==null?null:a.getErgebnis(),a==null?null:a.getFehler(),e.getStorno()==null?null:lesen(e.getStorno(),Stornoberechnung.class));}
    public Ansicht lesen(Long buchung,String id){user();return tx.execute(status->{var e=entwurf(buchung,id);aktuell(buchung(buchung),e,snapshots(buchung(buchung),e));return ansicht(e);});}
    public List<Kurz> liste(Long id){user();return tx.execute(status->entwuerfe.findByBuchungIdAndBenutzerAndTenantOrderByAblaufDesc(id,user(),TenantContext.getCurrentTenant()).stream().filter(e->e.isAusgefuehrt() || e.getAblauf().isAfter(Instant.now())).map(e->new Kurz(e.getId(),e.getZiel(),e.isAusgefuehrt(),e.getVersuchId()==null?null:archiv.findById(e.getVersuchId()).map(VorlagenVersand::getErgebnis).orElse(null),e.getAblauf())).toList());}
    public Ansicht aendern(Long b,String id,Aenderung a){user();return tx.execute(status->{var e=entwurf(b,id);offen(e);aktuell(buchung(b),e,snapshots(buchung(b),e));
        if(a==null || !Objects.equals(a.version(),e.getVersion()))throw konflikt("Entwurf wurde geändert. Bitte neu laden.");
        pruefeMail(a.betreff(),a.mailtext());if(!e.isOhneMail()){e.setBetreff(a.betreff().strip());e.setMailtext(normalisieren(a.mailtext()));}
        if(a.originalWiederherstellen() && !e.isOhneMail()){e.setPdf(e.getOriginalPdf().clone());e.setExternAm(null);e.setDateiname("Buchung_"+b+"_"+BuchungsnachrichtDaten.vorgang(e.getZiel())+".pdf");}
        entwuerfe.saveAndFlush(e);return ansicht(e);
    });}
    public Ansicht hochladen(Long b,String id,Long version,byte[] pdf,String name){user();BriefpapierPdf.pruefeAnhang(pdf);return tx.execute(status->{var e=entwurf(b,id);offen(e);aktuell(buchung(b),e,snapshots(buchung(b),e));
        if(e.isOhneMail())throw fehler("Diese Aktion versendet keinen Anhang.");if(!Objects.equals(version,e.getVersion()))throw konflikt("Entwurf wurde geändert. Bitte neu laden.");
        String filename=Objects.toString(name,"Bearbeitet.pdf").replaceAll("[^\\p{L}\\p{N}_. -]","_");if(filename.length()>160)filename=filename.substring(0,160);if(!filename.toLowerCase(Locale.ROOT).endsWith(".pdf"))filename+=".pdf";
        e.setDateiname(filename);e.setPdf(pdf.clone());e.setExternAm(Instant.now());entwuerfe.saveAndFlush(e);return ansicht(e);
    });}
    private static void pruefeMail(String betreff,String text){
        if(betreff==null || betreff.isBlank() || betreff.length()>240 || betreff.contains("\n") || betreff.contains("\r") || betreff.codePoints().anyMatch(Character::isISOControl))throw fehler("Betreff benötigt 1 bis 240 Zeichen ohne Zeilenumbruch.");
        if(text==null || text.isBlank() || text.length()>30000 || text.codePoints().anyMatch(c->Character.isISOControl(c)&&c!='\n'&&c!='\r'&&c!='\t'))throw fehler("Mailtext fehlt oder enthält unzulässige Zeichen (maximal 30000 Zeichen).");
    }
    public Ansicht ausfuehren(Long id,String entwurfId,Freigabe freigabe){
        user();Ausgang result=tx.execute(status->{var e=entwurf(id,entwurfId);var b=buchung(id);var sn=snapshots(b,e);aktuell(b,e,sn);
            if(freigabe==null || !Objects.equals(freigabe.fingerprint(),fingerprint(e)))throw konflikt("Die Nachricht wurde geändert. Bitte aktuelle Vorschau prüfen.");
            if(e.isAusgefuehrt()){
                if(e.isOhneMail())return new Ausgang(ansicht(e),null,null);
                var old=archiv.findById(e.getVersuchId()).orElseThrow();
                if(!freigabe.wiederholung())return new Ausgang(ansicht(e),null,null);
                if("SMTP_BESTAETIGT".equals(old.getErgebnis()))throw konflikt("SMTP hat diesen Versand bereits bestätigt. Keine Wiederholung erforderlich.");
                if("VORBEREITET".equals(old.getErgebnis()) && old.getErstelltAm().isAfter(Instant.now().minusSeconds(300)))throw konflikt("Versand läuft oder ist noch unbestätigt. Bitte vor einer Wiederholung warten und Postausgang prüfen.");
            }else{
                offen(e);buchungsservice.pruefeStatuswechsel(b,e.getZiel());BuchungsnachrichtDaten.zahlungsplanPruefen(b,e.getZiel(),finanzen.toDto(b).bewegungen());
            }
            var view=ansicht(e);HomepageStatusMailService.ReservierungMail mail=e.isOhneMail()?null:mails.nachrichtVorbereiten(view.routing(),e.getPdf(),e.getDateiname(),json(view));
            if(!e.isAusgefuehrt()){
                if(e.getStorno()!=null){var st=lesen(e.getStorno(),Stornoberechnung.class);b.setStornoDatum(st.datum());b.setStornoErstattung(st.erstattung());b.setStornoEinbehalt(st.einbehalt());}
                buchungsservice.anwendenGeprueft(id,e.getZiel(),ablesungen(e));
                if(e.getZiel()==Buchungsstatus.ABGESCHLOSSEN){var old=rechnungen.findByBuchungIdOrderByRevisionDesc(id);
                    if(!Objects.equals(e.getErsetztDokumentId(),old.isEmpty()?null:old.getFirst().getId()))throw konflikt("Rechnungsstand wurde geändert. Bitte neuen Entwurf erstellen.");
                    var doc=new VermietungAbrechnung();doc.setBuchung(b);doc.setRevision(e.getRechnungRevision());doc.setErstellt(Instant.now());doc.setKorrekturgrund(e.getKorrekturgrund());doc.setPdf(e.getPdf().clone());rechnungen.saveAndFlush(doc);}
                e.setAusgefuehrt(true);e.setDatenHash(zustand(b,e,sn));
            }
            String archivId=null;
            if(mail!=null){var a=new VorlagenVersand();a.setBuchungId(id);a.setMietobjektId(b.getMietobjekt().getId());a.setVorgang(BuchungsnachrichtDaten.vorgang(e.getZiel()).name());a.setPdf(e.getPdf().clone());a.setMetadaten(json(view));archivId=archiv.saveAndFlush(a).getId();e.setVersuchId(archivId);}
            entwuerfe.saveAndFlush(e);return new Ausgang(ansicht(e),archivId,mail);
        });
        if(result.mail()!=null){
            try{mails.reservierungSenden(result.mail());}
            catch(RuntimeException ex){try{archivstatus.ergebnis(result.archivId(),false,"SMTP nicht bestätigt. Vor Wiederholung Postausgang prüfen.");}catch(RuntimeException ignored){/* VORBEREITET remains an explicit unknown outcome. */}return lesen(id,entwurfId);}
            // Never mark a confirmed SMTP attempt as failed when only metadata persistence fails.
            archivstatus.ergebnis(result.archivId(),true,null);
        }
        return lesen(id,entwurfId);
    }
}
