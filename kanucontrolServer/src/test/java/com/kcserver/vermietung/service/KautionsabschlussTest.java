package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.BuchungFinanzbewegungRepository;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KautionsabschlussTest {
 final BuchungFinanzbewegungRepository bewegungen=mock(BuchungFinanzbewegungRepository.class);
 final BuchungService service=new BuchungService(null,null,null,null,null,null,null,null,null,null,null,null,null,bewegungen,null, org.mockito.Mockito.mock(com.kcserver.vermietung.service.VermietungAenderungsprotokoll.class));
 final Buchung b=new Buchung();final List<BuchungFinanzbewegung> history=new ArrayList<>();
 @BeforeEach void setup(){b.setId(1L);b.setBuchungsquelle(Buchungsquelle.DIREKT);b.setKautionBetrag(new BigDecimal("250"));when(bewegungen.findByBuchungIdOrderByDatumAscIdAsc(1L)).thenReturn(history);}
 void move(Finanzbewegungsart art,String betrag){var m=new BuchungFinanzbewegung();m.setKategorie(Finanzkategorie.KAUTION);m.setArt(art);m.setBetrag(new BigDecimal(betrag));history.add(m);}
 @Test void verrechnungMitRestguthabenErlaubtAbschlussOhneFiktiveRueckzahlung(){
  move(Finanzbewegungsart.EINGANG,"250");move(Finanzbewegungsart.VERRECHNUNG,"60.30");
  assertDoesNotThrow(()->service.pruefeKautionsabschluss(b));assertEquals(2,history.size());
  verify(bewegungen,never()).save(any());
 }
 @Test void fehlenderKautionseingangBleibtGesperrt(){
  move(Finanzbewegungsart.EINGANG,"200");move(Finanzbewegungsart.VERRECHNUNG,"60.30");
  assertThrows(IllegalArgumentException.class,()->service.pruefeKautionsabschluss(b));
 }
 @Test void ueberzogeneVerrechnungBleibtGesperrt(){
  move(Finanzbewegungsart.EINGANG,"250");move(Finanzbewegungsart.VERRECHNUNG,"260");
  assertThrows(IllegalArgumentException.class,()->service.pruefeKautionsabschluss(b));
 }
 @Test void vollstaendigeRueckzahlungErlaubtWeiterhinAbschluss(){
  move(Finanzbewegungsart.EINGANG,"250");move(Finanzbewegungsart.RUECKZAHLUNG,"250");assertDoesNotThrow(()->service.pruefeKautionsabschluss(b));
 }
 @Test void unverrechneteRestkautionBleibtGesperrt(){
  move(Finanzbewegungsart.EINGANG,"250");assertThrows(IllegalArgumentException.class,()->service.pruefeKautionsabschluss(b));
 }
}
