package com.kcserver.unit;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
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
        buchung=new Buchung();buchung.setId(1L);buchung.setHerkunft("DIREKT");
        Mietobjekt objekt=new Mietobjekt();objekt.setId(7L);buchung.setMietobjekt(objekt);buchung.setMieter(new com.kcserver.core.entity.Person());
        buchung.setAnreise(LocalDateTime.now().plusDays(5));buchung.setAbreise(LocalDateTime.now().plusDays(7));
        when(repository.findLockedById(1L)).thenReturn(Optional.of(buchung));when(repository.save(any())).thenAnswer(i->i.getArgument(0));
    }
    @Test void expliziteVersandentscheidungVormerktAuchFuerDirektInKcErstellteBuchungenEineMail() {
        service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true);
        service.aendereStatus(1L,Buchungsstatus.BESTAETIGT,List.of(),true);
        service.aendereStatus(1L,Buchungsstatus.STORNIERT,List.of(),true);
        verify(mails).senden(buchung,Buchungsstatus.RESERVIERT,Buchungsstatus.ANFRAGE);
        verify(mails).senden(buchung,Buchungsstatus.BESTAETIGT,Buchungsstatus.RESERVIERT);
        verify(mails).senden(buchung,Buchungsstatus.STORNIERT,Buchungsstatus.BESTAETIGT);
        assertEquals(Buchungsstatus.STORNIERT,buchung.getStatus());
    }
    @Test void fehlendeVersandentscheidungSendetKeineMail() {
        service.aendereStatus(1L,Buchungsstatus.RESERVIERT);
        assertEquals(Buchungsstatus.RESERVIERT,buchung.getStatus());
        verifyNoInteractions(mails);
    }
    @Test void wiederholterStatusErzeugtKeineZweiteMail() {
        service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true);
        assertThrows(IllegalArgumentException.class,()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true));
        verify(mails,times(1)).senden(buchung,Buchungsstatus.RESERVIERT,Buchungsstatus.ANFRAGE);
    }
    @Test void ungueltigerUebergangSendetKeineMail() {
        assertThrows(IllegalArgumentException.class,()->service.aendereStatus(1L,Buchungsstatus.ABGESCHLOSSEN,List.of(),true));verifyNoInteractions(mails);
    }
    @Test void versandfehlerLaesstDenBereitsGespeichertenStatusBestehen() {
        PlatformTransactionManager tx=mock(PlatformTransactionManager.class);
        when(tx.getTransaction(any(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
        java.util.concurrent.atomic.AtomicBoolean committed=new java.util.concurrent.atomic.AtomicBoolean();
        doAnswer(i -> { committed.set(true); return null; }).when(tx).commit(any());
        service.configureStatusTransaction(tx);
        doAnswer(i -> {
            assertTrue(committed.get(),"Status-Transaktion muss vor dem Mailversand committet sein");
            assertEquals(Buchungsstatus.RESERVIERT,buchung.getStatus());
            throw new IllegalStateException("Mail queue unavailable");
        }).when(mails).senden(any(),eq(Buchungsstatus.RESERVIERT),eq(Buchungsstatus.ANFRAGE));
        assertThrows(IllegalStateException.class,()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true));
        assertEquals(Buchungsstatus.RESERVIERT,buchung.getStatus());
        assertTrue(committed.get());
        verify(repository).save(buchung);
    }
    @Test void automatischeUmstellungBeimLesenSendetOhneBenutzerentscheidungKeineMail() {
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.now().minusDays(1));
        service.getById(1L);service.getById(1L);
        verifyNoInteractions(mails);
    }
    @Test void automatischeSammelUmstellungSendetOhneBenutzerentscheidungKeineMail() {
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.now().minusDays(1));
        when(repository.findByStatusAndAbreiseLessThanEqual(eq(Buchungsstatus.BESTAETIGT),any()))
            .thenAnswer(i->buchung.getStatus()==Buchungsstatus.BESTAETIGT?List.of(buchung):List.of());
        assertEquals(1,service.aktualisiereAbrechenbareBuchungen());assertEquals(0,service.aktualisiereAbrechenbareBuchungen());
        verifyNoInteractions(mails);
    }
}
