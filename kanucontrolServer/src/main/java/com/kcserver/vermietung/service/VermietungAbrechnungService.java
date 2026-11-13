package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.Instant;
@Service @Transactional @RequiredArgsConstructor
public class VermietungAbrechnungService {
 private final BuchungRepository buchungen;
 private final VermietungAbrechnungRepository dokumente;
 private final AbrechnungVorlagenService vorlagen;
 public record Dokument(Long id,int revision,Instant erstellt,String korrekturgrund){}
 public record Ausstellung(Long ersetztDokumentId,String korrekturgrund){}
 private Dokument dto(VermietungAbrechnung e){return new Dokument(e.getId(),e.getRevision(),e.getErstellt(),e.getKorrekturgrund());}
 @Transactional(readOnly=true) public List<Dokument> list(Long id){return dokumente.findByBuchungIdOrderByRevisionDesc(id).stream().map(this::dto).toList();}
 public Dokument create(Long id,Ausstellung request){
  if(request==null)throw new IllegalArgumentException("Dokumentangaben fehlen.");
  var b=buchungen.findLockedById(id).orElseThrow(()->new IllegalArgumentException("Buchung nicht gefunden."));
  if(b.getStatus()!=Buchungsstatus.ABGESCHLOSSEN||b.getAbrechnungRevision()!=b.getAbschlussRevision())throw new IllegalArgumentException("Aktuellen Buchungsstand zuerst ausdrücklich (erneut) abschließen.");
  if(Zahlungsplan.ohneAbrechnung(b))throw new IllegalArgumentException("Für Ohne Abrechnung wird keine Rechnung ausgestellt.");
  var old=dokumente.findByBuchungIdOrderByRevisionDesc(id);String reason=Objects.toString(request.korrekturgrund(),"").trim();
  if(reason.length()>1000)throw new IllegalArgumentException("Korrekturgrund zu lang.");
  if(old.isEmpty()?request.ersetztDokumentId()!=null:!Objects.equals(old.getFirst().getId(),request.ersetztDokumentId())||reason.isBlank())throw new IllegalArgumentException("Korrektur benötigt das letzte Dokument und eine Begründung. Bitte neu laden.");
  var e=new VermietungAbrechnung();e.setBuchung(b);e.setRevision(old.isEmpty()?1:old.getFirst().getRevision()+1);e.setErstellt(Instant.now());e.setKorrekturgrund(reason);
  e.setPdf(vorlagen.erstellen(b,e.getRevision(),null));return dto(dokumente.saveAndFlush(e));
 }
 @Transactional(readOnly=true) public byte[] pdf(Long id,Long dokumentId){return dokumente.findByIdAndBuchungId(dokumentId,id).orElseThrow(()->new IllegalArgumentException("Dokument nicht gefunden.")).getPdf();}
}
