package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.core.tenancy.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;

@Service @RequiredArgsConstructor @Transactional
public class VorlagenPaketService {
    private final MandantenBriefpapierRepository papier;
    private final MietobjektVorlageRepository vorlagen;
    private final MietobjektRepository objekte;
    private final VorlagenImportRepository importe;
    private final VorlagenService service;
    private final EntityManager em;
    public enum Aktion { NEU, ERSETZEN, UEBERSPRINGEN }
    public record Zuordnung(String quelle,Long ziel,boolean ueberspringen) {}
    public record Auswahl(String quelle,Vorgang vorgang,Aktion aktion) {}
    public record Plan(boolean briefpapier,List<Zuordnung> zuordnungen,List<Auswahl> auswahl,List<String> ohneLayoutFreigabe) {}
    public record Vergleich(String schluessel,Long ziel,Vorgang vorgang,Aktion aktion,Vorlage vorher,Vorlage nachher) {}
    public record Ansicht(String id,String ablauf,String paketHash,VorlagenPaket.Manifest manifest,Plan plan,List<Vergleich> vergleich,
            String grussVorher,String grussNachher,boolean bildWirdEntfernt,int neu,int ersetzt,int uebersprungen,int objekteUebersprungen,boolean geprueft,boolean uebernommen) {}
    public record Probe(String schluessel,String szenario,byte[] pdf,String betreff,String text) {}
    public record Pruefstatus(String schluessel,boolean erfolgreich,String ursache,boolean ausdruecklichFreigegeben) {}
    public record Vorschau(List<Probe> vorlagen,byte[] briefpapier,List<Pruefstatus> status,String hinweis) {}
    private static String benutzer(){VorlagenService.tenant();var a=SecurityContextHolder.getContext().getAuthentication();if(a==null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);return a.getName();}
    private static String hash(byte[] b){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(b));}catch(Exception e){throw new IllegalStateException(e);}}
    private static String json(Object v){return new String(VorlagenMarkdown.json(v),StandardCharsets.UTF_8);}
    private Vorlage lesen(MietobjektVorlage v){var x=VorlagenMarkdown.json(v.getInhalt().getBytes(StandardCharsets.UTF_8),Vorlage.class,"Gespeicherte Vorlage "+v.getMietobjektId()+"/"+v.getVorgang());pruefe(x);return normalisieren(new Vorlage(v.getVersion(),x.betreff(),x.mailtext(),x.bloecke(),x.anrede()));}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public byte[] exportieren(){
        benutzer();VorlagenMarkdown.vertrag();var files=new LinkedHashMap<String,byte[]>();var os=new ArrayList<VorlagenPaket.Objekt>();var vs=new ArrayList<VorlagenPaket.Eintrag>();
        var objects=new TreeMap<Long,String>();objekte.findAll().forEach(o->objects.put(o.getId(),o.getBezeichnung()));
        var rows=new ArrayList<>(vorlagen.findAll());rows.sort(Comparator.comparing(MietobjektVorlage::getMietobjektId).thenComparing(v->v.getVorgang().name()));var keys=new HashMap<Long,String>();
        for(var row:rows){String key=keys.get(row.getMietobjektId());if(key==null){if(!objects.containsKey(row.getMietobjektId()))throw fehler("Gespeicherte Vorlage ohne Mietobjekt.");key=String.format(Locale.ROOT,"objekt-%03d",keys.size()+1);keys.put(row.getMietobjektId(),key);os.add(new VorlagenPaket.Objekt(key,objects.get(row.getMietobjektId())));}
            String path="vorlagen/"+key+"/"+row.getVorgang()+".md";files.put(path,VorlagenMarkdown.schreiben(lesen(row)));vs.add(new VorlagenPaket.Eintrag(key,row.getVorgang(),path));}
        var b=papier.findById(1L).orElse(null);VorlagenPaket.Papier bp=null;
        if(b!=null){String bild=b.getBild()==null?null:b.getBild().length>2 && b.getBild()[0]==(byte)0x89?"mandant/unterschrift.png":"mandant/unterschrift.jpg";
            bp=new VorlagenPaket.Papier("mandant/briefpapier.pdf","mandant/grussformel.md",bild);files.put(bp.pdf(),b.getPdf());files.put(bp.grussformel(),normalisieren(b.getGruss()).getBytes(StandardCharsets.UTF_8));if(bild!=null)files.put(bild,b.getBild());}
        String name=TenantContext.getCurrentTenant();
        var vereine=em.createQuery("select v.name from Verein v where v.hausverein = true",String.class).setMaxResults(1).getResultList();if(!vereine.isEmpty())name=vereine.getFirst();
        return VorlagenPaket.schreiben(new VorlagenPaket.Manifest("kanucontrol-vorlagen",1,Instant.now().toString(),new VorlagenPaket.Quelle(name),bp,os,vs),files);
    }
    public Ansicht hochladen(byte[] bytes){String user=benutzer();VorlagenPaket.lesen(bytes);importe.abgelaufeneEntfernen(Instant.now());if(importe.countByBenutzerAndTenantAndUebernommenFalse(user,TenantContext.getCurrentTenant())>=3)throw fehler("Höchstens drei offene Importentwürfe. Bitte vorhandene Entwürfe abbrechen oder Ablauf abwarten.");
        var d=new VorlagenImport();d.setId(UUID.randomUUID().toString());d.setBenutzer(user);d.setTenant(TenantContext.getCurrentTenant());d.setAblauf(Instant.now().plusSeconds(1800));d.setPaket(bytes);d.setPaketHash(hash(bytes));importe.saveAndFlush(d);return ansicht(d);
    }
    private VorlagenImport entwurf(String id){String user=benutzer();var d=importe.gesperrt(id,user,TenantContext.getCurrentTenant()).orElseThrow(()->new ResponseStatusException(HttpStatus.GONE,"Importentwurf nicht verfügbar, abgelaufen oder nicht diesem Benutzer/Mandanten zugeordnet. Bitte erneut hochladen."));if(d.getAblauf().isBefore(Instant.now()))throw new ResponseStatusException(HttpStatus.GONE,"Importentwurf ist abgelaufen. Bitte erneut hochladen.");return d;}
    public Ansicht lesen(String id){return ansicht(entwurf(id));}
    public void abbrechen(String id){var d=entwurf(id);if(!d.isUebernommen())importe.delete(d);}
    private VorlagenPaket.Inhalt inhalt(VorlagenImport d){return VorlagenPaket.lesen(d.getPaket());}
    private Plan plan(VorlagenImport d){return d.getPlan()==null?null:VorlagenMarkdown.json(d.getPlan().getBytes(StandardCharsets.UTF_8),Plan.class,"Importplan");}
    private List<Vergleich> vergleichen(VorlagenPaket.Inhalt p,Plan plan) {
        if(plan==null)return List.of();if(plan.zuordnungen()==null || plan.auswahl()==null)throw fehler("Zuordnung und Auswahl fehlen.");
        if(plan.zuordnungen().size()>500 || plan.auswahl().size()>500 || plan.ohneLayoutFreigabe()!=null && plan.ohneLayoutFreigabe().size()>500)throw fehler("Importplan enthält zu viele Einträge.");
        if(plan.briefpapier() && p.pdf()==null)throw fehler("Paket enthält kein Briefpapier. Fehlend bedeutet beibehalten.");
        var mapping=new HashMap<String,Zuordnung>();var sources=new HashSet<String>();p.manifest().mietobjekte().forEach(o->sources.add(o.schluessel()));
        for(var m:plan.zuordnungen()){
            if(m==null || !sources.contains(m.quelle()) || mapping.put(m.quelle(),m)!=null)throw fehler("Unbekannte oder doppelte Quellobjektzuordnung.");
            if(m.ueberspringen()){if(m.ziel()!=null)throw fehler("Übersprungenes Objekt darf kein Ziel haben.");}
            else if(m.ziel()==null || !objekte.existsById(m.ziel()))throw fehler("Zielobjekt gehört nicht zum aktuellen Mandanten oder fehlt.");
        }
        if(!mapping.keySet().equals(sources))throw fehler("Jedes Quellobjekt ausdrücklich zuordnen oder überspringen.");
        var selections=new HashMap<String,Auswahl>();for(var a:plan.auswahl())if(a==null || a.aktion()==null || a.vorgang()==null || !p.vorlagen().containsKey(a.quelle()+"/"+a.vorgang()) || selections.put(a.quelle()+"/"+a.vorgang(),a)!=null)throw fehler("Unbekannte oder doppelte Vorlagenauswahl.");
        if(!selections.keySet().equals(p.vorlagen().keySet()))throw fehler("Jede Vorlage ausdrücklich auswählen oder überspringen.");
        var targets=new HashSet<String>();var result=new ArrayList<Vergleich>();
        for(var entry:p.manifest().vorlagen()) {
            String key=entry.mietobjekt()+"/"+entry.vorgang();var m=mapping.get(entry.mietobjekt());var a=selections.get(key);
            if(m.ueberspringen() && a.aktion()!=Aktion.UEBERSPRINGEN)throw fehler(key+": Übersprungenes Objekt enthält ausgewählte Vorlage.");
            var old=m.ziel()==null?null:vorlagen.findByMietobjektIdAndVorgang(m.ziel(),entry.vorgang()).map(this::lesen).orElse(null);
            if(a.aktion()!=Aktion.UEBERSPRINGEN){if(!targets.add(m.ziel()+"/"+entry.vorgang()))throw fehler(key+": Doppelter Zielschlüssel im Importplan.");
                if(a.aktion()==Aktion.NEU && old!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,key+": Für Zielobjekt "+m.ziel()+" existiert bereits eine Vorlage. Bitte „Vorhandene Vorlage ausdrücklich ersetzen“ oder „Überspringen“ auswählen.");
                if(a.aktion()==Aktion.ERSETZEN && old==null)throw new ResponseStatusException(HttpStatus.CONFLICT,key+": Für Zielobjekt "+m.ziel()+" ist noch keine Vorlage gespeichert. Bitte „Neu anlegen“ auswählen.");}
            result.add(new Vergleich(key,m.ziel(),entry.vorgang(),a.aktion(),old,p.vorlagen().get(key)));
        }return result;
    }
    /** Includes missing/new rows, all remaining templates, bytes and object names. */
    private String zielHash(){
        var values=new ArrayList<Object>();var b=papier.findById(1L).orElse(null);
        if(b!=null)values.add(Arrays.asList(b.getVersion(),b.getGruss(),hash(b.getPdf()),b.getBild()==null?null:hash(b.getBild())));
        objekte.findAll().stream().sorted(Comparator.comparing(Mietobjekt::getId)).forEach(o->values.add(List.of(o.getId(),o.getBezeichnung())));
        vorlagen.findAll().stream().sorted(Comparator.comparing(MietobjektVorlage::getId)).forEach(v->values.add(Arrays.asList(v.getId(),v.getVersion(),v.getMietobjektId(),v.getVorgang(),v.getInhalt())));
        return hash(VorlagenMarkdown.json(values));
    }
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Ansicht planen(String id,Plan p){var d=entwurf(id);offen(d);vergleichen(inhalt(d),p);d.setPlan(json(p));d.setZielHash(zielHash());d.setGeprueft(false);return ansicht(d);}
    private void offen(VorlagenImport d){if(d.isUebernommen())throw new ResponseStatusException(HttpStatus.CONFLICT,"Import wurde bereits übernommen.");}
    private void aktuell(VorlagenImport d){if(d.getPlan()==null || !Objects.equals(d.getZielHash(),zielHash())){d.setGeprueft(false);throw new ResponseStatusException(HttpStatus.CONFLICT,"Zielkonfiguration wurde geändert oder Plan fehlt. Bitte Zuordnung und Vorschauen erneut prüfen.");}}
    private Ansicht ansicht(VorlagenImport d){
        if(d.isUebernommen())return new Ansicht(d.getId(),d.getAblauf().toString(),d.getPaketHash(),null,null,List.of(),null,null,false,0,0,0,0,false,true);
        var p=inhalt(d);var plan=plan(d);var cmp=vergleichen(p,plan);var b=papier.findById(1L).orElse(null);
        return new Ansicht(d.getId(),d.getAblauf().toString(),d.getPaketHash(),p.manifest(),plan,cmp,b==null?null:b.getGruss(),plan!=null && plan.briefpapier()?p.gruss():b==null?null:b.getGruss(),plan!=null && plan.briefpapier() && p.bild()==null && b!=null && b.getBild()!=null,
            (int)cmp.stream().filter(x->x.aktion()==Aktion.NEU).count(),(int)cmp.stream().filter(x->x.aktion()==Aktion.ERSETZEN).count(),(int)cmp.stream().filter(x->x.aktion()==Aktion.UEBERSPRINGEN).count(),plan==null?0:(int)plan.zuordnungen().stream().filter(Zuordnung::ueberspringen).count(),d.isGeprueft(),false);
    }
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Vorschau vorschau(String id){var d=entwurf(id);offen(d);aktuell(d);var p=inhalt(d);var plan=plan(d);var cmp=vergleichen(p,plan);var b=papier.findById(1L).orElse(null);
        var effective=new LinkedHashMap<String,Vergleich>();
        if(plan.briefpapier())for(var v:vorlagen.findAll()){String key=v.getMietobjektId()+"/"+v.getVorgang();effective.put(key,new Vergleich("Bestand "+key,v.getMietobjektId(),v.getVorgang(),Aktion.UEBERSPRINGEN,lesen(v),lesen(v)));}
        for(var c:cmp)if(c.aktion()!=Aktion.UEBERSPRINGEN)effective.put(c.ziel()+"/"+c.vorgang(),c);
        if(effective.isEmpty() && !plan.briefpapier())throw fehler("Keine Änderungen ausgewählt.");
        var probes=new ArrayList<Probe>();var status=new ArrayList<Pruefstatus>();byte[] papierProbe=null;
        var freigaben=plan.ohneLayoutFreigabe()==null?Set.<String>of():new HashSet<>(plan.ohneLayoutFreigabe());
        if(!effective.values().stream().map(Vergleich::schluessel).toList().containsAll(freigaben))throw fehler("Unbekannte Freigabe ohne Layoutprüfung.");
        if(plan.briefpapier())papierProbe=service.probe(b==null?null:b.getVersion(),p.gruss(),p.pdf(),p.bild(),p.bild()==null).pdf();
        for(var c:effective.values()){String ursache=null;int start=probes.size();var sichtbar=new HashSet<String>();
          boolean ogRelevant=c.vorgang()==Vorgang.ABGERECHNET || bedingungVerwendet(c.nachher(),Set.of(Bedingung.OG_GEBUCHT,Bedingung.OG_NICHT_GEBUCHT));
          boolean fristRelevant=c.vorgang()!=Vorgang.STORNIERUNG && bedingungVerwendet(c.nachher(),Set.of(Bedingung.STORNO_UNTER_56_TAGEN,Bedingung.STORNO_AB_56_TAGEN));
          for(String szenario:VorlagenService.szenarien(c.vorgang()))for(boolean og:ogRelevant?new boolean[]{false,true}:new boolean[]{false})for(boolean kurz:fristRelevant?new boolean[]{false,true}:new boolean[]{szenario.equals("GESAMTMIETE_VORAUS")||szenario.equals("STORNO_KURZ")}){
            try {VorlagenService.Ergebnis r=service.beispielVorschauSzenario(c.ziel(),c.vorgang(),c.nachher(),og,kurz,szenario,plan.briefpapier(),b==null?null:b.getVersion(),p.gruss(),p.pdf(),p.bild(),p.bild()==null);
                String text;try(var doc=org.apache.pdfbox.Loader.loadPDF(r.pdf())){text=new org.apache.pdfbox.text.PDFTextStripper().getText(doc)+"|Seiten="+doc.getNumberOfPages();}catch(java.io.IOException ex){throw fehler("PDF-Vorschau ist nicht lesbar.");}
                if(sichtbar.add(VorlagenService.hash(text+"|"+r.betreff()+"|"+r.mailtext())))probes.add(new Probe(c.schluessel(),szenario+", OG "+(og?"mitgebucht":"nicht gebucht")+", "+(kurz?"unter":"ab")+" 8 Wochen",r.pdf(),r.betreff(),r.mailtext()));
            }catch(ResponseStatusException e){ursache=e.getReason();}
          }
          if(ursache!=null)while(probes.size()>start)probes.removeLast();
          status.add(new Pruefstatus(c.schluessel(),ursache==null,ursache,ursache!=null && freigaben.contains(c.schluessel())));
        }
        d.setGeprueft(status.stream().allMatch(x->x.erfolgreich() || x.ausdruecklichFreigegeben()));return new Vorschau(probes,papierProbe,status,"Fachliche Zahlungs-, Storno- und Abrechnungsszenarien; identische sichtbare Ergebnisse je Vorlage zusammengefasst. Layoutkontrolle mit synthetischen Daten; Laufzeitvalidierung bleibt erforderlich. Briefpapierwechsel betrifft alle Mietobjekte des Mandanten, einschließlich beibehaltener Vorlagen.");
    }
    private static boolean bedingungVerwendet(Vorlage v,Set<Bedingung> gesucht){return v.bloecke().stream().anyMatch(b->gesucht.contains(b.bedingung()) || b.varianten()!=null && b.varianten().stream().anyMatch(x->gesucht.contains(x.bedingung())));}
    public Ansicht uebernehmen(String id){var d=entwurf(id);if(d.isUebernommen())return ansicht(d);if(!d.isGeprueft())throw new ResponseStatusException(HttpStatus.CONFLICT,"Bitte den unveränderten Plan zuerst in der Vorschau prüfen.");
        // Also covers inserts into previously absent target keys, including concurrent normal editor writes.
        em.createNativeQuery("SET LOCAL lock_timeout = '5s'").executeUpdate();
        em.createNativeQuery("LOCK TABLE mietobjekt, vermietung_briefpapier, vermietung_vorlage IN SHARE ROW EXCLUSIVE MODE").executeUpdate();
        em.clear();d=entwurf(id);aktuell(d);var p=inhalt(d);var plan=plan(d);var cmp=vergleichen(p,plan);
        if(plan.briefpapier()){var b=papier.findById(1L).orElseGet(MandantenBriefpapier::new);b.setPdf(p.pdf());b.setBild(p.bild());b.setGruss(p.gruss());papier.saveAndFlush(b);}
        else papier.findById(1L).ifPresent(b->em.lock(b,jakarta.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT));
        // Even a partial import invalidates prior dispatch proofs for the whole tenant.
        for(var c:cmp)if(c.aktion()!=Aktion.UEBERSPRINGEN){var v=c.nachher();service.speichern(c.ziel(),c.vorgang(),new Vorlage(c.vorher()==null?null:c.vorher().version(),v.betreff(),v.mailtext(),v.bloecke(),v.anrede()));}
        d.setUebernommen(true);d.setGeprueft(false);d.setPaket(null);d.setPlan(null);return ansicht(d);
    }
}
