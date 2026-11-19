package com.kcserver.vermietung.service;
import com.kcserver.vermietung.repository.BuchungRepository;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
@Service @RequiredArgsConstructor
public class AnfrageMigrationService {
 private final BuchungRepository buchungen;
 private final BuchungService service;
 private final PlatformTransactionManager transactionManager;
 private final VermietungAenderungsprotokoll protokoll;
 public record Altbuchung(Long id,String nummer,String von,String bis) {}
 public List<Altbuchung> offen(){return new TransactionTemplate(transactionManager).execute(t->buchungen.findAll().stream().filter(b->b.getStatus()==Buchungsstatus.ANFRAGE).map(b->new Altbuchung(b.getId(),b.getBuchungsnummer(),b.getAnreise().toString(),b.getAbreise().toString())).toList());}
 public void migrieren(Long id,boolean ablehnen){new TransactionTemplate(transactionManager).executeWithoutResult(t->{
  var b=buchungen.findLockedById(id).orElseThrow();if(b.getStatus()!=Buchungsstatus.ANFRAGE)throw new IllegalArgumentException("Die Altanfrage wurde bereits bearbeitet.");
  var ziel=ablehnen?Buchungsstatus.ABGELEHNT:Buchungsstatus.RESERVIERT;
  service.anwendenGeprueft(id,ziel,List.of());
  protokoll.schreiben("ANFRAGE_MIGRIERT",Map.of("buchungId",id,"vorher","ANFRAGE","nachher",ziel.name()));
 });}
}
