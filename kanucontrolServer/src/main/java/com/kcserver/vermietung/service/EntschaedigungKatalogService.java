package com.kcserver.vermietung.service;
import com.kcserver.vermietung.dto.EntschaedigungKatalogDTO;
import com.kcserver.vermietung.entity.EntschaedigungKatalog;
import com.kcserver.vermietung.repository.*;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Service @Transactional @RequiredArgsConstructor
public class EntschaedigungKatalogService {
 private final EntschaedigungKatalogRepository katalog;
 private final MietobjektRepository objekte;
 private final Validator validator;
 @Transactional(readOnly=true) public List<EntschaedigungKatalogDTO> list(Long objektId){
  return katalog.findByMietobjektIdOrderBySortierungAscIdAsc(objektId).stream().map(this::dto).toList();
 }
 public EntschaedigungKatalogDTO save(Long objektId,Long id,EntschaedigungKatalogDTO input){
  if(input==null||!validator.validate(input).isEmpty())throw new IllegalArgumentException("Bitte Bezeichnung, positiven Preis mit höchstens zwei Nachkommastellen und Sortierung prüfen.");
  var objekt=objekte.findById(objektId).orElseThrow(()->new IllegalArgumentException("Mietobjekt nicht gefunden."));
  var e=id==null?new EntschaedigungKatalog():katalog.findById(id).orElseThrow(()->new IllegalArgumentException("Katalogeintrag nicht gefunden."));
  if(id!=null&&!Objects.equals(e.getMietobjekt().getId(),objektId))throw new IllegalArgumentException("Katalogeintrag gehört zu einem anderen Mietobjekt.");
  if(!Objects.equals(e.getVersion(),input.version()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Katalog wurde geändert. Bitte neu laden.");
  e.setMietobjekt(objekt);e.setBezeichnung(input.bezeichnung().trim());e.setEinzelpreis(input.einzelpreis());e.setSortierung(input.sortierung());e.setAktiv(input.aktiv());
  return dto(katalog.saveAndFlush(e));
 }
 private EntschaedigungKatalogDTO dto(EntschaedigungKatalog e){return new EntschaedigungKatalogDTO(e.getId(),e.getVersion(),e.getBezeichnung(),e.getEinzelpreis(),e.getSortierung(),e.isAktiv());}
}
