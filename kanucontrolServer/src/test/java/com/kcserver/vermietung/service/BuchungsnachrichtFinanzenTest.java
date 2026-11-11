package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BuchungsnachrichtFinanzenTest {
 @Test void stornoBleibtFinanziellErreichbarUndNurAusgefuehrteRueckzahlungAendertSaldo(){
  var b=AbrechnungVorlagenTest.buchung();b.setStatus(Buchungsstatus.STORNIERT);b.setStornoDatum(LocalDate.now());b.setStornoErstattung(new BigDecimal("1000"));b.setStornoEinbehalt(BigDecimal.ZERO);
  var br=mock(BuchungRepository.class);var mr=mock(BuchungFinanzbewegungRepository.class);var vr=mock(BuchungVerbrauchskostenRepository.class);
  var bewegungen=new ArrayList<BuchungFinanzbewegung>();var eingang=new BuchungFinanzbewegung();eingang.setId(1L);eingang.setBuchung(b);eingang.setKategorie(Finanzkategorie.MIETE);eingang.setArt(Finanzbewegungsart.EINGANG);eingang.setBetrag(new BigDecimal("1000"));eingang.setDatum(LocalDate.of(2026,1,1));bewegungen.add(eingang);
  when(br.findAll()).thenReturn(List.of(b));when(br.findLockedById(1L)).thenReturn(Optional.of(b));when(mr.findByBuchungIdOrderByDatumAscIdAsc(1L)).thenReturn(bewegungen);when(vr.findByBuchungIdOrderByIdAsc(1L)).thenReturn(List.of());when(mr.save(any())).thenAnswer(i->{BuchungFinanzbewegung m=i.getArgument(0);m.setId(2L);bewegungen.add(m);return m;});
  var service=new BuchungFinanzenService(br,mr,vr,mock(VerbrauchsabrechnungService.class));assertEquals(1,service.getAll().size());assertEquals(new BigDecimal("-1000.00"),service.toDto(b).saldo());verify(mr,never()).save(any());
  var result=service.add(1L,new FinanzbewegungSaveDTO(Finanzkategorie.MIETE,Finanzbewegungsart.RUECKZAHLUNG,new BigDecimal("100"),LocalDate.of(2026,1,2),"Tatsächlich überwiesen"));assertEquals(new BigDecimal("-900.00"),result.saldo());assertEquals(new BigDecimal("1000"),b.getStornoErstattung());
  b.setStornoEinbehalt(new BigDecimal("900"));assertEquals(new BigDecimal("0.00"),service.toDto(b).saldo());assertThrows(IllegalArgumentException.class,()->service.add(1L,new FinanzbewegungSaveDTO(Finanzkategorie.MIETE,Finanzbewegungsart.RUECKZAHLUNG,new BigDecimal("50"),LocalDate.of(2026,1,3),"Nicht zulässig")));verify(mr,times(1)).save(any());
 }
}
