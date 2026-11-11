package com.kcserver.vermietung.service;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.BuchungVerbrauchskosten;
import com.kcserver.vermietung.enumtype.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AbrechnungKautionsdarstellungTest {
 static BigDecimal n(String s){return new BigDecimal(s);}
 @Test void kautionseingangGenuegtOhneInterneUmbuchungszeilen() throws Exception {
  var date=LocalDate.of(2026,11,11);
  var movements=List.of(
   new FinanzbewegungDTO(1L,Finanzkategorie.ANZAHLUNG,Finanzbewegungsart.EINGANG,n("50"),date,""),
   new FinanzbewegungDTO(2L,Finanzkategorie.KAUTION,Finanzbewegungsart.EINGANG,n("250"),date,""),
   new FinanzbewegungDTO(3L,Finanzkategorie.KAUTION,Finanzbewegungsart.VERRECHNUNG,n("250"),date,""),
   new FinanzbewegungDTO(4L,Finanzkategorie.VERBRAUCHSKOSTEN,Finanzbewegungsart.VERRECHNUNG,n("250"),date,""));
  var f=new BuchungFinanzenDTO(1L,"2026-020","Testmieter","Bootshaus",Buchungsquelle.DIREKT,"ABGESCHLOSSEN",date,date,false,n("50"),n("250"),
   List.of(new MietkostenPositionDTO("Miete",1,n("465"),n("465"),1L,"BUCHUNG")),BigDecimal.ZERO,n("465"),n("637.29"),true,movements,List.of(),BigDecimal.ZERO);
  var v=new BuchungVerbrauchskosten();v.setBezeichnung("Verbrauchskosten");v.setKosten(n("637.29"));v.setPreis(n("637.29"));
  var table=Abrechnungsbereich.aus(f,List.of(v),1);
  assertEquals(n("802.29"),f.saldo());
  assertEquals(2,table.zeilen().stream().filter(z->z.beschreibung().startsWith("Bereits gezahlt")).count());
  assertFalse(table.zeilen().stream().anyMatch(z->z.beschreibung().contains("Verrechnung")||z.beschreibung().contains("Kategorieausgleich")));
  assertTrue(table.zeilen().getLast().betrag().startsWith("802,29"));
  var papier=getClass().getResourceAsStream("/pdf/Briefpapier_Bootshaus.pdf").readAllBytes();
  var pdf=AbrechnungBriefLayout.render(papier,Map.of(),List.of("Abrechnung"),"Mit freundlichen Grüßen",null,"2026-020",table);
  try(var doc=org.apache.pdfbox.Loader.loadPDF(pdf)){
   String text=new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
   assertFalse(text.contains("Verrechnung am"));assertTrue(text.contains("(Kaution)"));assertTrue(text.contains("802,29"));
  }
 }
}
