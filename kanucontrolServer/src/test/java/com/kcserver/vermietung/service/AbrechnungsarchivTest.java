package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AbrechnungsarchivTest {
 @Test void korrekturErsetztPdfUndNurLetzteFassungIstVerfuegbar(){
  var br=mock(BuchungRepository.class);var dr=mock(VermietungAbrechnungRepository.class);var renderer=mock(AbrechnungVorlagenService.class);
  var b=new Buchung();b.setId(7L);b.setStatus(Buchungsstatus.ABGESCHLOSSEN);b.setAbrechnungRevision(3);b.setAbschlussRevision(3);when(br.findLockedById(7L)).thenReturn(Optional.of(b));
  var old=new VermietungAbrechnung();old.setId(11L);old.setRevision(1);old.setPdf(new byte[]{1});when(dr.findByBuchungIdOrderByRevisionDesc(7L)).thenReturn(List.of(old));
  byte[] bytes={4,5,6};when(renderer.erstellen(b,2,null)).thenReturn(bytes);
  when(dr.saveAndFlush(any())).thenAnswer(i->{VermietungAbrechnung e=i.getArgument(0);e.setId(12L);assertEquals(2,e.getRevision());assertEquals("",e.getKorrekturgrund());assertArrayEquals(bytes,e.getPdf());when(dr.findByBuchungIdOrderByRevisionDesc(7L)).thenReturn(List.of(e,old));return e;});
  doAnswer(i->{old.setPdf(new byte[0]);return null;}).when(dr).entferneFrueherePdfs(eq(7L),eq(12L),any(byte[].class));
  var service=new VermietungAbrechnungService(br,dr,renderer);
  assertThrows(IllegalArgumentException.class,()->service.create(7L,new VermietungAbrechnungService.Ausstellung(null,"")));
  var doc=service.create(7L,new VermietungAbrechnungService.Ausstellung(11L,""));assertEquals(2,doc.revision());
  clearInvocations(renderer);assertArrayEquals(bytes,service.pdf(7L,12L));verifyNoInteractions(renderer);assertEquals(0,old.getPdf().length);assertThrows(IllegalArgumentException.class,()->service.pdf(7L,11L));assertEquals(1,service.list(7L).size());
 }
}
