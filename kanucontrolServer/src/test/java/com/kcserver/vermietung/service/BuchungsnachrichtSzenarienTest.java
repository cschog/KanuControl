package com.kcserver.vermietung.service;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.kcserver.vermietung.service.VorlagenModell.*;
class BuchungsnachrichtSzenarienTest {
 @AfterEach void cleanup(){TenantContext.clear();}
 @Test void importpaketUndAlleFachlichenSzenarienErzeugenPassendeDokumente()throws Exception{
  TenantContext.setCurrentTenant("ekc_test");var paket=VorlagenPaket.lesen(getClass().getResourceAsStream("/vorlagenpakete/bootshaus-status-beispiel.zip").readAllBytes());assertEquals(6,paket.vorlagen().size());assertNull(paket.pdf());
  var pr=mock(MandantenBriefpapierRepository.class);var vr=mock(MietobjektVorlageRepository.class);var or=mock(MietobjektRepository.class);
  var p=new MandantenBriefpapier();p.setPdf(getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes());p.setGruss("Mit freundlichen Grüßen\nHildegard Schog");p.setVersion(1L);when(pr.findById(1L)).thenReturn(Optional.of(p));var o=new Mietobjekt();o.setId(1L);o.setBezeichnung("Bootshaus");when(or.findById(1L)).thenReturn(Optional.of(o));
  var service=new VorlagenService(pr,vr,or,new ObjectMapper());
  for(var entry:paket.manifest().vorlagen())for(String s:VorlagenService.szenarien(entry.vorgang()))for(boolean og:new boolean[]{false,true}){
   var v=paket.vorlagen().get(entry.mietobjekt()+"/"+entry.vorgang());var result=service.beispielVorschauSzenario(1L,entry.vorgang(),v,og,s.equals("GESAMTMIETE_VORAUS")||s.equals("STORNO_KURZ"),s,false,null,null,null,null,false);
   try(var doc=Loader.loadPDF(result.pdf())){var text=new PDFTextStripper().getText(doc);
    if(s.equals("RESTZAHLUNG")){assertTrue(text.contains("Bitte überweist die noch offene Restzahlung"));assertFalse(text.contains("Die Rückerstattung"));}
    if(s.equals("ERSTATTUNG")){assertTrue(text.contains("Die Rückerstattung"));assertFalse(text.contains("Bitte überweist die noch offene Restzahlung"));}
    if(s.equals("AUSGEGLICHEN")){assertTrue(text.contains("Die Abrechnung ist ausgeglichen"));assertFalse(text.contains("Die Rückerstattung"));assertFalse(text.contains("Bitte überweist die noch offene Restzahlung"));}
    if(s.equals("STORNO_FRUEH")){assertTrue(text.contains("werden alle geleisteten Zahlungen"));assertTrue(text.contains("Kontoinhaber:"));}
    if(s.equals("STORNO_KURZ")){assertTrue(text.contains("Einbehalt beträgt"));assertFalse(text.contains("Kontoinhaber:"));}
    if(s.equals("STORNO_OHNE_ZAHLUNG")){assertTrue(text.contains("kein noch zu erstattender Betrag"));assertFalse(text.contains("Kontoinhaber:"));}
    if(s.equals("ANZAHLUNG"))assertTrue(text.contains("überweist die Anzahlung"));
    if(s.equals("GESAMTMIETE_VORAUS")){assertTrue(text.contains("überweist die Gesamtmiete"));assertFalse(text.contains("überweist die Anzahlung"));}
    if(s.equals("BEI_ABRECHNUNG"))assertFalse(text.contains("Bitte überweist"));
    if(entry.vorgang()==Vorgang.BESTAETIGUNG)assertFalse(text.contains("Bitte überweist"));
   }
   String export=System.getProperty("kc.preview.dir");if(export!=null && !og){var dir=java.nio.file.Path.of(export);java.nio.file.Files.createDirectories(dir);java.nio.file.Files.write(dir.resolve(entry.vorgang()+"_"+s+".pdf"),result.pdf());}
  }
  verify(vr,never()).saveAndFlush(any());verify(pr,never()).saveAndFlush(any());
 }
}
