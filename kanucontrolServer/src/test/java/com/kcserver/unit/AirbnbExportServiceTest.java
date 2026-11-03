package com.kcserver.unit;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.dto.AirbnbExportDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.ical.*;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.AirbnbExportService;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AirbnbExportServiceTest {
    AirbnbExportRegistry registry;MietobjektRepository objekte;BuchungRepository buchungen;AirbnbExportService service;Mietobjekt objekt;
    AirbnbExportRegistry.Config config(boolean aktiv,long version){return new AirbnbExportRegistry.Config("club_a",7,aktiv,Set.of(10L,11L),version,true);}
    @BeforeEach void setup(){
        TenantContext.setCurrentTenant("club_a");registry=mock(AirbnbExportRegistry.class);objekte=mock(MietobjektRepository.class);buchungen=mock(BuchungRepository.class);service=new AirbnbExportService(registry,objekte,buchungen);
        objekt=new Mietobjekt();objekt.setId(7L);objekt.setAnreisezeit(LocalTime.of(16,0));objekt.setAbreisezeit(LocalTime.of(14,0));
        for(long id:List.of(10L,11L)){var b=new Mietbereich();b.setId(id);b.setBezeichnung(id==10?"Erdgeschoss mit Wiese":"Obergeschoss");b.setMietobjekt(objekt);objekt.getMietbereiche().add(b);}
        when(objekte.findById(7L)).thenReturn(Optional.of(objekt));when(objekte.findLockedById(7L)).thenReturn(Optional.of(objekt));
    }
    @AfterEach void clear(){TenantContext.clear();}
    @Test void defaultsWaehlenEgUndOgOhneAktivierung(){var dto=service.get(7L);assertEquals(Set.of(10L,11L),dto.mietbereichIds());assertFalse(dto.aktiv());assertNull(dto.feedPfad());}
    @Test void neueLinksWerdenNurEinmalAusgegeben(){when(registry.get("club_a",7)).thenReturn(Optional.empty(),Optional.of(config(true,1)));var dto=service.save(7L,new AirbnbExportDTO(true,Set.of(10L,11L),0L,false,null),true);assertTrue(dto.feedPfad().matches("/api/kalender/airbnb/[A-Za-z0-9_-]{43}\\.ics"));when(registry.get("club_a",7)).thenReturn(Optional.of(config(true,1)));assertNull(service.get(7L).feedPfad());}
    @Test void versionsvergleichFunktioniertAuchUeberLongCacheGrenze(){when(registry.get("club_a",7)).thenReturn(Optional.of(config(true,128)),Optional.of(config(true,129)));when(registry.update(any(),anyBoolean(),anySet(),isNull())).thenReturn(true);assertEquals(129,service.save(7L,new AirbnbExportDTO(true,Set.of(10L,11L),128L,true,null),false).version());}
    @Test void fehlendeOderFremdeBereicheWerdenAbgelehnt(){assertThrows(IllegalArgumentException.class,()->service.save(7L,new AirbnbExportDTO(true,Set.of(),0L,false,null),false));assertThrows(IllegalArgumentException.class,()->service.save(7L,new AirbnbExportDTO(true,Set.of(999L),0L,false,null),false));verifyNoInteractions(registry);}
    @Test void alteVersionKannKeineEinstellungenUeberschreiben(){when(registry.get("club_a",7)).thenReturn(Optional.of(config(true,2)));assertEquals(409,assertThrows(ResponseStatusException.class,()->service.save(7L,new AirbnbExportDTO(true,Set.of(10L,11L),1L,true,null),true)).getStatusCode().value());verify(registry,never()).update(any(),anyBoolean(),anySet(),any());}
    @Test void fremdeMandantenDuerfenNichtGerendertWerden(){TenantContext.setCurrentTenant("club_b");assertEquals(404,assertThrows(ResponseStatusException.class,()->service.render(config(true,1))).getStatusCode().value());verifyNoInteractions(buchungen);}
    @Test void fehlendeRessourceLiefertFehlerStattFaelschlichFrei(){when(registry.get("club_a",7)).thenReturn(Optional.of(config(true,1)));objekt.getMietbereiche().clear();assertEquals(503,assertThrows(ResponseStatusException.class,()->service.render(config(true,1))).getStatusCode().value());verifyNoInteractions(buchungen);}
    @Test void deaktivierungLiefertLeerenKalenderAuchNachBereichsloeschung(){when(registry.get("club_a",7)).thenReturn(Optional.of(config(false,1)));objekt.getMietbereiche().clear();assertFalse(service.render(config(false,1)).contains("VEVENT"));verifyNoInteractions(buchungen);}
}
