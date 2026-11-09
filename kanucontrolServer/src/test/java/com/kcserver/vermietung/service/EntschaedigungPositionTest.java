package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.repository.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class EntschaedigungPositionTest {
 EntschaedigungPositionDTO p(Long id,Long katalog,String name,String count,String price,String original,String reason){return new EntschaedigungPositionDTO(id,katalog,name,BuchungEntschaedigung.Anlass.VERLUST,new BigDecimal(count),new BigDecimal(price),original==null?null:new BigDecimal(original),new BigDecimal("9999"),"INTERN",reason,0);}
 @Test void sechsunddreissigEuroUndHistorischePreiseBleibenStabil(){
  try(var validation=Validation.buildDefaultValidatorFactory()){
   var repo=mock(EntschaedigungKatalogRepository.class);var service=new EntschaedigungPositionService(repo,validation.getValidator());
   var b=new Buchung();var objekt=new Mietobjekt();objekt.setId(7L);b.setMietobjekt(objekt);
   var k=new EntschaedigungKatalog();k.setId(10L);k.setMietobjekt(objekt);k.setBezeichnung("Glas");k.setEinzelpreis(new BigDecimal("3"));when(repo.findById(10L)).thenReturn(Optional.of(k));
   service.sync(b,List.of(p(null,10L,"Glas","2","3","3",null),p(null,null,"Thermoskanne","1","30",null,null)));
   assertEquals(new BigDecimal("36.00"),EntschaedigungPositionService.sum(b));var first=b.getEntschaedigungen().getFirst();first.setId(100L);
   k.setAktiv(false);k.setEinzelpreis(new BigDecimal("99"));k.setBezeichnung("Neuer Name");
   service.sync(b,List.of(p(100L,10L,"Glas","2","3","99",null)));
   assertEquals(new BigDecimal("3"),first.getKatalogpreis());assertEquals("Glas",first.getBezeichnung());
   assertThrows(IllegalArgumentException.class,()->service.sync(b,List.of(p(100L,10L,"Glas","2","4","99",null))));
   assertThrows(IllegalArgumentException.class,()->service.sync(b,List.of(p(999L,null,"Fremd","1","1",null,null))));
   assertThrows(IllegalArgumentException.class,()->service.sync(b,List.of(p(null,null,"Bruchmenge","1.5","3",null,null))));
   assertThrows(IllegalArgumentException.class,()->service.sync(b,List.of(p(null,null,"Nullpreis","1","0",null,null))));
   service.sync(b,List.of());assertEquals(new BigDecimal("0.00"),EntschaedigungPositionService.sum(b));
  }
 }
}
