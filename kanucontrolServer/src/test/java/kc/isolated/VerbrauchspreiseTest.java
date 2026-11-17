package kc.isolated;
import com.kcserver.vermietung.entity.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class VerbrauchspreiseTest {
 @Test void inklusiveGrenzenUndKeinErsatzpreis(){
  var k=new Verbrauchskosten(); k.setBezeichnung("Wasser");
  k.getPreise().add(new Verbrauchspreis(LocalDate.of(2025,1,1),LocalDate.of(2025,12,31),BigDecimal.TEN));
  k.getPreise().add(new Verbrauchspreis(LocalDate.of(2026,1,1),null,new BigDecimal("20")));
  assertEquals(BigDecimal.TEN,k.zumDatum(LocalDate.of(2025,1,1)).getPreis());
  assertEquals(BigDecimal.TEN,k.zumDatum(LocalDate.of(2025,12,31)).getPreis());
  assertEquals(new BigDecimal("20"),k.zumDatum(LocalDate.of(2026,1,1)).getPreis());
  assertThrows(IllegalArgumentException.class,()->k.zumDatum(LocalDate.of(2024,12,31)));
 }

 private Verbrauchskosten config(){
  var k=new Verbrauchskosten();k.setId(2L);k.setBezeichnung("Wasser");k.setVerbrauchsart(com.kcserver.vermietung.enumtype.Verbrauchsart.WASSER);k.setAbrechnungsart(com.kcserver.vermietung.enumtype.Verbrauchsabrechnungsart.TAGESPREIS);
  var o=new Mietobjekt();o.setId(1L);k.setMietobjekt(o);
  k.getPreise().add(new Verbrauchspreis(LocalDate.of(2025,1,1),LocalDate.of(2025,12,31),BigDecimal.TEN));
  k.getPreise().add(new Verbrauchspreis(LocalDate.of(2026,1,1),null,new BigDecimal("20")));return k;
 }
 @Test void rueckwirkendesEinfuegenUndValidierung(){
  var k=config();var repo=org.mockito.Mockito.mock(com.kcserver.vermietung.repository.VerbrauchskostenRepository.class);
  org.mockito.Mockito.when(repo.findById(2L)).thenReturn(java.util.Optional.of(k));org.mockito.Mockito.when(repo.findForUpdate(2L)).thenReturn(java.util.Optional.of(k));org.mockito.Mockito.when(repo.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i->i.getArgument(0));
  var service=new com.kcserver.vermietung.service.VerbrauchskostenService(repo,org.mockito.Mockito.mock(com.kcserver.vermietung.repository.MietobjektRepository.class));
  var d=new com.kcserver.vermietung.dto.VerbrauchskostenDTO();d.setBezeichnung("Wasser");d.setVerbrauchsart(k.getVerbrauchsart());d.setAbrechnungsart(k.getAbrechnungsart());
  d.setPreise(new java.util.ArrayList<>(k.getPreise().stream().map(p->new Verbrauchspreis(p.getGueltigVon(),p.getGueltigBis(),p.getPreis())).toList()));
  d.getPreise().add(new Verbrauchspreis(LocalDate.of(2025,7,1),null,new BigDecimal("15")));
  var result=service.update(1L,2L,d);assertEquals(LocalDate.of(2025,6,30),result.getPreise().get(0).getGueltigBis());assertEquals(LocalDate.of(2025,12,31),result.getPreise().get(1).getGueltigBis());assertNull(result.getPreise().get(2).getGueltigBis());
  d.setPreise(java.util.List.of(new Verbrauchspreis(LocalDate.of(2025,1,1),LocalDate.of(2026,6,30),BigDecimal.TEN),new Verbrauchspreis(LocalDate.of(2026,1,1),null,BigDecimal.ONE)));
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.update(1L,2L,d));
  d.setPreise(java.util.List.of(new Verbrauchspreis(LocalDate.of(2025,1,1),LocalDate.of(2024,1,1),BigDecimal.TEN)));
  assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.update(1L,2L,d));
 }
 @Test void anreisetagFehlenderPreisUndAbschluss(){
  var k=config();var repo=org.mockito.Mockito.mock(com.kcserver.vermietung.repository.VerbrauchskostenRepository.class);
  var saved=org.mockito.Mockito.mock(com.kcserver.vermietung.repository.BuchungVerbrauchskostenRepository.class);
  org.mockito.Mockito.when(repo.findByMietobjektIdOrderByIdAsc(1L)).thenReturn(java.util.List.of(k));
  var service=new com.kcserver.vermietung.service.VerbrauchsabrechnungService(repo,org.mockito.Mockito.mock(com.kcserver.vermietung.repository.ZaehlerRepository.class),org.mockito.Mockito.mock(com.kcserver.vermietung.repository.ZaehlerstandRepository.class),saved);
  var b=new Buchung();b.setId(3L);b.setMietobjekt(k.getMietobjekt());b.setBuchungsquelle(com.kcserver.vermietung.enumtype.Buchungsquelle.DIREKT);b.setAnreise(LocalDate.of(2025,12,30).atStartOfDay());b.setAbreise(LocalDate.of(2026,1,3).atStartOfDay());
  var sn=service.berechneSnapshots(b,java.util.List.of()).getFirst();assertEquals(BigDecimal.TEN,sn.getPreis());assertEquals(new BigDecimal("40.00"),sn.getKosten());assertEquals(sn.getKosten(),service.berechneVorschau(b).getFirst().kosten());
  b.setAnreise(LocalDate.of(2024,1,1).atStartOfDay());assertFalse(service.berechneVorschau(b).getFirst().erfolgreich());assertThrows(IllegalArgumentException.class,()->service.berechneUndSpeichere(b));org.mockito.Mockito.verifyNoInteractions(saved);
  b.setStatus(com.kcserver.vermietung.enumtype.Buchungsstatus.ABGESCHLOSSEN);org.mockito.Mockito.when(saved.findByBuchungIdOrderByIdAsc(3L)).thenReturn(java.util.List.of(sn));k.getPreise().clear();
  assertSame(sn,service.berechneSimulationsSnapshots(b,java.util.List.of()).getFirst());assertEquals(BigDecimal.TEN,service.berechneVorschau(b).getFirst().preis());service.berechneUndSpeichere(b);org.mockito.Mockito.verify(saved,org.mockito.Mockito.never()).deleteByBuchungId(org.mockito.ArgumentMatchers.any());
 }
}
