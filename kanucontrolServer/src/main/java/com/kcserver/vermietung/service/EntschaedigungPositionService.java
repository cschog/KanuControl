package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.repository.EntschaedigungKatalogRepository;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.math.BigDecimal;
@Service @RequiredArgsConstructor
public class EntschaedigungPositionService {
 private final EntschaedigungKatalogRepository katalog;
 private final Validator validator;
 public void sync(Buchung b,List<EntschaedigungPositionDTO> inputs){
  if(inputs==null)return;
  if(inputs.size()>500)throw new IllegalArgumentException("Höchstens 500 Positionen erlaubt.");
  var old=new HashMap<Long,BuchungEntschaedigung>();b.getEntschaedigungen().forEach(e->old.put(e.getId(),e));
  var seen=new HashSet<Long>();var wanted=new ArrayList<BuchungEntschaedigung>();
  for(var d:inputs){
   if(d==null||!validator.validate(d).isEmpty())throw new IllegalArgumentException("Bitte Bezeichnung, Anlass, ganze positive Anzahl, positiven Preis mit höchstens zwei Nachkommastellen und Textlängen prüfen.");
   var e=d.id()==null?new BuchungEntschaedigung():old.get(d.id());
   if(e==null||d.id()!=null&&!seen.add(d.id()))throw new IllegalArgumentException("Fremde oder doppelte Positions-ID.");
   if(d.id()!=null&&!Objects.equals(d.katalogId(),e.getKatalog()==null?null:e.getKatalog().getId()))throw new IllegalArgumentException("Katalogbezug ist unveränderlich. Position entfernen und neu hinzufügen.");
   if(d.id()==null&&d.katalogId()!=null){
    var k=katalog.findById(d.katalogId()).orElseThrow(()->new IllegalArgumentException("Katalogeintrag nicht gefunden."));
    if(!k.isAktiv()||!Objects.equals(k.getMietobjekt().getId(),b.getMietobjekt().getId()))throw new IllegalArgumentException("Nur aktive Katalogeinträge dieses Mietobjekts sind auswählbar.");
    if(d.katalogpreis()==null||d.katalogpreis().compareTo(k.getEinzelpreis())!=0)throw new IllegalArgumentException("Katalogpreis wurde geändert. Bitte Position erneut auswählen.");
    e.setKatalog(k);e.setKatalogpreis(k.getEinzelpreis());e.setBezeichnung(k.getBezeichnung());
   }else e.setBezeichnung(d.bezeichnung().trim());
   if(e.getKatalogpreis()!=null&&e.getKatalogpreis().compareTo(d.einzelpreis())!=0&&(d.preisbegruendung()==null||d.preisbegruendung().isBlank()))throw new IllegalArgumentException("Abweichender Katalogpreis benötigt eine interne Begründung.");
   e.setBuchung(b);e.setAnlass(d.anlass());e.setAnzahl(d.anzahl().intValueExact());e.setEinzelpreis(d.einzelpreis());e.setBemerkung(d.bemerkung());e.setPreisbegruendung(d.preisbegruendung());e.setSortierung(d.sortierung());wanted.add(e);
  }
  b.getEntschaedigungen().removeIf(e->!wanted.contains(e));wanted.stream().filter(e->!b.getEntschaedigungen().contains(e)).forEach(b.getEntschaedigungen()::add);
 }
 public static List<EntschaedigungPositionDTO> rows(Buchung b){if(Zahlungsplan.ohneAbrechnung(b))return List.of();return b.getEntschaedigungen().stream().sorted(Comparator.comparingInt(BuchungEntschaedigung::getSortierung)).map(e->new EntschaedigungPositionDTO(e.getId(),e.getKatalog()==null?null:e.getKatalog().getId(),e.getBezeichnung(),e.getAnlass(),BigDecimal.valueOf(e.getAnzahl()),e.getEinzelpreis(),e.getKatalogpreis(),e.getGesamtbetrag(),e.getBemerkung(),e.getPreisbegruendung(),e.getSortierung())).toList();}
 public static BigDecimal sum(Buchung b){if(Zahlungsplan.ohneAbrechnung(b))return BigDecimal.ZERO.setScale(2);return b.getEntschaedigungen().stream().map(BuchungEntschaedigung::getGesamtbetrag).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2);}
}
