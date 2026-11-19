package com.kcserver.vermietung.service;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.enumtype.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class StornoRuecknahmeTest {
 BuchungService service;BuchungRepository br;MietobjektRepository objects;HomepageStatusMailService mails;VermietungAenderungsprotokoll log;Buchung b;
 @BeforeEach void setup(){br=mock(BuchungRepository.class);objects=mock(MietobjektRepository.class);mails=mock(HomepageStatusMailService.class);log=mock(VermietungAenderungsprotokoll.class);
 service=new BuchungService(br,mock(MieterRepository.class),mock(BuchungsnummerCounterRepository.class),mock(com.kcserver.kjfp.repository.PersonRepository.class),mock(com.kcserver.core.repository.VereinRepository.class),objects,mock(MietbereichRepository.class),mock(MietpreisRepository.class),mock(MietbereichRabattRepository.class),mock(ZaehlerRepository.class),mock(ZaehlerstandRepository.class),mock(VerbrauchsabrechnungService.class),mock(BuchungVerbrauchskostenRepository.class),mock(BuchungFinanzbewegungRepository.class),mails, org.mockito.Mockito.mock(com.kcserver.vermietung.service.VermietungAenderungsprotokoll.class));
 ReflectionTestUtils.setField(service,"aenderungsprotokoll",log);b=AbrechnungVorlagenTest.buchung();b.setStatus(Buchungsstatus.STORNIERT);b.setStornoDatum(LocalDate.now());b.setStornoEinbehalt(new java.math.BigDecimal("25"));var area=new Mietbereich();area.setId(10L);area.setBezeichnung("EG");area.setMietobjekt(b.getMietobjekt());area.setBestand(1);var p=new BuchungMietbereich();p.setMietbereich(area);p.setAnzahl(1);p.setVereinbarterPreis(java.math.BigDecimal.TEN);p.setBerechnungsart(Berechnungsart.BUCHUNG);b.getMietbereiche().add(p);when(br.findLockedById(1L)).thenReturn(Optional.of(b));when(br.save(any())).thenAnswer(i->i.getArgument(0));}
 @Test void freieRuecknahmeBestaetigtOhneMailUndBehaeltNummer(){String nummer=b.getBuchungsnummer();service.stornoZuruecknehmen(1L);assertEquals(Buchungsstatus.BESTAETIGT,b.getStatus());assertEquals(nummer,b.getBuchungsnummer());assertNull(b.getStornoDatum());assertEquals(0,b.getStornoEinbehalt().signum());verify(objects).findLockedById(7L);verify(log).schreiben(eq("STORNIERUNG_ZURUECKGENOMMEN"),any());verifyNoInteractions(mails);}
 @Test void konfliktLaesstStornierteBuchungUnveraendert(){var other=AbrechnungVorlagenTest.buchung();other.setId(2L);other.setStatus(Buchungsstatus.RESERVIERT);other.getMietbereiche().addAll(b.getMietbereiche());when(br.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThanAndIdNot(any(),any(),any(),eq(1L))).thenReturn(List.of(other));assertThrows(IllegalArgumentException.class,()->service.stornoZuruecknehmen(1L));assertEquals(Buchungsstatus.STORNIERT,b.getStatus());assertNotNull(b.getStornoDatum());verifyNoInteractions(mails,log);}
}
