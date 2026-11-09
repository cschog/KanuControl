package com.kcserver.unit;
import com.kcserver.core.entity.Person;
import com.kcserver.core.mapper.PersonMapper;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.MieterService;
import com.kcserver.vermietung.dto.MieterSaveDTO;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MieterMietobjekteTest {
 MieterRepository mieter=mock(MieterRepository.class);
 PersonRepository personen=mock(PersonRepository.class);
 BuchungRepository buchungen=mock(BuchungRepository.class);
 MieterService service=new MieterService(mieter,personen,buchungen,mock(PersonMapper.class));
 Person person; Mieter profil;
 @BeforeEach void setup(){person=new Person();person.setId(101L);person.setVorname("Anna");person.setName("Adler");profil=new Mieter();profil.setId(1L);profil.setPerson(person);profil.setAktiv(true);
 when(mieter.findAllByOrderByPersonNameAscPersonVornameAsc()).thenReturn(List.of(profil));
 when(mieter.findById(1L)).thenReturn(Optional.of(profil));when(mieter.findLockedById(1L)).thenReturn(Optional.of(profil));
 when(personen.findLockedById(101L)).thenReturn(Optional.of(person));when(mieter.save(any())).thenAnswer(i->i.getArgument(0));}
 Buchung booking(Person p,String objekt,Buchungsquelle quelle,Buchungsstatus status){var b=new Buchung();b.setMieter(p);var o=new Mietobjekt();o.setBezeichnung(objekt);b.setMietobjekt(o);b.setBuchungsquelle(quelle);b.setStatus(status);return b;}
 @Test void objekteWerdenProPersonZusammengefasstOhneDoppelteNamenUndOhneFremdeBuchungen(){
 var fremd=new Person();fremd.setId(1L);
 when(buchungen.findAll()).thenReturn(List.of(booking(person,"Container",Buchungsquelle.DIREKT,Buchungsstatus.BESTAETIGT),booking(person,"Bootshaus",Buchungsquelle.AIRBNB,Buchungsstatus.ABGESCHLOSSEN),booking(person,"Bootshaus",Buchungsquelle.DIREKT,Buchungsstatus.STORNIERT),booking(fremd,"Fremdes Objekt",Buchungsquelle.DIREKT,Buchungsstatus.ANFRAGE)));
 var dto=service.getAll().getFirst();assertEquals(List.of("Bootshaus","Container"),dto.getMietobjekte());assertEquals(3,dto.getBuchungen());assertEquals(2,dto.getBuchungsquellen().size());assertEquals(101L,dto.getPersonId());
 assertEquals(dto.getMietobjekte(),service.get(1L).getMietobjekte());
 }
 @Test void mieterOhneBuchungenBekommtLeereObjektliste(){var dto=service.getAll().getFirst();assertTrue(dto.getMietobjekte().isEmpty());assertEquals(0,dto.getBuchungen());}
 @Test void statusAendernErhaeltBuchungsobjekte(){when(buchungen.findAll()).thenReturn(List.of(booking(person,"Bootshaus",Buchungsquelle.DIREKT,Buchungsstatus.RESERVIERT)));
 var dto=new MieterSaveDTO();dto.setVorname("Anna");dto.setName("Adler");dto.setAktiv(false);
 var result=service.update(1L,dto);assertFalse(result.isAktiv());assertEquals(List.of("Bootshaus"),result.getMietobjekte());assertEquals(1,result.getBuchungen());
 }
}
