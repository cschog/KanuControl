package com.kcserver.unit;

import com.kcserver.vermietung.dto.SperrzeitDTO;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.SperrzeitService;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SperrzeitServiceTest {
    MietobjektRepository objekte; MietobjektSperrzeitRepository sperren; BuchungRepository buchungen;
    SperrzeitService service; Mietobjekt objekt;
    LocalDateTime von = LocalDateTime.of(2027,5,21,0,0), bis = von.plusDays(1);
    @BeforeEach void setup() {
        objekte=mock(MietobjektRepository.class); sperren=mock(MietobjektSperrzeitRepository.class); buchungen=mock(BuchungRepository.class);
        service=new SperrzeitService(objekte,sperren,buchungen); objekt=new Mietobjekt(); objekt.setId(7L);
        when(objekte.findById(7L)).thenReturn(Optional.of(objekt));
        when(objekte.findLockedById(7L)).thenReturn(Optional.of(objekt));
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of());
        when(sperren.saveAndFlush(any())).thenAnswer(inv -> { var s=(MietobjektSperrzeit)inv.getArgument(0);s.setId(1L);s.setVersion(0L);return s; });
    }
    SperrzeitDTO input(LocalDateTime a, LocalDateTime b, boolean ganztag) { return new SperrzeitDTO(null,null,a,b,ganztag,"Wartung intern"); }
    MietobjektSperrzeit vorhanden() {
        var s=new MietobjektSperrzeit();s.setId(1L);s.setVersion(2L);s.setMietobjekt(objekt);s.setVon(von);s.setBis(bis);s.setGrund("Geheim");objekt.getSperrzeiten().add(s);return s;
    }
    @Test void ganzerTagWirdOhneBuchungGespeichert() {
        var saved=service.save(7L,null,input(von,bis,true));
        assertEquals(von,saved.von());assertEquals(bis,saved.bis());assertTrue(saved.ganztaegig());
        verify(objekte).findLockedById(7L); verify(buchungen,never()).save(any());
    }
    @Test void zeitfensterAuchAmSelbenTagErlaubt() {
        assertDoesNotThrow(()->service.save(7L,null,input(von.plusHours(8),von.plusHours(12),false)));
    }
    @Test void ungueltigeZeitenUndGruendeWerdenAbgelehnt() {
        assertThrows(IllegalArgumentException.class,()->service.save(7L,null,input(von,von,false)));
        assertThrows(IllegalArgumentException.class,()->service.save(7L,null,input(bis,von,false)));
        assertThrows(IllegalArgumentException.class,()->service.save(7L,null,input(von.plusHours(8),bis,true)));
        assertThrows(IllegalArgumentException.class,()->service.save(7L,null,new SperrzeitDTO(null,null,von,bis,true," ")));
        assertThrows(IllegalArgumentException.class,()->service.save(7L,null,new SperrzeitDTO(null,null,von,bis,true,"x".repeat(501))));
        verifyNoInteractions(sperren);
    }
    @Test void angrenzendeZeitenSindFrei() {
        vorhanden();
        assertDoesNotThrow(()->SperrzeitService.pruefe(objekt,von.minusDays(1),von));
        assertDoesNotThrow(()->SperrzeitService.pruefe(objekt,bis,bis.plusHours(2)));
        assertDoesNotThrow(()->service.save(7L,null,input(bis,bis.plusDays(1),true)));
    }
    @Test void ueberlappungBlockiertUndGrundBleibtPrivat() {
        vorhanden();
        var e=assertThrows(IllegalArgumentException.class,()->SperrzeitService.pruefe(objekt,von.plusHours(16),bis.plusHours(14)));
        assertFalse(e.getMessage().contains("Geheim"));
        assertThrows(ResponseStatusException.class,()->service.save(7L,null,input(von,bis,true)));
    }
    @Test void bestehendeBuchungVerhindertSperre() {
        Buchung b=new Buchung();b.setMietobjekt(objekt);b.setStatus(Buchungsstatus.BESTAETIGT);b.setBuchungsjahr(2027);b.setLaufendeNummer(5);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of(b));
        var e=assertThrows(ResponseStatusException.class,()->service.save(7L,null,input(von,bis,true)));
        assertEquals(409,e.getStatusCode().value());assertTrue(e.getReason().contains("2027-005"));verifyNoInteractions(sperren);
    }
    @Test void fremdesObjektBlockiertNicht() {
        var fremd=new Mietobjekt();fremd.setId(8L);var b=new Buchung();b.setMietobjekt(fremd);
        when(buchungen.findByStatusInAndAnreiseLessThanAndAbreiseGreaterThan(any(),any(),any())).thenReturn(List.of(b));
        assertDoesNotThrow(()->service.save(7L,null,input(von,bis,true)));
    }
    @Test void unbekanntesObjektOderFremdeSperreErgibt404() {
        assertEquals(404,assertThrows(ResponseStatusException.class,()->service.list(8L)).getStatusCode().value());
        assertEquals(404,assertThrows(ResponseStatusException.class,()->service.delete(7L,999L,0L)).getStatusCode().value());
        assertEquals(404,assertThrows(ResponseStatusException.class,()->service.save(7L,999L,input(von,bis,true))).getStatusCode().value());
    }
    @Test void gleichzeitigeAenderungenWerdenErkannt() {
        vorhanden();
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.save(7L,1L,new SperrzeitDTO(1L,1L,von,bis,true,"Neu"))).getStatusCode().value());
        assertEquals(409,assertThrows(ResponseStatusException.class,()->service.delete(7L,1L,1L)).getStatusCode().value());
        assertEquals(1,objekt.getSperrzeiten().size());
    }
    @Test void bearbeitenUndAufhebenMitAktuellerVersion() {
        vorhanden();
        assertDoesNotThrow(()->service.save(7L,1L,new SperrzeitDTO(1L,2L,von,bis,true,"Neu")));
        // save mock assigns version 0; deletion still requires current version.
        service.delete(7L,1L,0L);assertTrue(objekt.getSperrzeiten().isEmpty());verify(objekte).flush();
    }
}
