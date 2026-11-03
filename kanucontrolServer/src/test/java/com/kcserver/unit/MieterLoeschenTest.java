package com.kcserver.unit;
import com.kcserver.core.entity.Person;
import com.kcserver.core.mapper.PersonMapper;
import com.kcserver.kjfp.repository.PersonRepository;
import com.kcserver.vermietung.entity.Mieter;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.MieterService;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MieterLoeschenTest {
    MieterRepository mieter; PersonRepository personen; BuchungRepository buchungen; MieterService service; Mieter profile; Person person;
    @BeforeEach void setup(){
        mieter=mock(MieterRepository.class);personen=mock(PersonRepository.class);buchungen=mock(BuchungRepository.class);
        service=new MieterService(mieter,personen,buchungen,mock(PersonMapper.class));
        person=new Person();person.setId(41L);person.setKjfpSichtbar(true);person.setAktiv(true);
        profile=new Mieter();profile.setId(5L);profile.setPerson(person);profile.setAktiv(true);
        when(mieter.findById(5L)).thenReturn(Optional.of(profile));when(mieter.findLockedById(5L)).thenReturn(Optional.of(profile));when(personen.findLockedById(41L)).thenReturn(Optional.of(person));
    }
    @Test void ohneBuchungWirdNurDerMietereintragGeloescht(){
        service.delete(5L);verify(mieter).delete(profile);verify(mieter).flush();verify(buchungen).existsByMieterId(41L);
        verify(buchungen,never()).existsByMieterId(5L);verify(personen,never()).delete(any(Person.class));verify(personen,never()).deleteById(any());assertTrue(person.isKjfpSichtbar());assertTrue(person.isAktiv());
    }
    @Test void jedeZugeordneteBuchungVerhindertLoeschenUnabhaengigVomStatus(){
        when(buchungen.existsByMieterId(41L)).thenReturn(true);
        var e=assertThrows(ResponseStatusException.class,()->service.delete(5L));assertEquals(409,e.getStatusCode().value());assertTrue(e.getReason().contains("stornierte und abgeschlossene"));verify(mieter,never()).delete(any());verify(mieter,never()).flush();assertTrue(profile.isAktiv());
    }
    @Test void erstNachLoeschenDerLetztenBuchungIstLoeschenMoeglich(){
        when(buchungen.existsByMieterId(41L)).thenReturn(true,false);
        assertThrows(ResponseStatusException.class,()->service.delete(5L));assertDoesNotThrow(()->service.delete(5L));verify(mieter,times(1)).delete(profile);
    }
    @Test void auchArchivierteMieterMitBuchungenSindGeschuetzt(){profile.setAktiv(false);when(buchungen.existsByMieterId(41L)).thenReturn(true);assertThrows(ResponseStatusException.class,()->service.delete(5L));verify(mieter,never()).delete(any());}
    @Test void archivierterMieterOhneBuchungKannGeloeschtWerden(){profile.setAktiv(false);assertDoesNotThrow(()->service.delete(5L));verify(mieter).delete(profile);}
    @Test void nurVermietungsPersonWirdNichtVersehentlichAlsKjfpPersonAngelegt(){person.setKjfpSichtbar(false);service.delete(5L);assertFalse(person.isKjfpSichtbar());verify(personen,never()).save(any());verify(personen,never()).delete(any(Person.class));}
    @Test void unbekannterMieterErgibt404(){assertEquals(404,assertThrows(ResponseStatusException.class,()->service.delete(999L)).getStatusCode().value());verifyNoInteractions(buchungen,personen);verify(mieter,never()).delete(any());}
    @Test void zwischenzeitlichGeloeschterMieterErgibt404(){when(mieter.findLockedById(5L)).thenReturn(Optional.empty());assertEquals(404,assertThrows(ResponseStatusException.class,()->service.delete(5L)).getStatusCode().value());verifyNoInteractions(buchungen);verify(mieter,never()).delete(any());}
    @Test void archivierenBleibtAuchMitBuchungenMoeglichUndLoeschtNicht(){when(buchungen.existsByMieterId(41L)).thenReturn(true);service.deactivate(5L);assertFalse(profile.isAktiv());verify(mieter,never()).delete(any());verifyNoInteractions(buchungen);}
    @Test void buchungenWerdenNachDerGemeinsamenPersonensperreErneutGeprueft(){
        var neu=new AtomicBoolean(false);when(personen.findLockedById(41L)).thenAnswer(inv->{neu.set(true);return Optional.of(person);});when(buchungen.existsByMieterId(41L)).thenAnswer(inv->neu.get());
        assertThrows(ResponseStatusException.class,()->service.delete(5L));verify(mieter,never()).delete(any());
    }
}
