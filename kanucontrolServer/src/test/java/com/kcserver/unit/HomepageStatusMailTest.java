package com.kcserver.unit;

import com.kcserver.core.entity.Person;
import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomepageStatusMailTest {
    private ValidatorFactory factory;
    private HomepageStatusMailRepository mails;
    private HomepageAnbindungRepository configs;
    private HomepageStatusMailService service;
    private Buchung buchung;
    @BeforeEach void setup() {
        factory=Validation.buildDefaultValidatorFactory();mails=mock(HomepageStatusMailRepository.class);
        configs=mock(HomepageAnbindungRepository.class);service=new HomepageStatusMailService(mails,configs,factory.getValidator());
        buchung=new Buchung();buchung.setId(1L);buchung.setBuchungsjahr(2027);buchung.setLaufendeNummer(1);
        buchung.setHerkunft("HOMEPAGE");buchung.setAnreise(LocalDateTime.of(2027,6,10,16,0));buchung.setAbreise(LocalDateTime.of(2027,6,12,14,0));
        Person person=new Person();person.setVorname("Erika");person.setEmail("erika@example.org");buchung.setMieter(person);
        Mietobjekt objekt=new Mietobjekt();objekt.setId(7L);objekt.setBezeichnung("Bootshaus");buchung.setMietobjekt(objekt);
    }
    @AfterEach void close() { factory.close(); }
    @Test void jederStatusErhaeltEinenPassendenTextMitBuchungsbezug() {
        Map<Buchungsstatus,String> keywords=Map.of(Buchungsstatus.ANFRAGE,"Anfrage",Buchungsstatus.RESERVIERT,"vorläufig reserviert",
            Buchungsstatus.BESTAETIGT,"bestätigen",Buchungsstatus.ABGELEHNT,"abgelehnt",Buchungsstatus.STORNIERT,"storniert",
            Buchungsstatus.ABRECHENBAR,"noch nicht abgeschlossen",Buchungsstatus.ABGESCHLOSSEN,"ist abgeschlossen");
        for (Buchungsstatus status:Buchungsstatus.values()) {
            buchung.setStatus(status==Buchungsstatus.ANFRAGE?Buchungsstatus.RESERVIERT:Buchungsstatus.ANFRAGE);
            service.vormerken(buchung,status);
        }
        var captor=ArgumentCaptor.forClass(HomepageStatusMail.class);verify(mails,times(7)).save(captor.capture());
        for (HomepageStatusMail m:captor.getAllValues()) {
            assertEquals(7L,m.getMietobjektId());assertEquals(1L,m.getBuchungId());assertEquals("2027-001",m.getBuchungsnummer());
            assertEquals("erika@example.org",m.getEmpfaenger());assertTrue(m.getBetreff().contains("2027-001"));
            assertTrue(m.getText().contains("10.06.2027"));assertTrue(m.getText().contains("12.06.2027"));
            assertTrue(m.getText().contains("Bootshaus"));assertTrue(m.getText().contains(keywords.get(Buchungsstatus.valueOf(m.getStatus()))));
            assertNotNull(m.getCreatedAt());assertNull(m.getQuittiertAm());
        }
    }
    @Test void reservierungIstNochKeineEndgueltigeBestaetigung() {
        service.vormerken(buchung,Buchungsstatus.RESERVIERT);
        var c=ArgumentCaptor.forClass(HomepageStatusMail.class);verify(mails).save(c.capture());
        assertTrue(c.getValue().getText().contains("noch keine endgültige Buchungsbestätigung"));
    }
    @Test void wiedereroeffneteAbrechnungIstKeineNeueEndabrechnung() {
        buchung.setStatus(Buchungsstatus.ABGESCHLOSSEN);service.vormerken(buchung,Buchungsstatus.ABRECHENBAR);
        var c=ArgumentCaptor.forClass(HomepageStatusMail.class);verify(mails).save(c.capture());
        assertTrue(c.getValue().getText().contains("erneut geöffnet"));
    }
    @Test void unveraenderterStatusUndInterneBuchungenErzeugenKeineZusaetzlicheMail() {
        service.vormerken(buchung,Buchungsstatus.ANFRAGE);
        buchung.setHerkunft(null);service.vormerken(buchung,Buchungsstatus.RESERVIERT);
        verifyNoInteractions(mails);
    }
    @Test void fehlenderKontaktVerhindertEineLautlosVerloreneBenachrichtigung() {
        buchung.getMieter().setEmail(null);
        assertThrows(IllegalArgumentException.class,()->service.vormerken(buchung,Buchungsstatus.BESTAETIGT));
        verifyNoInteractions(mails);
    }
    private HomepageAnbindung freigabe() {
        HomepageAnbindung c=new HomepageAnbindung();c.setAktiv(true);c.setTechnischerClientId("ekc-homepage");
        c.setEmpfaengerEmail("bootshaus@example.org");c.setMietobjekt(buchung.getMietobjekt());
        when(configs.findById(7L)).thenReturn(Optional.of(c));return c;
    }
    @Test void antwortadresseKommtAusDerVereinskonfiguration() {
        freigabe();service.vormerken(buchung,Buchungsstatus.BESTAETIGT);
        var c=ArgumentCaptor.forClass(HomepageStatusMail.class);verify(mails).save(c.capture());assertEquals("bootshaus@example.org",c.getValue().getReplyTo());
    }
    @Test void abrufIstAufFreigegebenenClientUndMietobjektBeschraenkt() {
        HomepageAnbindung c=freigabe();
        assertThrows(ResponseStatusException.class,()->service.offen(7L,"fremd"));
        c.setAktiv(false);assertThrows(ResponseStatusException.class,()->service.offen(7L,"ekc-homepage"));
        verifyNoInteractions(mails);
    }
    @Test void nichtMehrBuchbaresObjektDarfStatusnachrichtenWeiterAusliefern() {
        freigabe();buchung.getMietobjekt().setMietbar(false);
        when(mails.findTop20ByMietobjektIdAndQuittiertAmIsNullOrderByCreatedAtAscIdAsc(7L)).thenReturn(List.of());
        assertTrue(service.offen(7L,"ekc-homepage").isEmpty());
    }
    @Test void quittierungIstIdempotentUndObjektgebunden() {
        freigabe();var mail=new HomepageStatusMail();mail.setId(UUID.randomUUID().toString());
        when(mails.findByIdAndMietobjektId(mail.getId(),7L)).thenReturn(Optional.of(mail));
        service.quittieren(7L,"ekc-homepage",mail.getId());Instant first=mail.getQuittiertAm();assertNotNull(first);
        service.quittieren(7L,"ekc-homepage",mail.getId());assertEquals(first,mail.getQuittiertAm());
        assertThrows(ResponseStatusException.class,()->service.quittieren(7L,"ekc-homepage","fremde-id"));
    }
}
