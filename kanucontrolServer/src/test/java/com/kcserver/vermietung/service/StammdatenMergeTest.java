package com.kcserver.vermietung.service;
import com.kcserver.core.entity.*;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.dto.*;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.core.repository.VereinRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class StammdatenMergeTest {
 MieterRepository mr;PersonRepository pr;VereinRepository vr;BuchungRepository br;MieterService ms;VeranstalterService vs;VermietungAenderungsprotokoll log;StammdatenMergeService service;
 Mieter source,target;Verein sourceV,targetV;List<Buchung> bookings;jakarta.validation.ValidatorFactory factory;
 @BeforeEach void setup(){TenantContext.setCurrentTenant("test_a");mr=mock(MieterRepository.class);pr=mock(PersonRepository.class);vr=mock(VereinRepository.class);br=mock(BuchungRepository.class);ms=mock(MieterService.class);vs=mock(VeranstalterService.class);log=mock(VermietungAenderungsprotokoll.class);factory=Validation.buildDefaultValidatorFactory();service=new StammdatenMergeService(mr,pr,vr,br,ms,vs,log,new ObjectMapper().findAndRegisterModules(),factory.getValidator());
 source=mieter(1,101);target=mieter(2,102);when(mr.findById(1L)).thenReturn(Optional.of(source));when(mr.findById(2L)).thenReturn(Optional.of(target));when(mr.findLockedById(anyLong())).thenAnswer(i->mr.findById(i.getArgument(0)));when(pr.findLockedById(101L)).thenReturn(Optional.of(source.getPerson()));when(pr.findLockedById(102L)).thenReturn(Optional.of(target.getPerson()));when(ms.get(1L)).thenReturn(dto(source));when(ms.get(2L)).thenReturn(dto(target));
 sourceV=new Verein();sourceV.setId(11L);sourceV.setName("Quelle");sourceV.setAbk("Q");targetV=new Verein();targetV.setId(12L);targetV.setName("Ziel");targetV.setAbk("Z");targetV.setKjfpVerein(true);when(vr.findById(11L)).thenReturn(Optional.of(sourceV));when(vr.findById(12L)).thenReturn(Optional.of(targetV));when(vr.findLockedById(anyLong())).thenAnswer(i->vr.findById(i.getArgument(0)));when(vs.lesen(11L)).thenReturn(dto(sourceV));when(vs.lesen(12L)).thenReturn(dto(targetV));
 bookings=new ArrayList<>();for(var status:List.of(Buchungsstatus.ABGESCHLOSSEN,Buchungsstatus.STORNIERT)){var b=AbrechnungVorlagenTest.buchung();b.setId((long)bookings.size()+1);b.setStatus(status);b.setMieter(source.getPerson());b.setVeranstalter(sourceV);bookings.add(b);}when(br.findAll()).thenAnswer(i->List.copyOf(bookings));when(br.findLockedById(anyLong())).thenAnswer(i->bookings.stream().filter(b->b.getId().equals(i.getArgument(0))).findFirst());
 }
 Mieter mieter(long id,long person){var p=new Person();p.setId(person);p.setVorname("Vorname");p.setName("Name");var m=new Mieter();m.setId(id);m.setPerson(p);return m;}
 MieterDTO dto(Mieter m){var d=new MieterDTO();d.setId(m.getId());d.setPersonId(m.getPerson().getId());d.setVorname(m.getPerson().getVorname());d.setName(m.getPerson().getName());d.setCountryCode("DE");d.setAktiv(true);return d;}
 VeranstalterDTO dto(Verein v){var d=new VeranstalterDTO();d.setId(v.getId());d.setName(v.getName());d.setAbk(v.getAbk());d.setCountryCode("DE");return d;}
 @AfterEach void close(){factory.close();TenantContext.clear();}
 @Test void mieterMitHistorischenBuchungenUndZahlungsbezugWirdAtomarUmgehaengt(){var preview=service.vorschau(true,1L,2L);assertEquals(2,preview.buchungen().size());var move=new BuchungFinanzbewegung();move.setBuchung(bookings.getFirst());service.zusammenfuehren(true,new StammdatenMergeService.Auftrag(1L,2L,Map.of(),preview.fingerprint()));assertTrue(bookings.stream().allMatch(b->b.getMieter()==target.getPerson()));assertSame(bookings.getFirst(),move.getBuchung());assertEquals(Buchungsstatus.ABGESCHLOSSEN,bookings.getFirst().getStatus());assertFalse(source.isAktiv());assertEquals(2L,source.getZusammengefuehrtIn());verify(mr,never()).delete(any());verify(pr,never()).delete(any(Person.class));verify(log).schreiben(eq("MIETER_ZUSAMMENGEFUEHRT"),any());}
 @Test void veranstalterMergeLaesstKjfpIdentitaetenUndStammdatenUnveraendert(){var preview=service.vorschau(false,11L,12L);assertTrue(preview.zielGeschuetzt());service.zusammenfuehren(false,new StammdatenMergeService.Auftrag(11L,12L,Map.of(),preview.fingerprint()));assertTrue(bookings.stream().allMatch(b->b.getVeranstalter()==targetV));assertEquals(12L,sourceV.getVermietungZusammengefuehrtIn());assertTrue(targetV.isKjfpVerein());assertEquals("Ziel",targetV.getName());verify(vs,never()).speichern(anyLong(),any());verify(vr,never()).delete(any(Verein.class));}
 @Test void geschuetztesZielUndVeralteteVorschauBlockierenOhneBuchungsaenderung(){var preview=service.vorschau(false,11L,12L);assertThrows(Exception.class,()->service.zusammenfuehren(false,new StammdatenMergeService.Auftrag(11L,12L,Map.of("name",true),preview.fingerprint())));assertThrows(Exception.class,()->service.zusammenfuehren(false,new StammdatenMergeService.Auftrag(11L,12L,Map.of(),"veraltet")));assertSame(sourceV,bookings.getFirst().getVeranstalter());verifyNoInteractions(log);}
 @Test void fehlenderMandantVerhindertVorschau(){TenantContext.clear();assertThrows(Exception.class,()->service.vorschau(true,1L,2L));verifyNoInteractions(log);}
}
