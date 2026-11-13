package com.kcserver.vermietung.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

/** Uses the existing draft store, renderer, routing and immutable send archive. */
@Service
public class BuchungsdokumentService {

    public static final int UPLOAD_LIMIT=10_000_000;
    private final BuchungsnachrichtEntwurfRepository entwuerfe;
    private final BuchungRepository buchungen;
    private final BuchungsnachrichtService nachrichten;
    private final HomepageStatusMailService mails;
    private final VorlagenVersandRepository archiv;
    private final VorlagenArchivService archivstatus;
    private final ObjectMapper mapper;
    private final TransactionTemplate tx;

    public BuchungsdokumentService(BuchungsnachrichtEntwurfRepository e,BuchungRepository b,BuchungsnachrichtService n,HomepageStatusMailService m,VorlagenVersandRepository a,VorlagenArchivService s,ObjectMapper om,PlatformTransactionManager tm){

        entwuerfe=e;
        buchungen=b;
        nachrichten=n;
        mails=m;
        archiv=a;
        archivstatus=s;
        mapper=om;
        tx=new TransactionTemplate(tm);

    }

    public record Fassung(String id,Long version,Buchungsstatus art,boolean gespeichert,String herkunft,Instant zeitpunkt,String benutzer,String dateiname,String pruefsumme,boolean simulation,boolean datenGeaendert,String betreff,String mailtext,byte[] pdf,List<String> hinweise,Instant letzterVersand,String versandstatus){}
    public record Uebersicht(List<Fassung> dokumente,int uploadLimit){}
    public record Speichern(String ersetztId){}
    public record Versandvorbereitung(String dokumentId,String pruefsumme,String routingHash,String mieter,HomepageStatusMailService.StatusVorschau routing,String betreff,String mailtext,String migrationsBetreff,String migrationsText,Fassung fassung,Buchungsstatus buchungsstatus){}
    public record Senden(String versuch,String pruefsumme,String routingHash,String betreff,String mailtext){}
    public record Versand(String id,String ergebnis,String fehler){}
    private record Ausgang(Versand result,HomepageStatusMailService.ReservierungMail mail){}
    private static String user(){

        VorlagenService.tenant();
        var a=SecurityContextHolder.getContext().getAuthentication();

        if(a==null||!a.isAuthenticated()||"anonymousUser".equals(a.getPrincipal())||a.getAuthorities().stream().noneMatch(x->Set.of("ROLE_ADMIN","ROLE_VERMIETUNG").contains(x.getAuthority())))throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return a.getName();

    }

    private static ResponseStatusException konflikt(String s){
        return new ResponseStatusException(HttpStatus.CONFLICT,s);
    }

    private Buchung buchung(Long id){
        return buchungen.findLockedById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Buchung fehlt."));
    }

    private BuchungsnachrichtEntwurf dokument(Long b,String id){
        return entwuerfe.dokument(id,user(),TenantContext.getCurrentTenant(),b).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Dokument nicht verfügbar."));
    }

    private String json(Object o){
        try{
            return mapper.writeValueAsString(o);
        }
        catch(Exception ex){
            throw new IllegalStateException(ex);
        }
    }

    private String archivAuftragHash(VorlagenVersand a){
        try{return mapper.readTree(a.getMetadaten()).path("auftragHash").asText();}
        catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException("Versandmetadaten sind nicht lesbar.",ex);}
    }

    private static String hash(byte[] bytes){
        try{
            return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));
        }
        catch(Exception ex){
            throw new IllegalStateException(ex);
        }
    }

    private List<BuchungsnachrichtEntwurf> aktuelle(Long id){
        return entwuerfe.findByBuchungIdAndTenantAndUnabhaengigTrueAndAktuelleFassungTrue(id,TenantContext.getCurrentTenant());
    }

    private BuchungsnachrichtEntwurf aktuelle(Long id,Buchungsstatus art){
        return aktuelle(id).stream().filter(e->e.getZiel()==art).findFirst().orElse(null);
    }

    private void ersetzung(BuchungsnachrichtEntwurf alt,String erwartet){
        if(!Objects.equals(alt==null?null:alt.getId(),erwartet))throw konflikt("Die gespeicherte Fassung wurde geändert. Bitte neu laden und Ersetzen bestätigen.");
    }

    private boolean geaendert(Buchung b,BuchungsnachrichtEntwurf e){
        try{
            return !Objects.equals(e.getDatenHash(),nachrichten.dokumentDatenstand(b,e));
        }
        catch(ResponseStatusException|IllegalArgumentException ex){
            return true;
        }
    }

    private Fassung ansicht(Buchung b,BuchungsnachrichtEntwurf e){

        List<String> hinweise;
        try{
            hinweise=Arrays.asList(mapper.readValue(e.getHinweise(),String[].class));
        }
        catch(Exception ex){
            throw new IllegalStateException(ex);
        }

        var a=e.getVersuchId()==null?null:archiv.findById(e.getVersuchId()).orElse(null);

        return new Fassung(e.getId(),e.getVersion(),e.getZiel(),e.isAktuelleFassung(),e.getExternAm()==null?"In KanuControl erzeugt":"Manuell hochgeladen",e.getDokumentAm(),e.getBenutzer(),e.getDateiname(),hash(e.getPdf()),e.getZiel()==Buchungsstatus.ABGESCHLOSSEN,geaendert(b,e),e.getBetreff(),e.getMailtext(),e.getPdf(),hinweise,a==null?null:a.getErstelltAm(),a==null?null:a.getErgebnis());

    }

    public Uebersicht liste(Long id){
        user();
        return tx.execute(s->{
            var b=buchung(id);
            return new Uebersicht(aktuelle(id).stream().map(e->ansicht(b,e)).toList(),UPLOAD_LIMIT);
        }
        );
    }

    public Fassung lesen(Long id,String fassung){
        user();
        return tx.execute(s->{
            var b=buchung(id);
            return ansicht(b,dokument(id,fassung));
        }
        );
    }

    public Fassung vorschau(Long id,Buchungsstatus art){
        user();
        return tx.execute(s->{

            var b=buchung(id);
            var v=nachrichten.vorschau(id,new BuchungsnachrichtService.Start(art,List.of(),null));
            var e=neu(b,art);

            e.setPdf(v.pdf().clone());
            e.setBetreff(v.betreff());
            e.setMailtext(v.mailtext());
            e.setHinweise(json(v.hinweise()));
            e.setDatenHash(nachrichten.dokumentDatenstand(b,e));
            entwuerfe.saveAndFlush(e);
            return ansicht(b,e);

        }
        );
    }

    private BuchungsnachrichtEntwurf neu(Buchung b,Buchungsstatus art){

        if(art==null)throw fehler("Dokumentart fehlt.");
        BuchungsnachrichtDaten.vorgang(art);
        var e=new BuchungsnachrichtEntwurf();
        e.setBuchungId(b.getId());
        e.setTenant(TenantContext.getCurrentTenant());
        e.setBenutzer(user());
        e.setQuelle(b.getStatus());
        e.setZiel(art);
        e.setUnabhaengig(true);
        e.setOhneMail(true);
        e.setRouting("null");
        e.setDatum(LocalDate.now(ZoneId.of("Europe/Berlin")));
        e.setDokumentAm(Instant.now());
        e.setAblauf(Instant.now().plusSeconds(86400));

        e.setDateiname(b.getBuchungsnummer()+"_"+BuchungsnachrichtDaten.vorgang(art)+(art==Buchungsstatus.ABGESCHLOSSEN?"_Simulation":"")+".pdf");
        return e;

    }

    public Fassung speichern(Long id,String fassung,String ersetztId){
        user();
        return tx.execute(s->{

            var b=buchung(id);
            var e=dokument(id,fassung);
            if(e.isAktuelleFassung())return ansicht(b,e);
            if(e.getAblauf().isBefore(Instant.now()))throw konflikt("Vorschau abgelaufen. Bitte erneut erzeugen.");

            var alt=aktuelle(id,e.getZiel());
            ersetzung(alt,ersetztId);
            if(alt!=null){
                alt.setAktuelleFassung(false);
                entwuerfe.saveAndFlush(alt);
            }
            e.setAktuelleFassung(true);
            entwuerfe.saveAndFlush(e);
            return ansicht(b,e);

        }
        );
    }

    public Fassung importieren(Long id,Buchungsstatus art,String ersetztId,byte[] bytes,String name){
        user();
        BriefpapierPdf.pruefeAnhang(bytes);
        return tx.execute(s->{

            var b=buchung(id);
            var alt=aktuelle(id,art);
            ersetzung(alt,ersetztId);
            var e=neu(b,art);
            String filename=Objects.toString(name,"Bearbeitet.pdf").replaceAll("[^\\p{L}\\p{N}_. -]","_");
            if(filename.length()>160)filename=filename.substring(0,160);
            if(!filename.toLowerCase(Locale.ROOT).endsWith(".pdf"))filename+=".pdf";

            e.setDateiname(filename);
            e.setPdf(bytes.clone());
            e.setExternAm(e.getDokumentAm());
            if(alt!=null){
                e.setBetreff(alt.getBetreff());
                e.setMailtext(alt.getMailtext());
            }
            e.setDatenHash(nachrichten.dokumentDatenstand(b,e));

            if(alt!=null){
                alt.setAktuelleFassung(false);
                entwuerfe.saveAndFlush(alt);
            }
            e.setAktuelleFassung(true);
            entwuerfe.saveAndFlush(e);
            return ansicht(b,e);

        }
        );
    }

    private String routingHash(HomepageStatusMailService.StatusVorschau route,Buchung b,BuchungsnachrichtEntwurf e){
        return VorlagenService.hash(json(Arrays.asList(route,b.getMieter().getEmail(),nachrichten.dokumentDatenstand(b,e))));
    }

    public Versandvorbereitung vorbereiten(Long id,String fassung){
        user();
        return tx.execute(s->{

            var b=buchung(id);
            var e=dokument(id,fassung);
            var route=mails.nachrichtVorschau(b,"","");

            var vorgaben=nachrichten.dokumentMailvorgaben(b,e.getZiel());
            String betreff=vorgaben.betreff(),text=vorgaben.mailtext();

            if(route.testmodus()&&!betreff.startsWith("[TEST] "))betreff="[TEST] "+betreff;

            String alt=Objects.toString(b.getFruehereBuchungsnummer(),"").strip();
            String mig="Wir haben unsere Buchungsverwaltung auf KanuControl umgestellt. "+(alt.isEmpty()?"Ihre Buchung wird künftig unter der Buchungsnummer "+b.getBuchungsnummer()+" geführt.":"Ihre bisherige Buchungsnummer "+alt+" wird künftig unter der Buchungsnummer "+b.getBuchungsnummer()+" geführt.")+" Bitte verwenden Sie diese bei künftigen Rückfragen und Zahlungen. Die aktualisierten Buchungsunterlagen finden Sie im Anhang.";

            return new Versandvorbereitung(e.getId(),hash(e.getPdf()),routingHash(route,b,e),b.getMieter().getEmail(),route,betreff,text,"Ihre Buchung "+b.getBuchungsnummer()+" – neue Buchungsnummer",nachrichten.migrationsText(b,e.getZiel(),mig),ansicht(b,e),b.getStatus());

        }
        );
    }

    public Versand senden(Long id,String fassung,Senden request){

        user();
        if(request==null)throw fehler("Versandauftrag fehlt.");
        try{
            UUID.fromString(request.versuch());
        }
        catch(Exception ex){
            throw fehler("Gültige Versandkennung erforderlich.");
        }
        BuchungsnachrichtService.pruefeMail(request.betreff(),request.mailtext());

        var result=tx.execute(s->{

            var b=buchung(id);
            var e=dokument(id,fassung);
            var old=archiv.findByVersandSchluessel(request.versuch()).orElse(null);
            String auftrag=json(Arrays.asList(TenantContext.getCurrentTenant(),id,fassung,user(),request));

            if(old!=null){
                if(!Objects.equals(archivAuftragHash(old),VorlagenService.hash(auftrag)))throw konflikt("Versandkennung wurde bereits für einen anderen Auftrag verwendet.");
                return new Ausgang(new Versand(old.getId(),old.getErgebnis(),old.getFehler()),null);
            }

            if(!Objects.equals(request.pruefsumme(),hash(e.getPdf())))throw konflikt("Die ausgewählte PDF wurde geändert. Bitte neu prüfen.");
            if(!e.isAktuelleFassung()&&e.getAblauf().isBefore(Instant.now()))throw konflikt("Vorschau abgelaufen.");

            var route=mails.nachrichtVorschau(b,"","");
            if(!Objects.equals(request.routingHash(),routingHash(route,b,e)))throw konflikt("Buchungsdaten, Vorlage, Empfänger oder Testmodus wurden geändert. Bitte E-Mail erneut vorbereiten.");

            var finalRoute=new HomepageStatusMailService.StatusVorschau(route.absender(),route.empfaenger(),route.cc(),route.replyTo(),request.betreff(),request.mailtext(),route.testmodus(),null);

            var meta=new LinkedHashMap<String,Object>();
            meta.put("auftragHash",VorlagenService.hash(auftrag));
            meta.put("benutzer",user());
            meta.put("tenant",TenantContext.getCurrentTenant());
            meta.put("dokumentId",e.getId());
            meta.put("dokumentart",e.getZiel());
            meta.put("dateiname",e.getDateiname());
            meta.put("pruefsumme",hash(e.getPdf()));
            meta.put("simulation",e.getZiel()==Buchungsstatus.ABGESCHLOSSEN);
            meta.put("herkunft",e.getExternAm()==null?"ERZEUGT":"IMPORTIERT");
            meta.put("mieter",b.getMieter().getEmail());
            meta.put("routing",finalRoute);
            meta.put("betreff",request.betreff());
            meta.put("mailtext",request.mailtext());
            meta.put("externAm",e.getExternAm());
            meta.put("pdfHash",hash(e.getPdf()));

            var mail=mails.nachrichtVorbereiten(finalRoute,e.getPdf().clone(),e.getDateiname(),json(meta));
            var a=new VorlagenVersand();
            a.setVersandSchluessel(request.versuch());
            a.setBuchungId(id);
            a.setMietobjektId(b.getMietobjekt().getId());
            a.setVorgang(BuchungsnachrichtDaten.vorgang(e.getZiel()).name());
            a.setPdf(e.getPdf().clone());
            a.setMetadaten(json(meta));
            archiv.saveAndFlush(a);
            e.setVersuchId(a.getId());
            entwuerfe.saveAndFlush(e);
            return new Ausgang(new Versand(a.getId(),a.getErgebnis(),null),mail);

        }
        );

        if(result.mail()==null)return result.result();

        try{
            mails.reservierungSenden(result.mail());
        }
        catch(RuntimeException ex){
            try{
                archivstatus.ergebnis(result.result().id(),false,Objects.toString(ex.getMessage(),"SMTP-Ergebnis unklar"));
            }
            catch(RuntimeException ignored){}return new Versand(result.result().id(),"FEHLER_ODER_UNKLAR","SMTP nicht bestätigt. Vor einem bewussten erneuten Versand Postausgang prüfen.");
        }

        try{
            archivstatus.ergebnis(result.result().id(),true,null);
        }
        catch(RuntimeException ex){
            return new Versand(result.result().id(),"VORBEREITET","SMTP bestätigt, Protokollaktualisierung fehlgeschlagen. Nicht automatisch erneut senden.");
        }

        return new Versand(result.result().id(),"SMTP_BESTAETIGT",null);

    }

}
