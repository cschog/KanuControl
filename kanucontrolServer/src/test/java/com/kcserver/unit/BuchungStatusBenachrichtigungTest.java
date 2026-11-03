package com.kcserver.unit;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungStatusBenachrichtigungTest {
    private BuchungRepository repository;
    private HomepageStatusMailService mails;
    private BuchungService service;
    private Buchung buchung;
    @BeforeEach void setup() {
        repository=mock(BuchungRepository.class);mails=mock(HomepageStatusMailService.class);
        service=new BuchungService(repository,mock(MieterRepository.class),mock(BuchungsnummerCounterRepository.class),
            mock(com.kcserver.kjfp.repository.PersonRepository.class),mock(com.kcserver.core.repository.VereinRepository.class),
            mock(MietobjektRepository.class),mock(MietbereichRepository.class),mock(MietpreisRepository.class),mock(MietbereichRabattRepository.class),
            mock(ZaehlerRepository.class),mock(ZaehlerstandRepository.class),mock(VerbrauchsabrechnungService.class),
            mock(BuchungVerbrauchskostenRepository.class),mock(BuchungFinanzbewegungRepository.class),mails);
        buchung=new Buchung();buchung.setId(1L);buchung.setHerkunft("HOMEPAGE");
        Mietobjekt objekt=new Mietobjekt();objekt.setId(7L);buchung.setMietobjekt(objekt);buchung.setMieter(new com.kcserver.core.entity.Person());
        buchung.setAnreise(LocalDateTime.now().plusDays(5));buchung.setAbreise(LocalDateTime.now().plusDays(7));
        when(repository.findLockedById(1L)).thenReturn(Optional.of(buchung));when(repository.save(any())).thenAnswer(i->i.getArgument(0));
    }
    @Test void reservierungBestaetigungUndStornierungWerdenJeweilsVorgemerkt() {
        service.aendereStatus(1L,Buchungsstatus.RESERVIERT);service.aendereStatus(1L,Buchungsstatus.BESTAETIGT);service.aendereStatus(1L,Buchungsstatus.STORNIERT);
        verify(mails).vormerken(buchung,Buchungsstatus.RESERVIERT);verify(mails).vormerken(buchung,Buchungsstatus.BESTAETIGT);verify(mails).vormerken(buchung,Buchungsstatus.STORNIERT);
        assertEquals(Buchungsstatus.STORNIERT,buchung.getStatus());
    }
    @Test void wiederholterStatusErzeugtKeineZweiteMail() {
        service.aendereStatus(1L,Buchungsstatus.RESERVIERT);
        assertThrows(IllegalArgumentException.class,()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT));
        verify(mails,times(1)).vormerken(buchung,Buchungsstatus.RESERVIERT);
    }
    @Test void ungueltigerUebergangSendetKeineMail() {
        assertThrows(IllegalArgumentException.class,()->service.aendereStatus(1L,Buchungsstatus.ABGESCHLOSSEN));verifyNoInteractions(mails);
    }
    @Test void automatischeUmstellungBeimLesenWirdNurEinmalVorgemerkt() {
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.now().minusDays(1));
        service.getById(1L);service.getById(1L);
        verify(mails,times(1)).vormerken(buchung,Buchungsstatus.ABRECHENBAR);
    }
    @Test void automatischeSammelUmstellungErzeugtNachrichten() {
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.now().minusDays(1));
        when(repository.findByStatusAndAbreiseLessThanEqual(eq(Buchungsstatus.BESTAETIGT),any()))
            .thenAnswer(i->buchung.getStatus()==Buchungsstatus.BESTAETIGT?List.of(buchung):List.of());
        assertEquals(1,service.aktualisiereAbrechenbareBuchungen());assertEquals(0,service.aktualisiereAbrechenbareBuchungen());
        verify(mails,times(1)).vormerken(buchung,Buchungsstatus.ABRECHENBAR);
    }
}
