package com.kcserver.vermietung.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.core.repository.VereinRepository;
import com.kcserver.kjfp.repository.PersonRepository;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Service @Transactional @RequiredArgsConstructor
public class StammdatenMergeService {
 private final MieterRepository mieter;
 private final PersonRepository personen;
 private final VereinRepository vereine;
 private final BuchungRepository buchungen;
 private final MieterService mieterService;
 private final VeranstalterService veranstalterService;
 private final VermietungAenderungsprotokoll protokoll;
 private final ObjectMapper mapper;
 private final Validator validator;
 private static final List<String> MIETER=List.of("vorname","name","email","telefon","telefonFestnetz","strasse","plz","ort","countryCode","organisation","bankName","iban","bic","kontoinhaber","airbnb");
 private static final List<String> VERANSTALTER=List.of("name","abk","strasse","plz","ort","countryCode","bankName","iban","bic","kontoinhaberId");
 public record Auftrag(Long quelle,Long ziel,Map<String,Boolean> ausQuelle,String fingerprint) {}
 public record Vorschau(Object quelle,Object ziel,List<String> felder,List<Map<String,Object>> buchungen,boolean zielGeschuetzt,String fingerprint) {}
 private void pruefe(Long quelle,Long ziel){VorlagenService.tenant();if(quelle==null||ziel==null||quelle.equals(ziel))throw fehler("Bitte zwei verschiedene Datensätze auswählen.");}
 private ResponseStatusException fehler(String text){return new ResponseStatusException(HttpStatus.CONFLICT,text);}
 private List<Buchung> beziehungen(boolean person,Long id){return buchungen.findAll().stream().filter(b->person?Objects.equals(b.getMieter().getId(),id):b.getVeranstalter()!=null&&Objects.equals(b.getVeranstalter().getId(),id)).sorted(Comparator.comparing(Buchung::getId)).toList();}
 private String hash(Object value){try{return VorlagenService.hash(mapper.writeValueAsString(value));}catch(Exception e){throw new IllegalStateException(e);}}
 private List<Map<String,Object>> liste(List<Buchung> rows){return rows.stream().map(b->Map.<String,Object>of("id",b.getId(),"nummer",b.getBuchungsnummer(),"status",b.getStatus().name())).toList();}
 private Mieter miet(Long id){return mieter.findById(id).filter(m->m.getZusammengefuehrtIn()==null).orElseThrow(()->fehler("Mieter fehlt oder wurde bereits zusammengeführt."));}
 private com.kcserver.core.entity.Verein verein(Long id){return vereine.findById(id).filter(v->v.getVermietungZusammengefuehrtIn()==null).orElseThrow(()->fehler("Veranstalter fehlt oder wurde bereits zusammengeführt."));}
 public Vorschau vorschau(boolean person,Long quelle,Long ziel){
  pruefe(quelle,ziel);Object q,z;boolean geschuetzt;List<Buchung> rows;
  if(person){var a=miet(quelle);var b=miet(ziel);q=mieterService.get(quelle);z=mieterService.get(ziel);geschuetzt=b.getPerson().isKjfpSichtbar();rows=beziehungen(true,a.getPerson().getId());}
  else{var a=verein(quelle);var b=verein(ziel);q=veranstalterService.lesen(quelle);z=veranstalterService.lesen(ziel);geschuetzt=b.isKjfpVerein()||b.isHausverein();rows=beziehungen(false,a.getId());}
  var list=liste(rows);return new Vorschau(q,z,person?MIETER:VERANSTALTER,list,geschuetzt,hash(Arrays.asList(q,z,list,geschuetzt)));
 }
 @SuppressWarnings("unchecked") public Vorschau zusammenfuehren(boolean person,Auftrag a){
  pruefe(a.quelle(),a.ziel());
  // Same parent locks as booking creation; sorted acquisition prevents merge/merge deadlocks.
  var ids=new ArrayList<>(List.of(a.quelle(),a.ziel()));ids.sort(Long::compareTo);
  if(person){var people=ids.stream().map(id->miet(id).getPerson().getId()).sorted().toList();for(var id:people)personen.findLockedById(id).orElseThrow();for(var id:ids)mieter.findLockedById(id).orElseThrow();}
  else for(var id:ids)vereine.findLockedById(id).orElseThrow();
  var vorher=vorschau(person,a.quelle(),a.ziel());
  if(a.fingerprint()==null||!a.fingerprint().equals(vorher.fingerprint()))throw fehler("Stammdaten oder Buchungen wurden geändert. Bitte Vorschau neu laden.");
  var q=mapper.convertValue(vorher.quelle(),Map.class);var z=new LinkedHashMap<String,Object>(mapper.convertValue(vorher.ziel(),Map.class));
  var choices=a.ausQuelle()==null?Map.<String,Boolean>of():a.ausQuelle();
  if(!vorher.felder().containsAll(choices.keySet()))throw fehler("Unzulässige Feldauswahl.");
  for(var field:vorher.felder())if(Boolean.TRUE.equals(choices.get(field))){
   if(vorher.zielGeschuetzt()&&!Objects.equals(q.get(field),z.get(field)))throw fehler("Gemeinsam mit KJFP verwendete Zielstammdaten dürfen hier nicht überschrieben werden. Bitte den KJFP-Datensatz als unverändertes Ziel verwenden.");
   z.put(field,q.get(field));
  }
  List<Buchung> rows;
  if(person){var source=miet(a.quelle());var target=miet(a.ziel());
   if(!vorher.zielGeschuetzt()){var input=mapper.convertValue(z.entrySet().stream().filter(e->MIETER.contains(e.getKey())||e.getKey().equals("aktiv")).collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,e->e.getValue()==null?"":e.getValue())),MieterSaveDTO.class);if(!validator.validate(input).isEmpty())throw fehler("Bitte gültige Zielstammdaten auswählen.");mieterService.update(target.getId(),input);}
   rows=beziehungen(true,source.getPerson().getId());
   for(var row:rows){var b=buchungen.findLockedById(row.getId()).orElseThrow();b.setMieter(target.getPerson());}
   source.setAktiv(false);source.setZusammengefuehrtIn(target.getId());
  }else{var source=verein(a.quelle());var target=verein(a.ziel());
   if(!vorher.zielGeschuetzt()){var input=mapper.convertValue(z,VeranstalterDTO.class);if(!validator.validate(input).isEmpty())throw fehler("Bitte gültige Zielstammdaten auswählen.");veranstalterService.speichern(target.getId(),input);}
   rows=beziehungen(false,source.getId());
   for(var row:rows){var b=buchungen.findLockedById(row.getId()).orElseThrow();b.setVeranstalter(target);}
   source.setVermietungZusammengefuehrtIn(target.getId());
  }
  buchungen.flush();
  protokoll.schreiben(person?"MIETER_ZUSAMMENGEFUEHRT":"VERANSTALTER_ZUSAMMENGEFUEHRT",Map.of("quelleId",a.quelle(),"zielId",a.ziel(),"vorher",vorher,"ausQuelle",choices));
  return vorher;
 }
}
